// Mirrors the backend's JSON shapes. Value objects (Email, Money, ...) serialize as plain
// values via @JsonValue, so e.g. Customer.email is just a string here, not a nested object.

export interface Customer {
  customerId: string
  firstName: string
  lastName: string
  email: string
  phoneNumber: string
  dateOfBirth: string | null
  registeredAt: string
}

export interface Stylist {
  stylistId: string
  firstName: string
  lastName: string
  email: string
  phoneNumber: string
  speciality: string
  active: boolean
}

export type ServiceCategory = 'HAIRCUT' | 'COLOURING' | 'STYLING' | 'TREATMENT' | 'EXTENSIONS' | 'OTHER'

export interface SalonService {
  serviceId: string
  name: string
  description: string | null
  durationMinutes: number
  price: number
  category: ServiceCategory
  active: boolean
}

export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW'

export interface Appointment {
  appointmentId: string
  appointmentDate: string
  startTime: string
  endTime: string
  status: AppointmentStatus
  notes: string | null
  createdAt: string
  customer: Customer
  stylist: Stylist
  salonService: SalonService
  /** Code the customer chose when booking - applied when the in-store payment is recorded. */
  promoCode: string | null
}

export type LoyaltyTier = 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM'

export interface LoyaltyReward {
  loyaltyId: string
  pointsBalance: number
  totalEarned: number
  totalRedeemed: number
  tier: LoyaltyTier
  lastUpdated: string
  customer: Customer
}

export interface ProductInventory {
  productId: string
  name: string
  brand: string | null
  category: string | null
  stockQuantity: number
  reorderLevel: number
  costPrice: number
  sellingPrice: number
  salonService: SalonService | null
  /** Shown to customers on the public Products page. */
  forSale: boolean
}

export type PaymentStatus = 'PENDING' | 'PAID' | 'REFUNDED' | 'FAILED'
export type PaymentMethod = 'CASH' | 'CARD' | 'EFT' | 'VOUCHER'

export interface Payment {
  paymentId: string
  amount: number
  discount: number
  finalAmount: number
  method: PaymentMethod
  status: PaymentStatus
  paidAt: string | null
  transactionRef: string
  appointment: Appointment
  promotion: Promotion | null
  /** Loyalty points spent on this payment (R0.50 off each). */
  loyaltyPointsRedeemed: number
}

export type DiscountType = 'PERCENTAGE' | 'FIXED_AMOUNT'

export interface Promotion {
  promotionId: string
  code: string
  description: string | null
  discountType: DiscountType
  discountValue: number
  startDate: string
  endDate: string
  usageLimit: number
  usageCount: number
  /** Ended early by an admin. */
  ended: boolean
}

export interface Feedback {
  reviewId: string
  rating: number
  comment: string | null
  submittedAt: string
  verified: boolean
  appointment: Appointment
  customer: Customer
}

export type NotificationChannel = 'EMAIL' | 'SMS' | 'PUSH' | 'IN_APP'
export type NotificationType = 'APPOINTMENT_REMINDER' | 'APPOINTMENT_CONFIRMED' | 'PROMOTION'
export type NotificationStatus = 'SCHEDULED' | 'SENT' | 'FAILED'

export interface Notification {
  notificationId: string
  message: string
  type: NotificationType
  channel: NotificationChannel
  status: NotificationStatus
  scheduledAt: string
  sentAt: string | null
  appointment: Appointment | null
}

export interface Role {
  roleId: string
  name: string
  description: string | null
  /** "MODULE:ACTION" strings. Only custom roles use them. */
  permissions: string[]
}

export interface User {
  userId: string
  username: string
  email: string
  firstName: string
  lastName: string
  active: boolean
  createdAt: string
  role: Role | null
}
