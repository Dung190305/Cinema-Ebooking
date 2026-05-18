import { ref, onMounted } from 'vue';
import { movieApi } from '@/api/movie.api';
import type { MovieResponse, MovieStatus } from '@/types/movie.types';

export function useMovieSection() {
    const movies = ref<MovieResponse[]>([]);
    const loading = ref(true);
    const error = ref<string | null>(null);  
    const currentStatus = ref<MovieStatus>('NOW_SHOWING');

    async function fetchMovies() {
        loading.value = true;
        error.value = null;      
        try {
        const response = await movieApi.getList({
            page: 0,
            size: 8,
            status: currentStatus.value,
        });
        
        movies.value = response.content;
        } catch (error) {
            console.error('Lỗi khi tải danh sách phim:', err);
            error.value = 'Không thể tải phim. Vui lòng thử lại.';
        } finally {
            loading.value = false;
        }
    }

    function switchTab(status: MovieStatus) {
        if (currentStatus.value === status) return;
        currentStatus.value = status;
        fetchMovies();
    }

    function onBook(id: number) {
        console.log('Đặt vé cho phim ID:', id);
    }

    onMounted(() => {
        fetchMovies();
    });

    return {
        movies,
        loading,
        error,    
        currentStatus,
        switchTab,
        onBook,
        retry: fetchMovies,
    };
}