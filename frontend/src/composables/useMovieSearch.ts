// src/composables/useMovieSearch.ts
import { ref, computed, watch } from 'vue'
import { movieApi } from '@/api/movie.api'
import type { MovieResponse } from '@/types/movie.types'
import { useRouter } from 'vue-router'

const DEBOUNCE_DELAY = 200
const MAX_DROPDOWN_ITEMS = 6

// Bỏ dấu tiếng Việt: "Bố già" → "bo gia", "ô" → "o"
function normalize(str: string): string {
    return str
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/đ/g, 'd')
        .replace(/Đ/g, 'D')
        .toLowerCase()
}

export function useMovieSearch() {
    const router = useRouter()

    const query = ref('')
    const isOpen = ref(false)
    const isLoading = ref(false)

    const cachedMovies = ref<MovieResponse[]>([])
    let cacheLoaded = false

    async function loadCache() {
        if (cacheLoaded) return
        isLoading.value = true
        try {
            const res = await movieApi.getList({ status: 'NOW_SHOWING', size: 100 })
            cachedMovies.value = res.content ?? []
            cacheLoaded = true
        } catch {
            // silent fail
        } finally {
            isLoading.value = false
        }
    }

    const dropdownItems = computed<MovieResponse[]>(() => {
        const q = normalize(query.value.trim())
        if (!q) return []
        return cachedMovies.value
            .filter(m => normalize(m.title).includes(q))
            .slice(0, MAX_DROPDOWN_ITEMS)
    })

    let debounceTimer: ReturnType<typeof setTimeout>
    watch(query, (val) => {
        clearTimeout(debounceTimer)
        debounceTimer = setTimeout(() => {
            isOpen.value = val.trim().length > 0
        }, DEBOUNCE_DELAY)
    })

    function onFocus() {
        loadCache()
        if (query.value.trim()) isOpen.value = true
    }

    function closeDropdown() {
        isOpen.value = false
    }

    function selectMovie(movie: MovieResponse) {
        closeDropdown()
        query.value = ''
        router.push(`/movies/${movie.id}`)
    }

    return {
        query,
        isOpen,
        isLoading,
        dropdownItems,
        onFocus,
        closeDropdown,
        selectMovie,
    }
}