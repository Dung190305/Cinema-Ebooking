<template>
    <div class="movie-trailer w-full">
        <div class="max-w-215 w-full mx-auto pt-10 py-2">
            <!-- Thumbnail -->
            <div class="relative w-full aspect-video rounded-lg overflow-hidden cursor-pointer transition-transform duration-200 hover:scale-[1.02] hover:shadow-2xl group"
                :class="{ 'cursor-not-allowed hover:scale-100 hover:shadow-none': !hasTrailer }" @click="openModal"
                :aria-label="hasTrailer ? `Play trailer for ${title}` : 'No trailer available'"
                :tabindex="hasTrailer ? 0 : -1">
                <img v-if="thumbnailSrc" :src="thumbnailSrc" :alt="`${title} trailer thumbnail`"
                    class="w-full h-full object-cover" />
                <div v-else
                    class="w-full h-full flex flex-col items-center justify-center bg-gray-800 text-gray-400 text-sm gap-2">
                    <svg class="w-12 h-12 opacity-60" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <rect x="2" y="3" width="20" height="18" rx="2" stroke-width="2" />
                        <path d="M10 9l5 3-5 3V9z" fill="currentColor" stroke="none" />
                    </svg>
                    <span>Chưa có trailer</span>
                </div>

                <div v-if="hasTrailer"
                    class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                    <svg class="w-16 h-16 text-white drop-shadow-lg" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M8 5v14l11-7z" />
                    </svg>
                </div>
            </div>

            <!-- Modal chiếu trailer (v-if + Transition để hủy iframe khi đóng) -->
            <Transition name="modal-fade">
                <div v-if="isModalOpen && hasTrailer"
                    class="fixed inset-0 z-50 bg-black/85 flex items-center justify-center p-4" @click.self="closeModal"
                    @keydown.escape="closeModal" tabindex="-1">
                    <div class="relative w-full max-w-5xl aspect-video bg-black rounded-lg overflow-hidden shadow-2xl"
                        role="dialog" aria-modal="true" :aria-label="`Trailer phim ${title}`">
                        <button
                            class="absolute top-2 right-2 z-10 bg-black/60 text-white w-9 h-9 rounded-full flex items-center justify-center hover:bg-white/20 transition-colors"
                            @click="closeModal" aria-label="Đóng trailer">
                            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor"
                                stroke-width="2" stroke-linecap="round">
                                <path d="M18 6L6 18M6 6l12 12" />
                            </svg>
                        </button>

                        <div class="w-full h-full">
                            <iframe v-if="videoType === 'youtube'" :src="embedUrl" title="YouTube trailer player"
                                frameborder="0"
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                allowfullscreen class="w-full h-full"></iframe>
                            <div v-else
                                class="w-full h-full flex items-center justify-center text-white text-lg bg-gray-900">
                                <p>Định dạng video chưa được hỗ trợ</p>
                            </div>
                        </div>
                    </div>
                </div>
            </Transition>
        </div>
    </div>
</template>

<script setup lang="ts">
import { toRef } from 'vue'
import { useMovieTrailer } from '@/composables/useMovieTrailer'

const props = defineProps({
    trailerUrl: { type: String, default: '' },
    title: { type: String, default: 'Movie' },
    thumbnailUrl: { type: String, default: '' }
})

const {
    hasTrailer,
    videoType,
    embedUrl,
    thumbnailSrc,
    isModalOpen,
    openModal,
    closeModal
} = useMovieTrailer(
    toRef(props, 'trailerUrl'),
    toRef(props, 'thumbnailUrl')
)
</script>

<style scoped>
/* Transition cho modal */
.modal-fade-enter-active,
.modal-fade-leave-active {
    transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
    opacity: 0;
}
</style>