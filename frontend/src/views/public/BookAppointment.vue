<template>
  <PublicLayout>
    <section class="site-section">
      <div class="site-container max-w-3xl">
        <SectionHeading eyebrow="Booking" level="h1">
          Book an appointment
          <template #lead>
            Pick a service and stylist, choose a time, and we will do the rest. You pay in store
            when you arrive &mdash; no card details needed.
          </template>
        </SectionHeading>

        <!-- Success state -->
        <div
          v-if="bookedAppointment"
          ref="successPanel"
          tabindex="-1"
          class="mt-12 rounded-2xl border border-ink-line bg-cream-dim p-8"
        >
          <h2 class="site-h2 text-2xl sm:text-2xl">You're booked in</h2>

          <dl class="mt-6 divide-y divide-ink-line border-y border-ink-line text-base">
            <div class="flex justify-between gap-6 py-3">
              <dt class="text-ink-muted">Service</dt>
              <dd class="text-right font-medium">{{ bookedAppointment.salonService.name }}</dd>
            </div>
            <div class="flex justify-between gap-6 py-3">
              <dt class="text-ink-muted">Stylist</dt>
              <dd class="text-right font-medium">
                {{ bookedAppointment.stylist.firstName }} {{ bookedAppointment.stylist.lastName }}
              </dd>
            </div>
            <div class="flex justify-between gap-6 py-3">
              <dt class="text-ink-muted">When</dt>
              <dd class="text-right font-medium tabular-nums">
                {{ bookedAppointment.appointmentDate }} at {{ bookedAppointment.startTime }}
              </dd>
            </div>
            <div class="flex justify-between gap-6 py-3">
              <dt class="text-ink-muted">Status</dt>
              <dd class="text-right font-medium">{{ bookedAppointment.status }}</dd>
            </div>
            <div v-if="bookedAppointment.promoCode" class="flex justify-between gap-6 py-3">
              <dt class="text-ink-muted">Promotion</dt>
              <dd class="text-right font-medium">{{ bookedAppointment.promoCode }}</dd>
            </div>
            <div class="flex justify-between gap-6 py-3">
              <dt class="text-ink-muted">{{ bookedAppointment.promoCode && quote?.valid ? 'Estimated total' : 'Amount due' }}</dt>
              <dd class="text-right font-medium tabular-nums">
                R{{ (bookedAppointment.promoCode && quote?.valid ? quote.finalPrice : bookedAppointment.salonService.price).toFixed(2) }}
              </dd>
            </div>
          </dl>

          <p class="mt-5 text-sm leading-relaxed text-ink-muted">
            Payable in store &mdash; we do not take payment online. We will confirm your booking
            shortly; check back here or under My Appointments for the update.
          </p>

          <router-link to="/customer/appointments" class="site-btn-dark mt-8">
            View my appointments
          </router-link>
        </div>

        <!-- Booking form -->
        <form v-else class="mt-12 space-y-12" @submit.prevent="submit">
          <fieldset>
            <legend class="site-eyebrow mb-5">Step 1 &mdash; Choose a service</legend>

            <p v-if="loadingServices" class="text-sm text-ink-muted">
              Loading services&hellip;
            </p>
            <p v-else-if="!services.length" class="text-sm text-ink-muted">
              No services available yet.
            </p>

            <div v-else class="grid gap-3 sm:grid-cols-2">
              <label
                v-for="service in services"
                :key="service.serviceId"
                class="cursor-pointer rounded-xl border p-5 transition-colors has-[:focus-visible]:outline has-[:focus-visible]:outline-2 has-[:focus-visible]:outline-offset-2"
                :class="
                  form.serviceId === service.serviceId
                    ? 'border-ink bg-ink text-cream'
                    : 'border-ink-line hover:border-ink'
                "
              >
                <input
                  v-model="form.serviceId"
                  type="radio"
                  name="service"
                  :value="service.serviceId"
                  class="sr-only"
                />
                <span class="block font-medium">{{ service.name }}</span>
                <span
                  class="mt-1 block text-sm tabular-nums"
                  :class="
                    form.serviceId === service.serviceId
                      ? 'text-cream/70'
                      : 'text-ink-muted'
                  "
                >
                  {{ service.durationMinutes }} min &middot; R{{ service.price.toFixed(2) }}
                </span>
              </label>
            </div>
          </fieldset>

          <fieldset>
            <legend class="site-eyebrow mb-5">Step 2 &mdash; Choose a stylist</legend>

            <p v-if="loadingStylists" class="text-sm text-ink-muted">
              Loading stylists&hellip;
            </p>
            <p v-else-if="!stylists.length" class="text-sm text-ink-muted">
              No stylists available yet.
            </p>

            <div v-else class="grid gap-3 sm:grid-cols-2">
              <label
                v-for="stylist in stylists"
                :key="stylist.stylistId"
                class="cursor-pointer rounded-xl border p-5 transition-colors has-[:focus-visible]:outline has-[:focus-visible]:outline-2 has-[:focus-visible]:outline-offset-2"
                :class="
                  form.stylistId === stylist.stylistId
                    ? 'border-ink bg-ink text-cream'
                    : 'border-ink-line hover:border-ink'
                "
              >
                <input
                  v-model="form.stylistId"
                  type="radio"
                  name="stylist"
                  :value="stylist.stylistId"
                  class="sr-only"
                />
                <span class="block font-medium">
                  {{ stylist.firstName }} {{ stylist.lastName }}
                </span>
                <span
                  class="mt-1 block text-sm"
                  :class="
                    form.stylistId === stylist.stylistId
                      ? 'text-cream/70'
                      : 'text-ink-muted'
                  "
                >
                  {{ stylist.speciality }}
                </span>
              </label>
            </div>
          </fieldset>

          <fieldset>
            <legend class="site-eyebrow mb-5">Step 3 &mdash; Pick a time</legend>
            <div class="grid gap-5 sm:grid-cols-2">
              <div>
                <label for="book-date" :class="labelClass">Date</label>
                <input
                  id="book-date"
                  v-model="form.date"
                  type="date"
                  :min="today"
                  required
                  autocomplete="off"
                  :class="inputClass"
                />
              </div>
              <div>
                <label for="book-time" :class="labelClass">Time</label>
                <input
                  id="book-time"
                  v-model="form.startTime"
                  type="time"
                  required
                  autocomplete="off"
                  :class="inputClass"
                />
                <p class="mt-2 text-sm text-ink-muted">
                  We are open {{ salon.hours[0].time }} on weekdays.
                </p>
              </div>
            </div>
          </fieldset>

          <fieldset>
            <legend class="site-eyebrow mb-5">Step 4 &mdash; Your details</legend>
            <!--
              No name/email/phone inputs any more. You must be signed in to reach this page,
              and the server books against the customer attached to YOUR session - it ignores
              any customer id sent in the request body. So there is nothing here to type, and
              nothing a visitor could change to book as somebody else.
            -->
            <div class="rounded-xl border border-ink-line p-5">
              <p class="text-base font-medium">
                {{ session.user.value?.firstName }} {{ session.user.value?.lastName }}
              </p>
              <p class="mt-1 text-sm text-ink-muted">{{ session.user.value?.email }}</p>
              <p class="mt-3 text-sm text-ink-muted">
                Booking as <strong>{{ session.user.value?.username }}</strong>.
                <router-link to="/customer/profile" class="underline underline-offset-4">
                  Update your details
                </router-link>
              </p>
            </div>
          </fieldset>

          <!-- Promotions. The price shown is calculated by the server (GET /promotions/quote); the
               final discount is applied when you pay in the salon, so nothing typed here can
               change what you are charged. -->
          <fieldset>
            <legend class="site-eyebrow mb-5">Promotion (optional)</legend>
            <ul v-if="offers.length" class="mb-4 flex flex-wrap gap-2" aria-label="Current offers">
              <li v-for="o in offers" :key="o.code">
                <button
                  type="button"
                  class="rounded-full border border-ink-line px-3 py-1.5 text-sm hover:border-ink"
                  :aria-pressed="form.promoCode.toUpperCase() === o.code"
                  @click="form.promoCode = o.code; checkPromo()"
                >
                  <strong>{{ o.code }}</strong>
                  &middot; {{ o.discountType === 'PERCENTAGE' ? `${o.discountValue}% off` : `R${o.discountValue.toFixed(2)} off` }}
                  <span class="text-ink-muted">until {{ o.validUntil }}</span>
                </button>
              </li>
            </ul>
            <div class="flex gap-3">
              <div class="flex-1">
                <label for="book-promo" class="sr-only">Promotion code</label>
                <input
                  id="book-promo"
                  v-model="form.promoCode"
                  placeholder="Promotion code"
                  autocomplete="off"
                  :class="inputClass"
                  @input="quote = null"
                />
              </div>
              <button type="button" class="site-btn-outline" :disabled="!form.promoCode.trim() || !form.serviceId || checkingPromo" @click="checkPromo">
                {{ checkingPromo ? 'Checking…' : 'Apply' }}
              </button>
            </div>
            <p v-if="!form.serviceId && form.promoCode.trim()" class="mt-2 text-sm text-ink-muted">Choose a service first to see your price.</p>
            <p v-if="quote" role="status" class="mt-3 text-sm" :class="quote.valid ? 'text-ink' : 'text-ink-muted'">
              {{ quote.message }}
              <template v-if="quote.valid">
                R{{ quote.price.toFixed(2) }} &minus; R{{ quote.discount.toFixed(2) }} =
                <strong>R{{ quote.finalPrice.toFixed(2) }}</strong>, payable in store.
              </template>
            </p>
          </fieldset>

          <fieldset>
            <label for="book-notes" :class="labelClass">Notes (optional)</label>
            <textarea
              id="book-notes"
              v-model="form.notes"
              rows="3"
              :class="[inputClass, 'h-auto py-3']"
            />
            <p class="mt-2 text-sm text-ink-muted">
              Anything we should know before your visit &mdash; a photo reference, an allergy, a
              hard deadline.
            </p>
          </fieldset>

          <!-- role="alert" so a screen reader hears the failure without moving focus. -->
          <p
            v-if="errorMessage"
            role="alert"
            class="rounded-xl border border-ink bg-cream-dim px-5 py-4 text-sm"
          >
            {{ errorMessage }}
          </p>

          <button type="submit" :disabled="!canSubmit || submitting" class="site-btn-dark w-full disabled:cursor-not-allowed disabled:opacity-40">
            {{ submitting ? 'Booking…' : 'Confirm booking' }}
          </button>

          <p class="text-center text-sm text-ink-muted">
            Your booking arrives with us as pending. We confirm it shortly afterwards.
          </p>
        </form>
      </div>
    </section>
  </PublicLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import PublicLayout from '@/components/layout/PublicLayout.vue'
import SectionHeading from '@/components/public/SectionHeading.vue'
import { servicesApi } from '@/api/services'
import { stylistsApi, type PublicStylist } from '@/api/stylists'
import { appointmentsApi } from '@/api/appointments'
import { promotionsApi, type PromotionOffer, type PromotionQuote } from '@/api/promotions'
import { ApiError } from '@/api/client'
import { useSession } from '@/composables/useSession'
import { salon } from '@/config/site'
import type { SalonService, Appointment } from '@/api/types'

const session = useSession()

const labelClass = 'mb-2 block text-sm font-medium text-ink'

const inputClass =
  'h-12 w-full rounded-xl border border-ink-line bg-transparent px-4 text-base text-ink placeholder:text-ink-muted focus:border-ink focus:outline-none'

const services = ref<SalonService[]>([])
const stylists = ref<PublicStylist[]>([])
const loadingServices = ref(true)
const loadingStylists = ref(true)
const submitting = ref(false)
const errorMessage = ref('')
const bookedAppointment = ref<Appointment | null>(null)
const successPanel = ref<HTMLElement | null>(null)

const today = new Date().toISOString().split('T')[0]

const form = reactive({
  serviceId: '',
  stylistId: '',
  date: '',
  startTime: '',
  notes: '',
  promoCode: '',
})

const offers = ref<PromotionOffer[]>([])
const quote = ref<PromotionQuote | null>(null)
const checkingPromo = ref(false)

async function checkPromo() {
  if (!form.promoCode.trim() || !form.serviceId) return
  checkingPromo.value = true
  try {
    quote.value = await promotionsApi.quote(form.promoCode.trim(), form.serviceId)
  } catch (err) {
    quote.value = null
    errorMessage.value = describeError(err, 'Could not check that code.')
  } finally {
    checkingPromo.value = false
  }
}

// The discount depends on the service's price - re-check when the service changes.
watch(() => form.serviceId, () => {
  if (quote.value && form.promoCode.trim()) checkPromo()
})

const canSubmit = computed(
  () =>
    form.serviceId &&
    form.stylistId &&
    form.date &&
    form.startTime &&
    session.isAuthenticated.value,
)

// The form is replaced by the confirmation, so move focus there or a keyboard user is stranded.
watch(bookedAppointment, async (appointment) => {
  if (!appointment) return
  await nextTick()
  successPanel.value?.focus()
})

onMounted(async () => {
  // Offers are optional - a failure here must not block booking.
  promotionsApi.available().then((list) => (offers.value = list)).catch(() => (offers.value = []))
  try {
    services.value = await servicesApi.listActive()
  } catch (err) {
    errorMessage.value = describeError(err, 'Could not load services.')
  } finally {
    loadingServices.value = false
  }

  try {
    stylists.value = await stylistsApi.listActive()
  } catch (err) {
    errorMessage.value = describeError(err, 'Could not load stylists.')
  } finally {
    loadingStylists.value = false
  }
})

function describeError(err: unknown, fallback: string): string {
  if (err instanceof ApiError) return err.message
  return fallback
}

async function submit() {
  errorMessage.value = ''
  submitting.value = true
  try {
    const appointment = await appointmentsApi.book({
      // Sent for backwards compatibility with the request shape; the server overrides it
      // with the signed-in customer, so it cannot be used to book on another account.
      customerId: session.customerId.value ?? '',
      stylistId: form.stylistId,
      serviceId: form.serviceId,
      date: form.date,
      startTime: form.startTime,
      notes: form.notes || null,
      // Validated again on the server; an unusable code fails the booking with a clear message.
      promoCode: form.promoCode.trim() || null,
    })
    bookedAppointment.value = appointment
  } catch (err) {
    errorMessage.value = describeError(err, 'Something went wrong while booking. Please try again.')
  } finally {
    submitting.value = false
  }
}
</script>
