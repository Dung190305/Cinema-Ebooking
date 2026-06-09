<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { reviewApi } from '@/api/review.api'
import { useAuthStore } from '@/stores/auth.store'
import type { ReviewResponse } from '@/types/review.types'

// ==========================================
// PROPS
// ==========================================
const props = defineProps<{
  movieId: number
}>()

const emit = defineEmits<{
  'request-login': []
  'reviews-updated': [averageRating: number, totalReviews: number]
}>()

// ==========================================
// AUTH
// ==========================================
const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const currentUserId = computed(() => authStore.user?.id)

// ==========================================
// TRẠNG THÁI KIỂM TRA
// ==========================================
const loadingCheck = ref(true)
const hasCheckedInTicket = ref(false)
const checkedInCount = ref(0)
const latestBookingCode = ref<string | null>(null)
const hasReview = ref(false)
const myReviewId = ref<number | null>(null)
const myReviewFinalText = ref('')
const isEditMode = ref(false)

// ==========================================
// DANH SÁCH BÌNH LUẬN
// ==========================================
const reviews = ref<ReviewResponse[]>([])
const totalReviews = ref(0)
const averageRating = ref(0)
const loadingReviews = ref(false)

// ==========================================
// FORM
// ==========================================
const selectedRating = ref(0)
const commentText = ref('')
const hoverRating = ref(0)
const submitting = ref(false)
const submitError = ref('')

const isStarActive = (star: number) => star <= (hoverRating.value || selectedRating.value)

// ==========================================
// SPOILER
// ==========================================
const revealedSpoilers = ref(new Set<number>())

const toggleSpoiler = (reviewId: number) => {
  if (revealedSpoilers.value.has(reviewId)) {
    revealedSpoilers.value.delete(reviewId)
  } else {
    revealedSpoilers.value.add(reviewId)
  }
}

// ==========================================
// KHỞI TẠO DỮ LIỆU
// ==========================================
const initData = async () => {
  loadingCheck.value = true

  try {
    // Luôn tải danh sách review
    await loadReviews()

    // Nếu chưa đăng nhập thì dừng
    if (!isLoggedIn.value || !currentUserId.value) return

    // Kiểm tra song song: vé check-in + review của tôi
    const [ticketRes, myReviewRes] = await Promise.all([
      reviewApi.checkTicket(props.movieId, currentUserId.value),
      reviewApi.getMyReview(props.movieId, currentUserId.value),
    ])

    // Fix lỗi TypeScript bằng cách lấy data từ AxiosResponse
    const ticketData = (ticketRes as any).data ?? ticketRes
    const myReviewData = (myReviewRes as any).data ?? myReviewRes

    hasCheckedInTicket.value = ticketData.hasCheckedInTicket
    checkedInCount.value = ticketData.checkedInCount
    latestBookingCode.value = ticketData.latestBookingCode ?? null

    hasReview.value = myReviewData.hasReview
    if (myReviewData.hasReview && myReviewData.review) {
      myReviewId.value = myReviewData.review.reviewId
      selectedRating.value = myReviewData.review.rating ?? 0
      commentText.value = myReviewData.review.comment ?? ''
      myReviewFinalText.value = myReviewData.review.finalText ?? myReviewData.review.comment ?? ''
    }
  } catch (err) {
    console.error('Lỗi khởi tạo dữ liệu review:', err)
  } finally {
    loadingCheck.value = false
  }
}

const loadReviews = async () => {
  loadingReviews.value = true
  try {
    const res = await reviewApi.getByMovieId(props.movieId, { size: 20, sort: 'createdAt,desc' })
    const page = (res as any).data ?? res
    reviews.value = page.content ?? []
    totalReviews.value = page.totalElements ?? 0

    // Tính điểm trung bình từ danh sách
    if (reviews.value.length > 0) {
      const sum = reviews.value.reduce((acc, r) => acc + (r.rating ?? 0), 0)
      averageRating.value = sum / reviews.value.length
    } else {
      averageRating.value = 0
    }

    emit('reviews-updated', averageRating.value, totalReviews.value)
  } catch (err) {
    console.error('Lỗi tải danh sách review:', err)
  } finally {
    loadingReviews.value = false
  }
}

// ==========================================
// GỬI / CẬP NHẬT BÌNH LUẬN
// ==========================================
const handleSubmit = async () => {
  if (!selectedRating.value || submitting.value) return
  submitError.value = ''
  submitting.value = true

  try {
    if (isEditMode.value && myReviewId.value) {
      // Cập nhật bình luận cũ
      await reviewApi.update(myReviewId.value, {
        userId: currentUserId.value,
        rating: selectedRating.value,
        comment: commentText.value,
      })
    } else {
      // Tạo bình luận mới
      await reviewApi.create({
        userId: currentUserId.value,
        movieId: props.movieId,
        bookingCode: latestBookingCode.value ?? '',
        rating: selectedRating.value,
        comment: commentText.value,
      })
    }

    isEditMode.value = false
    hasReview.value = true
    await loadReviews()
    // Cập nhật lại my-review để lấy reviewId nếu vừa tạo
    if (currentUserId.value) {
      const myReviewRes = await reviewApi.getMyReview(props.movieId, currentUserId.value)
      const myReviewData = (myReviewRes as any).data ?? myReviewRes
      if (myReviewData.review) {
        myReviewId.value = myReviewData.review.reviewId
      }
    }
  } catch (err: any) {
    submitError.value = err?.response?.data?.message ?? 'Đã có lỗi xảy ra, vui lòng thử lại.'
  } finally {
    submitting.value = false
  }
}

const handleEdit = () => {
  isEditMode.value = true
  commentText.value = myReviewFinalText.value
}

const handleCancelEdit = () => {
  isEditMode.value = false
}

// ==========================================
// THÊM HÀM XỬ LÝ CHUYỂN TRANG ĐĂNG NHẬP
// ==========================================
const handleGoToLogin = () => {
  emit('request-login')
}

// ==========================================
// FORMAT DATE
// ==========================================
const formatDate = (date: string | Date | null | undefined) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

const getInitial = (name: string | undefined | null) => (name ? name.charAt(0).toUpperCase() : 'U')

// ==========================================
// WATCH movieId thay đổi + login state thay đổi
// ==========================================
watch(() => props.movieId, initData, { immediate: false })
watch(isLoggedIn, (loggedIn) => {
  if (loggedIn) initData()
}, { immediate: false })
onMounted(initData)

defineExpose({
  getAverageRating: () => averageRating.value,
  getTotalReviews: () => totalReviews.value,
})
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-12 gap-6 items-start text-white max-w-7xl mx-auto p-4">
    <div class="md:col-span-5 space-y-4 md:sticky md:top-4">
      <div
        class="p-5 rounded-2xl bg-zinc-900/60 border border-white/5 shadow-xl backdrop-blur-md relative overflow-hidden"
      >
        <div class="absolute top-0 right-0 w-24 h-24 bg-amber-500/10 blur-2xl -mr-12 -mt-12"></div>
        <h3 class="text-[10px] font-black text-amber-500 uppercase tracking-widest mb-3">
          Tổng quan đánh giá
        </h3>
        <div class="flex items-baseline gap-2">
          <span
            class="text-4xl font-black text-amber-400 drop-shadow-[0_0_10px_rgba(251,191,36,0.2)]"
          >
            {{ averageRating.toFixed(1) }}
          </span>
          <span class="text-xs text-zinc-500">/ 10 điểm</span>
          <span
            class="ml-auto px-2 py-0.5 text-[10px] font-bold bg-zinc-800 text-zinc-400 rounded-full"
          >
            {{ totalReviews }} lượt đánh giá
          </span>
        </div>

        <div class="flex items-center gap-0.5 mt-3">
          <svg
            v-for="star in 10"
            :key="star"
            class="w-3.5 h-3.5"
            :class="star <= Math.round(averageRating) ? 'text-amber-400' : 'text-zinc-700'"
            :fill="star <= Math.round(averageRating) ? 'currentColor' : 'none'"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z"
            />
          </svg>
          <span class="ml-2 text-[11px] font-bold text-zinc-400">
            {{ Math.round(averageRating) }}/10
          </span>
        </div>
      </div>

      <div class="p-5 rounded-2xl bg-zinc-900/40 border border-white/5 shadow-inner">
        <div v-if="loadingCheck" class="py-8 text-center">
          <div
            class="w-6 h-6 mx-auto border-2 border-amber-500/30 border-t-amber-400 rounded-full animate-spin"
          ></div>
          <p class="text-xs text-zinc-500 mt-3">Đang kiểm tra...</p>
        </div>

        <div v-else-if="!isLoggedIn" class="text-center py-6">
          <div
            class="w-10 h-10 mx-auto mb-3 text-zinc-500 flex items-center justify-center bg-zinc-900/60 rounded-full"
          >
            <svg
              class="w-5 h-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
              />
            </svg>
          </div>
          <p class="text-xs text-zinc-400 leading-relaxed">
            Vui lòng đăng nhập để đánh giá bộ phim này.
          </p>
          <button
            @click="handleGoToLogin"
            class="mt-3 text-xs font-black text-amber-400 hover:underline tracking-wider uppercase"
          >
            Đăng nhập ngay
          </button>
        </div>

        <div v-else-if="!hasCheckedInTicket" class="space-y-3">
          <div class="flex gap-3 items-start p-4 rounded-xl bg-black/30 border border-white/[0.02]">
            <div class="p-1.5 rounded-lg bg-zinc-800 text-zinc-400 mt-0.5 shrink-0">
              <svg
                class="w-4 h-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                />
              </svg>
            </div>
            <div class="text-xs leading-relaxed text-zinc-400">
              Hệ thống chỉ cho phép những khán giả
              <span class="text-amber-400 font-semibold bg-amber-400/10 px-1 rounded"
                >đã mua vé trực tuyến</span
              >
              và
              <span class="text-amber-400 font-semibold bg-amber-400/10 px-1 rounded"
                >xem bộ phim này</span
              >
              gửi bình luận để đảm bảo tính khách quan.
            </div>
          </div>
        </div>

        <div v-else-if="hasReview && !isEditMode" class="space-y-3">
          <div class="flex items-center justify-between">
            <h4 class="text-xs font-bold text-zinc-300">Đánh giá của bạn</h4>
            <span
              class="px-2 py-0.5 text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full"
            >
              Đã đánh giá
            </span>
          </div>
          <div class="p-3 rounded-xl bg-black/30 border border-white/[0.04] space-y-2">
            <div class="flex items-center gap-1">
              <svg
                v-for="star in 10"
                :key="star"
                class="w-4 h-4"
                :class="star <= selectedRating ? 'text-amber-400' : 'text-zinc-700'"
                fill="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z"
                />
              </svg>
              <span class="ml-2 text-xs font-black text-amber-400">{{ selectedRating }}/10</span>
            </div>
            <p v-if="myReviewFinalText" class="text-xs text-zinc-300 leading-relaxed">
              {{ myReviewFinalText }}
            </p>
            <p v-else class="text-xs text-zinc-600 italic">Không có nội dung bình luận.</p>
          </div>
          <div class="flex justify-end">
            <button
              @click="handleEdit"
              class="px-4 py-1.5 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-300 hover:text-white text-xs font-bold transition-all active:scale-95 border border-white/5"
            >
              Chỉnh sửa đánh giá
            </button>
          </div>
        </div>

        <div v-else class="space-y-4">
          <div class="flex items-center justify-between">
            <h4 class="text-xs font-bold text-zinc-300">
              {{ isEditMode ? 'Chỉnh sửa đánh giá' : 'Đánh giá của bạn' }}
            </h4>
            <span v-if="checkedInCount > 1" class="text-[10px] text-zinc-500">
              {{ checkedInCount }} lần check-in
            </span>
          </div>

          <div
            class="flex items-center justify-between bg-black/40 p-3 rounded-xl border border-white/5"
          >
            <div class="flex gap-0.5">
              <button
                v-for="star in 10"
                :key="star"
                @click="selectedRating = star"
                @mouseenter="hoverRating = star"
                @mouseleave="hoverRating = 0"
                class="transition-all duration-100"
                :class="isStarActive(star) ? 'text-amber-400 scale-110' : 'text-zinc-700'"
              >
                <svg
                  class="w-5 h-5"
                  :fill="isStarActive(star) ? 'currentColor' : 'none'"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  stroke-width="1.5"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z"
                  />
                </svg>
              </button>
            </div>
            <span class="text-xs font-black text-amber-400 tabular-nums"
              >{{ selectedRating }}/10</span
            >
          </div>

          <textarea
            v-model="commentText"
            rows="3"
            placeholder="Chia sẻ cảm nhận của bạn về phim..."
            class="w-full bg-black/40 border border-white/10 rounded-xl px-3 py-2.5 text-xs text-white placeholder:text-zinc-600 focus:border-amber-500/50 focus:ring-2 focus:ring-amber-500/10 transition-all outline-none resize-none"
          ></textarea>

          <p v-if="submitError" class="text-[11px] text-red-400">{{ submitError }}</p>

          <div class="flex justify-end gap-2">
            <button
              v-if="isEditMode"
              @click="handleCancelEdit"
              class="px-4 py-2 rounded-lg bg-zinc-800 hover:bg-zinc-700 text-zinc-400 text-xs font-bold transition-all active:scale-95"
            >
              Huỷ
            </button>
            <button
              @click="handleSubmit"
              :disabled="submitting || !selectedRating"
              class="px-5 py-2 rounded-lg bg-amber-500 text-black text-xs font-black shadow-md shadow-amber-500/10 hover:bg-amber-400 active:scale-95 transition-all disabled:opacity-20 disabled:pointer-events-none"
            >
              {{ submitting ? 'ĐANG GỬI...' : isEditMode ? 'CẬP NHẬT' : 'ĐĂNG BÌNH LUẬN' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="md:col-span-7 space-y-4">
      <div class="flex items-center gap-2 pb-2 border-b border-white/[0.05]">
        <h3 class="text-xs font-black uppercase tracking-wider text-white">Bình luận & Đánh giá</h3>
      </div>

      <div v-if="loadingReviews" class="py-12 text-center">
        <div
          class="w-6 h-6 mx-auto border-2 border-amber-500/30 border-t-amber-400 rounded-full animate-spin"
        ></div>
      </div>

      <div
        v-else-if="reviews.length === 0"
        class="py-16 text-center bg-zinc-950/20 rounded-2xl border border-dashed border-white/[0.03]"
      >
        <div
          class="w-10 h-10 mx-auto mb-3 text-zinc-600 flex items-center justify-center bg-zinc-900 rounded-full"
        >
          <svg
            class="w-5 h-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="1.5"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
            />
          </svg>
        </div>
        <p class="text-xs font-bold text-zinc-400">Chưa có lượt bình luận nào</p>
        <p class="text-[11px] text-zinc-500 mt-1">
          Hãy mua vé xem phim và chia sẻ những cảm nhận đầu tiên của bạn nhé!
        </p>
      </div>

      <div v-else class="space-y-3">
        <div
          v-for="review in reviews"
          :key="review.reviewId"
          class="p-4 rounded-xl bg-white/[0.02] border border-white/[0.04] hover:bg-white/[0.04] transition-all"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2.5">
              <div
                class="w-8 h-8 rounded-full bg-gradient-to-tr from-amber-500 to-yellow-400 flex items-center justify-center text-black font-black text-xs shrink-0"
              >
                {{ getInitial(review.userName) }}
              </div>
              <div>
                <div class="flex items-center gap-1.5">
                  <h4 class="text-xs font-bold text-zinc-200">{{ review.userName }}</h4>
                  <span v-if="review.edited" class="text-[9px] text-zinc-600 italic"
                    >(đã chỉnh sửa)</span
                  >
                </div>
                <span class="text-[10px] text-zinc-500">{{ formatDate(review.createdAt) }}</span>
              </div>
            </div>

            <div
              class="flex items-center gap-1 bg-amber-500/10 px-2 py-0.5 rounded-full border border-amber-500/20 text-amber-400"
            >
              <span class="text-[11px] font-black">{{ review.rating }}</span>
              <span class="text-[9px] opacity-60">/10</span>
            </div>
          </div>

          <div v-if="review.isSpoiler && review.userId !== currentUserId && !revealedSpoilers.has(review.reviewId)">
            <div class="relative mt-3 pl-0.5">
              <p
                class="text-xs leading-relaxed text-zinc-300 blur-sm select-none pointer-events-none"
              >
                {{ review.finalText || review.comment }}
              </p>
              <div
                class="absolute inset-0 flex flex-col items-center justify-center gap-2 cursor-pointer bg-zinc-900/40 rounded-lg hover:bg-zinc-900/60 transition-colors"
                @click="toggleSpoiler(review.reviewId)"
              >
                <svg
                  class="w-5 h-5 text-red-400"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  stroke-width="1.5"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M3 3l7.07 16.97 2.51-7.39 7.39-2.51L3 3z"
                  />
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M13 13l6 6"
                  />
                </svg>
                <span class="text-[11px] font-bold text-red-400 bg-red-500/20 border border-red-500/30 px-3 py-1 rounded-full">
                  Nội dung có spoiler — bấm để xem
                </span>
              </div>
            </div>
          </div>

            <p
              v-else-if="review.finalText ?? review.comment"
              class="mt-3 text-xs text-zinc-300 leading-relaxed pl-0.5"
            >
              {{ review.finalText ?? review.comment }}
            </p>

          <button
            v-if="review.isSpoiler && review.userId !== currentUserId && revealedSpoilers.has(review.reviewId)"
            class="mt-1 text-[10px] text-zinc-500 hover:text-red-400 transition-colors"
            @click="toggleSpoiler(review.reviewId)"
          >
            ▲ Ẩn nội dung spoiler
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
