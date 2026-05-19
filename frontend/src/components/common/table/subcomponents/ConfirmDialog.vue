<template>
    <!-- Backdrop -->
    <Teleport to="body">
        <Transition enter-from-class="opacity-0" enter-active-class="transition-opacity duration-150"
            leave-to-class="opacity-0" leave-active-class="transition-opacity duration-150">
            <div v-if="modelValue" class="fixed inset-0 z-60 flex items-center justify-center bg-black/40 p-4">
                <!-- Dialog -->
                <div class="w-full max-w-sm rounded-xl bg-white p-6 shadow-xl">
                    <h3 class="text-sm font-semibold text-slate-900">{{ title }}</h3>
                    <p class="mt-1.5 text-sm text-slate-500">{{ description }}</p>

                    <div class="mt-5 flex justify-end gap-2">
                        <!-- Cancel -->
                        <button class="rounded-lg px-4 py-2 text-sm text-slate-600 transition-colors hover:bg-slate-100"
                            @click="$emit('update:modelValue', false)">
                            {{ cancelLabel }}
                        </button>

                        <!-- Confirm -->
                        <button class="rounded-lg px-4 py-2 text-sm font-medium transition-colors" :class="danger
                            ? 'bg-red-500 text-white hover:bg-red-600'
                            : 'bg-slate-900 text-white hover:bg-slate-700'
                            " @click="confirm">
                            {{ confirmLabel }}
                        </button>
                    </div>
                </div>
            </div>
        </Transition>
    </Teleport>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
    modelValue: boolean
    title: string
    description?: string
    confirmLabel?: string
    cancelLabel?: string
    danger?: boolean
}>(), {
    confirmLabel: 'Xác nhận',
    cancelLabel: 'Hủy',
})

const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    confirm: []
}>()

function confirm() {
    emit('confirm')
    emit('update:modelValue', false)
}
</script>