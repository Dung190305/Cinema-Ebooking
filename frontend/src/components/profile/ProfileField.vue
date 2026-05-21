<!-- components/profile/ProfileField.vue -->
<script setup lang="ts">
defineProps<{
    label: string;
    value: string;
    edit?: boolean;
    readonly?: boolean;
    type?: 'text' | 'date' | 'select';
    valueEdit?: string;
    options?: { label: string; value: string }[];
    fieldError?: string; // Thêm
}>();

defineEmits<{
    (e: 'update:valueEdit', value: string): void;
}>();
</script>

<template>
    <div>
        <label class="block text-caption text-text-tertiary mb-1">{{ label }}</label>
        <template v-if="edit && !readonly">
            <select v-if="type === 'select' && options" :value="valueEdit"
                @input="$emit('update:valueEdit', ($event.target as HTMLSelectElement).value)" :class="[
                    'w-full rounded-lg border bg-bg-base px-3 py-2 text-body text-text-primary focus:outline-none focus:ring-2',
                    fieldError ? 'border-red-500 focus:ring-red-500' : 'border-border-default focus:ring-accent'
                ]">
                <option v-for="opt in options" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                </option>
            </select>
            <input v-else :type="type" :value="valueEdit"
                @input="$emit('update:valueEdit', ($event.target as HTMLInputElement).value)" :class="[
                    'w-full rounded-lg border bg-bg-base px-3 py-2 text-body text-text-primary focus:outline-none focus:ring-2',
                    fieldError ? 'border-red-500 focus:ring-red-500' : 'border-border-default focus:ring-accent'
                ]" />
            <p v-if="fieldError" class="mt-1 text-caption text-red-500">{{ fieldError }}</p>
        </template>
        <p v-else class="text-body text-text-primary py-2">{{ value || '—' }}</p>
    </div>
</template>