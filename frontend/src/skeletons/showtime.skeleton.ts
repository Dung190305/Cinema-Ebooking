import type { SkeletonBlock } from '@/components/ui/skeleton/Skeleton.vue'

export const showtimeCardSkeletonBlocks: SkeletonBlock[] = [
  // Row chính: poster (avatar) + nội dung
  { type: 'avatar', width: 128, height: 128, class: 'sm:w-32 sm:h-32 rounded-md flex-shrink-0' },
  {
    type: 'box',
    height: 20,
    width: '75%',
    class: 'mb-2 rounded-md'
  },
  {
    type: 'text',
    width: '50%',
    height: 16,
    class: 'mb-1'
  },
  {
    type: 'text',
    width: '60%',
    height: 16,
    class: 'mb-3'
  },
  {
    type: 'box',
    width: 80,
    height: 32,
    class: 'ml-auto rounded-md'
  }
]

// Dùng cho danh sách lặp lại
export const showtimeListSkeleton = (count = 5): SkeletonBlock[] => {
  return Array(count).fill(null).flatMap(() => [
    { type: 'box', height: 8, class: 'mb-2' }, // khoảng cách giữa các card
    ...showtimeCardSkeletonBlocks
  ])
}