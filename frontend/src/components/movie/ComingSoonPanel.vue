<template>
    <div class="space-y-5">
        <!-- Thông báo rõ lý do -->
        <div class="rounded-lg bg-overlay-light-10 border border-border-default p-4 text-center space-y-2">
            <BaseIcon :icon="CalendarClock" :size="32" class="text-accent mx-auto" />
            <p class="font-semibold text-text-primary">Phim sắp ra mắt</p>
            <p class="text-sm text-text-secondary leading-relaxed">
                Suất chiếu sẽ được mở bán trước ngày khởi chiếu khoảng <span class="text-accent font-medium">1
                    tuần</span>.
                Hãy quay lại sau hoặc đăng ký nhận thông báo.
            </p>
        </div>

        <!-- CTA đăng ký thông báo -->
        <BaseButton variant="primary" class="w-full" :loading="notifyLoading" :disabled="notifyRegistered"
            @click="handleNotify">
            <BaseIcon :icon="notifyRegistered ? BellRing : Bell" :size="16" />
            {{ notifyRegistered ? 'Đã đăng ký nhận thông báo' : 'Thông báo khi mở bán' }}
        </BaseButton>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { CalendarClock, Bell, BellRing } from 'lucide-vue-next'

defineProps<{ movieId: number }>()

const notifyLoading = ref(false)
const notifyRegistered = ref(false)

const handleNotify = async () => {
    notifyLoading.value = true
    // TODO: gọi API đăng ký notification sau
    await new Promise(r => setTimeout(r, 500)) // giả lập
    notifyRegistered.value = true
    notifyLoading.value = false
}
</script>