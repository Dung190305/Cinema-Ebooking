<!-- components/ui/LoadingOverlay.vue -->
<template>
    <Teleport to="body">
        <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div class="bg-bg-surface rounded-xl p-4 shadow-xl flex items-center gap-3">
                <div class="w-6 h-6 border-2 border-accent border-t-transparent rounded-full animate-spin"></div>
                <span class="text-sm text-text-primary">Đang xử lý...</span>
            </div>
        </div>
    </Teleport>
</template>

<script setup lang="ts">
defineProps<{ visible: boolean }>()
</script>