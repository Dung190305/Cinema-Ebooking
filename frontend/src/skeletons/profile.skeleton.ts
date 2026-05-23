// skeletons/profile.skeleton.ts
import type { SkeletonBlock } from '@/components/ui/skeleton/Skeleton.vue'

export const personalInfoSkeletonBlocks: SkeletonBlock[] = [
  // Header: avatar + tên + email + nút edit
  { type: 'avatar', width: 80, height: 80, class: 'mr-4' },
  { type: 'text', width: '200px', height: 24, class: 'mb-2' },
  { type: 'text', width: '150px', height: 16, class: 'mb-4' },
  { type: 'text', width: '80px', height: 32, class: 'ml-auto' }, // nút edit giả

  // Các field trong grid (2 cột)
  { type: 'box', height: 40, repeat: 4, class: 'w-full' },
]

// Skeleton cho LoyaltySection
export const loyaltySkeletonBlocks: SkeletonBlock[] = [
  { type: 'box', height: 100, rounded: 'rounded-xl', class: 'mb-4' },
  { type: 'text', width: '60%', height: 20, class: 'mx-auto mb-2' },
  { type: 'box', height: 8, rounded: 'full', class: 'w-full my-3' },
  { type: 'text', width: '50%', height: 16, class: 'mx-auto' },
]