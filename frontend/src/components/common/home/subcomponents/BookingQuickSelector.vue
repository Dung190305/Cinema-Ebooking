<template>
    <section class="w-full flex items-center justify-center z-20">
        <div class="flex items-center max-w-280 w-fit pl-3 h-18 gap-1 bg-bg-surface rounded-sm overflow-hidden">
            <!-- Chọn Phim -->
            <div
                class="w-56 lg:w-64 h-full flex cursor-pointer items-center justify-center bg-bg-surface px-2 text-center transition-all hover:bg-[#2A2A2A] hover:shadow-sm">
                <div class="flex w-full justify-between items-center text-text-primary gap-2 min-w-0">
                    <div class="flex gap-2 items-center min-w-0">
                        <div
                            class="w-6 h-6 rounded-full bg-accent flex items-center justify-center text-xs font-bold shrink-0">
                            1</div>
                        <span class="text-body text-text-primary truncate">Chọn Phim</span>
                    </div>
                    <BaseIcon :icon="ChevronDown" :size="12" class="shrink-0" />
                </div>
            </div>

            <!-- Chọn Rạp -->
            <div
                class="w-48 lg:w-56 h-full flex cursor-pointer items-center justify-center bg-bg-surface px-2 text-center transition-all hover:bg-[#2A2A2A] hover:shadow-sm">
                <div class="flex w-full justify-between items-center text-text-primary gap-2 min-w-0">
                    <div class="flex gap-2 items-center min-w-0">
                        <div
                            class="w-6 h-6 rounded-full bg-accent flex items-center justify-center text-xs font-bold shrink-0">
                            2</div>
                        <span class="text-body text-text-primary truncate">Chọn Rạp</span>
                    </div>
                    <BaseIcon :icon="ChevronDown" :size="12" class="shrink-0" />
                </div>
            </div>

            <!-- Chọn Ngày -->
            <div
                class="w-44 lg:w-52 h-full flex cursor-pointer items-center justify-center bg-bg-surface px-2 text-center transition-all hover:bg-[#2A2A2A] hover:shadow-sm">
                <div class="flex w-full justify-between items-center text-text-primary gap-2 min-w-0">
                    <div class="flex gap-2 items-center min-w-0">
                        <div
                            class="w-6 h-6 rounded-full bg-accent flex items-center justify-center text-white text-xs font-bold shrink-0">
                            3</div>
                        <span class="text-body text-text-primary truncate">Chọn Ngày</span>
                    </div>
                    <BaseIcon :icon="ChevronDown" :size="12" class="shrink-0" />
                </div>
            </div>

            <!-- Chọn Suất -->
            <div
                class="w-56 lg:w-64 h-full flex cursor-pointer items-center justify-center bg-bg-surface px-2 text-center transition-all hover:bg-[#2A2A2A] hover:shadow-sm">
                <div class="flex w-full justify-between items-center text-text-primary gap-2 min-w-0">
                    <div class="flex gap-2 items-center min-w-0">
                        <div
                            class="w-6 h-6 rounded-full bg-accent flex items-center justify-center text-white text-xs font-bold shrink-0">
                            4</div>
                        <span class="text-body text-text-primary truncate">Chọn Suất</span>
                    </div>
                    <BaseIcon :icon="ChevronDown" :size="12" class="shrink-0" />
                </div>
            </div>

            <!-- Nút hành động -->
            <div class="shrink-0">
                <BaseButton variant="primary" size="4xl" rounded="none">
                    Đặt vé ngay
                </BaseButton>
            </div>
        </div>
    </section>
</template>

<script setup lang="ts">
import BaseButton from '@/components/ui/button/BaseButton.vue';
import BaseIcon from '@/components/ui/icon/BaseIcon.vue';
import { ChevronDown } from 'lucide-vue-next';
</script>