<template>
    <Transition name="search-bar">
        <div v-if="isVisible" class="w-full px-4 sm:px-8 md:px-16 py-3" ref="wrapperRef">
            <div class="relative max-w-2xl mx-auto">

                <!-- Input -->
                <div class="flex items-center gap-3 bg-bg-surface border border-accent/30 rounded-xl px-4 py-2.5 shadow-lg transition-all duration-200"
                    :class="{ 'border-accent': isFocused }">
                    <BaseIcon :icon="Search" :size="18" class="text-text-primary shrink-0" aria-hidden="true" />
                    <input ref="inputRef" v-model="query" type="search" autocomplete="off" spellcheck="false"
                        placeholder="Tìm phim đang chiếu..."
                        class="flex-1 bg-transparent text-text-primary placeholder:text-text-secondary text-caption outline-none"
                        @focus="onInputFocus" @blur="onInputBlur" @keydown.escape="$emit('close')" />
                    <button v-if="query" @click="clearQuery"
                        class="shrink-0 flex items-center justify-center text-text-primary hover:bg-overlay-dark-10 transition-colors rounded-full p-1"
                        aria-label="Xóa tìm kiếm">
                        <BaseIcon :icon="X" :size="18" />
                    </button>
                </div>

                <!-- Dropdown -->
                <Transition name="dropdown">
                    <div v-if="isOpen && dropdownItems.length > 0"
                        class="absolute top-full left-0 right-0 mt-2 bg-bg-surface border border-border-default rounded-xl overflow-hidden shadow-xl z-50"
                        role="listbox">
                        <button v-for="movie in dropdownItems" :key="movie.id" role="option"
                            class="w-full flex items-center gap-3 px-3 py-2 text-left hover:bg-bg-elevated transition-colors duration-100"
                            @mousedown.prevent="handleSelect(movie)">
                            <!-- Poster -->
                            <div class="shrink-0 w-10 h-14 rounded-lg overflow-hidden bg-bg-elevated">
                                <img v-if="movie.posterUrl"
                                    :src="getTransformedUrl(movie.posterUrl, { width: 80, height: 112, crop: 'fill' })"
                                    :alt="movie.title" class="w-full h-full object-cover" loading="lazy" />
                                <div v-else class="w-full h-full flex items-center justify-center">
                                    <BaseIcon :icon="Film" :size="16" class="text-text-secondary" />
                                </div>
                            </div>
                            <!-- Info -->
                            <div class="flex-1 min-w-0">
                                <p class="text-sm font-medium text-text-primary truncate">{{ movie.title }}</p>
                                <p class="text-xs text-text-secondary mt-0.5 flex items-center gap-2">
                                    <span>{{ movie.duration }} phút</span>
                                    <span class="inline-block w-1 h-1 rounded-full bg-text-secondary"></span>
                                    <span
                                        class="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-accent/10 text-accent">
                                        {{ movie.ageRating }}
                                    </span>
                                </p>
                                <p v-if="movie.genres?.length" class="text-xs text-text-secondary truncate mt-0.5">
                                    {{movie.genres.slice(0, 2).map(g => g.name).join(' · ')}}
                                </p>
                            </div>
                        </button>
                    </div>
                </Transition>

                <!-- Empty state: chỉ 1 dòng text, không icon phức tạp -->
                <Transition name="dropdown">
                    <div v-if="isOpen && query.trim() && dropdownItems.length === 0 && !isLoading"
                        class="absolute top-full left-0 right-0 mt-2 bg-bg-surface border border-border-default rounded-xl px-4 py-4 text-center shadow-xl z-50">
                        <p class="text-sm text-text-secondary">Không tìm thấy kết quả cho "<span
                                class="text-text-primary">{{ query }}</span>"</p>
                    </div>
                </Transition>

            </div>
        </div>
    </Transition>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { Search, X, Film } from 'lucide-vue-next'
import { useMovieSearch } from '@/composables/useMovieSearch'
import { useCloudinaryImage } from '@/composables/useCloudinaryImage'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import type { MovieResponse } from '@/types/movie.types'

const props = defineProps<{ isVisible: boolean }>()
const emit = defineEmits<{ close: [] }>()

const { getTransformedUrl } = useCloudinaryImage()
const { query, isOpen, isLoading, dropdownItems, onFocus, closeDropdown, selectMovie } = useMovieSearch()

const inputRef = ref<HTMLInputElement | null>(null)
const wrapperRef = ref<HTMLElement | null>(null)
const isFocused = ref(false)

watch(() => props.isVisible, async (val) => {
    if (val) {
        await nextTick()
        inputRef.value?.focus()
    } else {
        query.value = ''
        closeDropdown()
    }
})

function onInputFocus() {
    isFocused.value = true
    onFocus()
}

function onInputBlur() {
    isFocused.value = false
    setTimeout(() => closeDropdown(), 150)
}

function clearQuery() {
    query.value = ''
    inputRef.value?.focus()
}

function handleSelect(movie: MovieResponse) {
    selectMovie(movie)
    emit('close')
}

function handleClickOutside(e: MouseEvent) {
    if (wrapperRef.value && !wrapperRef.value.contains(e.target as Node)) {
        emit('close')
    }
}

watch(() => props.isVisible, (val) => {
    if (val) document.addEventListener('mousedown', handleClickOutside)
    else document.removeEventListener('mousedown', handleClickOutside)
}, { immediate: true })
</script>

<style scoped>
.search-bar-enter-active,
.search-bar-leave-active {
    transition: opacity 0.2s ease, transform 0.2s ease, max-height 0.25s ease;
    overflow: hidden;
}

.search-bar-enter-from,
.search-bar-leave-to {
    opacity: 0;
    transform: translateY(-8px);
    max-height: 0;
}

.search-bar-enter-to,
.search-bar-leave-from {
    opacity: 1;
    transform: translateY(0);
    max-height: 200px;
}

.dropdown-enter-active,
.dropdown-leave-active {
    transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
    opacity: 0;
    transform: translateY(-4px);
}

input[type="search"]::-webkit-search-cancel-button {
    display: none;
}

input[type="search"]::-ms-clear {
    display: none;
}
</style>