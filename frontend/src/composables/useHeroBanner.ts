// src/composables/useHeroBanner.ts
import { ref, onMounted } from 'vue'
import { movieApi } from '@/api/movie.api'
import type { MovieResponse } from '@/types/movie.types'
import { useCloudinaryImage } from '@/composables/useCloudinaryImage'

export interface HeroBannerItem {
    imageUrl: string
    title: string
    movieId: number
}

export function useHeroBanner() {
    const banners = ref<HeroBannerItem[]>([])
    const loading = ref(true)

    const { getTransformedUrl } = useCloudinaryImage()

    async function fetchBanners() {
        loading.value = true
        try {
            const res = await movieApi.getList({
                status: 'NOW_SHOWING',
                sort: 'releaseDate,desc',
                size: 7,
            })

            banners.value = res.content.map((movie: MovieResponse) => ({
                imageUrl: getTransformedUrl(movie.bannerUrl, {
                    width: 2200,
                    height: 720,
                    crop: 'fill',
                    gravity: 'auto:subject',
                    quality: 'auto:good',
                    format: 'auto',
                }),
                title: movie.title,
                movieId: movie.id,
            }))
        } catch (error) {
            console.error('Không thể tải banner:', error)
        } finally {
            loading.value = false
        }
    }

    onMounted(fetchBanners)

    return { banners, loading }  // ← Đảm bảo export loading
}