// composables/useMovieTrailer.ts
import { ref, computed, type Ref } from 'vue'
import { getYouTubeIdFromUrl, getYouTubeThumbnailUrl } from '@/utils/youtube'

export function useMovieTrailer(trailerUrl: Ref<string>, fallbackThumbnailUrl: any) {
  const isModalOpen = ref(false)

  // Xác định loại video (có thể mở rộng thêm Vimeo, MP4...)
  const videoType = computed(() => {
    const url = trailerUrl.value.trim()
    if (!url) return null
    if (/youtube\.com|youtu\.be/.test(url)) return 'youtube'
    // if (/vimeo\.com/.test(url)) return 'vimeo'
    return 'unknown'
  })

  const hasTrailer = computed(() => videoType.value === 'youtube') // tạm thời chỉ hỗ trợ YouTube

  // Lấy ID video YouTube
  function extractYouTubeID(url: string) {
    const patterns = [
      /(?:youtube\.com\/watch\?v=)([^&]+)/,
      /(?:youtu\.be\/)([^?]+)/,
      /(?:youtube\.com\/embed\/)([^/?]+)/
    ]
    for (const p of patterns) {
      const match = url.match(p)
      if (match) return match[1]
    }
    return ''
  }

  // URL nhúng autoplay cho iframe
  const embedUrl = computed(() => {
    if (videoType.value === 'youtube') {
      const id = extractYouTubeID(trailerUrl.value)
      return id ? `https://www.youtube.com/embed/${id}?autoplay=1&rel=0` : ''
    }
    return trailerUrl.value
  })

  const thumbnailSrc = computed(() => {
    const url = trailerUrl.value
    if (!url) return fallbackThumbnailUrl.value // fallback poster
    
    const videoId = getYouTubeIdFromUrl(url)
    if (videoId) {
      // Thử dùng chất lượng maxres, nếu lỗi 404 thì fallback xuống hq
      return getYouTubeThumbnailUrl(videoId, 'maxres')
    }
    
    // Nếu không phải YouTube, dùng poster
    return fallbackThumbnailUrl.value
  })

  function openModal() {
    if (!hasTrailer.value) return
    isModalOpen.value = true
    document.body.style.overflow = 'hidden'
  }

  function closeModal() {
    isModalOpen.value = false
    document.body.style.overflow = ''
  }

  return {
    hasTrailer,
    videoType,
    embedUrl,
    thumbnailSrc,
    isModalOpen,
    openModal,
    closeModal
  }
}