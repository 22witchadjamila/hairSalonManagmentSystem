// Photography for the public marketing pages.
//
// Every photo is served straight from the Unsplash CDN - nothing is committed to the repo.
// All ids below were checked to return 200 from images.unsplash.com; if one ever 404s, swap
// the id for another from https://unsplash.com/s/photos/hair-salon and keep the alt text honest.
//
// Unsplash's licence allows free commercial use without attribution, but crediting the
// photographer is good manners if these survive past the demo.

export interface SitePhoto {
  /** Unsplash photo id, e.g. 'photo-1634449571010-02389ed0f9b0'. */
  id: string
  /** Describes what is actually in the frame, for screen readers. */
  alt: string
}

/** Builds a CDN url at a given width. `auto=format` serves WebP/AVIF where supported. */
export function photoUrl(photo: SitePhoto, width: number, quality = 72): string {
  return `https://images.unsplash.com/${photo.id}?auto=format&fit=crop&w=${width}&q=${quality}`
}

/** Matching srcset so small screens never download a 2000px hero. */
export function photoSrcset(photo: SitePhoto, widths: number[] = [640, 960, 1280, 1920]): string {
  return widths.map((w) => `${photoUrl(photo, w)} ${w}w`).join(', ')
}

export const heroPhoto: SitePhoto = {
  id: 'photo-1634449571010-02389ed0f9b0',
  alt: 'A stylist cutting a seated client’s hair in a salon',
}

export const aboutPhoto: SitePhoto = {
  id: 'photo-1633681926022-84c23e8cb2d6',
  alt: 'Salon interior with black styling chairs, round mirrors and a brick wall',
}

export const ctaPhoto: SitePhoto = {
  id: 'photo-1600948836101-f9ffda59d250',
  alt: 'Three black salon chairs facing circular mirrors on a dark wall',
}

/**
 * Shot on a near-white backdrop with the tools rising from the bottom edge, leaving a
 * large empty top. The band on the landing page sits the headline in that empty space and
 * blends the backdrop away with `mix-blend-multiply`, so nothing reads as a photo edge.
 */
export const craftPhoto: SitePhoto = {
  id: 'photo-1595475884562-073c30d45670',
  alt: 'Five raised hands holding paddle brushes, a round brush, scissors and a tinting brush',
}

export const contactPhoto: SitePhoto = {
  id: 'photo-1781450090585-1a511b7066d9',
  alt: 'A modern hair salon interior with stylish chairs and mirrors',
}

/** The results / gallery grid. */
export const galleryPhotos: SitePhoto[] = [
  { id: 'photo-1580618672591-eb180b1a973f', alt: 'A stylist blow-drying a client’s hair with a round brush' },
  { id: 'photo-1774660810725-fb04e002003d', alt: 'A stylist pinning a seated client’s hair into an updo' },
  { id: 'photo-1503951914875-452162b0f3f1', alt: 'A man having his hair cut in a barber’s chair' },
  { id: 'photo-1761839256791-6a93f89fb8b0', alt: 'A stylist draping a client with a black cape before a cut' },
  { id: 'photo-1605497788044-5a32c7078486', alt: 'A barber in a denim apron blow-drying a client’s hair' },
  { id: 'photo-1773904215697-e6c21fc27ac2', alt: 'Styling chairs and wash basins in a dimly lit salon' },
  { id: 'photo-1595475884562-073c30d45670', alt: 'Hairbrushes, scissors and a tinting brush held in two hands' },
  { id: 'photo-1560869713-7d0a29430803', alt: 'A hand holding a heated hair curler' },
  { id: 'photo-1761839256840-7780a45b85dc', alt: 'A client smiling towards the camera after a salon appointment' },
]

/**
 * Service categories come from the backend (SalonService.category), so this map is keyed by
 * the enum values the API already returns. Anything unrecognised falls back to `fallback`.
 */
const serviceCategoryPhotos: Record<string, SitePhoto> = {
  HAIRCUT: { id: 'photo-1503951914875-452162b0f3f1', alt: 'A haircut in progress in a barber’s chair' },
  STYLING: { id: 'photo-1774660810725-fb04e002003d', alt: 'A stylist creating an updo for a client' },
  COLORING: { id: 'photo-1595475884562-073c30d45670', alt: 'Colouring brushes and salon tools' },
  COLOURING: { id: 'photo-1595475884562-073c30d45670', alt: 'Colouring brushes and salon tools' },
  TREATMENT: { id: 'photo-1560869713-7d0a29430803', alt: 'A heated styling tool used for a hair treatment' },
  EXTENSIONS: { id: 'photo-1761839256840-7780a45b85dc', alt: 'A client after a hair appointment' },
}

const fallbackServicePhoto: SitePhoto = {
  id: 'photo-1580618672591-eb180b1a973f',
  alt: 'A stylist blow-drying a client’s hair',
}

export function serviceCategoryPhoto(category: string | null | undefined): SitePhoto {
  if (!category) return fallbackServicePhoto
  return serviceCategoryPhotos[category.toUpperCase()] ?? fallbackServicePhoto
}

/** Portraits used when a stylist has no photo of their own on the backend. */
export const stylistFallbackPhotos: SitePhoto[] = [
  { id: 'photo-1554519934-e32b1629d9ee', alt: 'Portrait of a salon stylist' },
  { id: 'photo-1574015974293-817f0ebebb74', alt: 'Portrait of a salon stylist' },
  { id: 'photo-1761839256840-7780a45b85dc', alt: 'Portrait of a salon stylist' },
]

/** Stable per-stylist portrait so the same person keeps the same photo between renders. */
export function stylistPhoto(key: string): SitePhoto {
  let hash = 0
  for (let i = 0; i < key.length; i += 1) hash = (hash * 31 + key.charCodeAt(i)) >>> 0
  return stylistFallbackPhotos[hash % stylistFallbackPhotos.length]
}
