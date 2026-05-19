<template>
    <Teleport to="body">
        <!-- Backdrop -->
        <Transition enter-from-class="opacity-0" enter-active-class="transition-opacity duration-200"
            leave-to-class="opacity-0" leave-active-class="transition-opacity duration-200">
            <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4"
                @click.stop @keydown.enter.prevent="onEnter">
                <!-- Modal box -->
                <Transition enter-from-class="opacity-0 scale-95"
                    enter-active-class="transition-all duration-200 ease-out" leave-to-class="opacity-0 scale-95"
                    leave-active-class="transition-all duration-150 ease-in">
                    <div v-if="modelValue" class="flex w-full flex-col rounded-xl bg-white shadow-xl"
                        :class="modalWidthClass">
                        <!-- Header -->
                        <div class="flex items-center justify-between border-b border-border-admin-default px-5 py-4">
                            <h2 class="text-sm font-semibold text-text-admin-primary">{{ title }}</h2>
                            <BaseButton variant="ghost" size="sm" rounded="full" isAdmin iconOnly @click="onClose">
                                <BaseIcon :icon="X" :size="16" />
                            </BaseButton>
                        </div>

                        <!-- Fields -->
                        <div class="max-h-[60vh] overflow-y-auto px-5 py-4 hide-scrollbar flex flex-col items-center">
                            <div class="flex flex-col gap-4 w-full">
                                <FieldRenderer v-for="col in creatableColumns" :key="col.key" :column="col"
                                    :modelValue="draft[col.key]" :error="localErrors[col.key]"
                                    :depValues="depValuesMap[col.key]"
                                    @update:modelValue="onFieldUpdate(col.key, $event)"
                                    @blur="onFieldBlur(col.key, draft)" />
                            </div>
                            <slot name="extra" :draft="draft" :fieldErrors="localErrors" />
                        </div>

                        <!-- Footer -->
                        <div class="border-t border-border-admin-subtle px-5 py-4">
                            <!-- Required fields warning -->
                            <p v-if="emptyFields.length" class="mb-3 text-xs text-red-500">
                                Vui lòng điền: {{ emptyFields.join(', ') }}
                            </p>

                            <div class="flex gap-2">
                                <button
                                    class="flex-1 rounded-lg py-2.5 text-sm text-slate-500 transition-colors hover:bg-slate-100"
                                    @click="onClose">
                                    Hủy
                                </button>
                                <BaseButton variant="primary" size="md" rounded="lg" class="flex-1"
                                    :class="!canSubmit && 'opacity-50 cursor-not-allowed pointer-events-none'"
                                    :disabled="!canSubmit || isLoading" @click="onSubmit">
                                    <span v-if="isLoading">Đang tạo...</span>
                                    <span v-else>{{ submitLabel }}</span>
                                </BaseButton>
                            </div>
                        </div>

                    </div>
                </Transition>
            </div>
        </Transition>
    </Teleport>
</template>

<script setup lang="ts" generic="T extends Record<string, unknown>">
import { ref, computed, watch } from 'vue'
import { X } from 'lucide-vue-next'
import BaseIcon from '@/components/ui/icon/BaseIcon.vue'
import BaseButton from '@/components/ui/button/BaseButton.vue'
import FieldRenderer from '@/components/common/table/subcomponents/FieldRenderer.vue'
import type { ColumnDef } from '@/components/common/table/types/table'

// ── Props & Emits ─────────────────────────────────────────────────────────────

const props = withDefaults(
    defineProps<{
        modelValue: boolean                   // v-model để mở/đóng
        columns: ColumnDef[]                  // dùng lại ColumnDef — same source of truth
        title?: string
        submitLabel?: string
        isLoading?: boolean
        fieldErrors?: Record<string, string>  // lỗi từ backend sau khi submit
        size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl'
        onFieldChange?: (key: string, value: unknown, draft: Record<string, unknown>) => void
        onFieldBlur?: (key: string, draft: Record<string, unknown>) => void
    }>(),
    {
        title: 'Tạo mới',
        submitLabel: 'Tạo',
        isLoading: false,
        fieldErrors: () => ({}),
        size: 'md',
        onFieldChange: undefined,
        onFieldBlur: undefined,
    },
)

const emit = defineEmits<{
    'update:modelValue': [value: boolean]
    submit: [draft: Record<string, unknown>]
}>()

const modalWidthClass = computed(() => {
    switch (props.size) {
        case 'sm': return 'max-w-sm'
        case 'lg': return 'max-w-lg'
        case 'xl': return 'max-w-xl'
        case '2xl': return 'max-w-2xl'
        default: return 'max-w-md'
    }
})

const localErrors = ref<Record<string, string>>({})

// Sync từ prop khi backend trả lỗi mới
watch(() => props.fieldErrors, (val) => {
    localErrors.value = { ...val }
}, { deep: true })

// Reset localErrors khi modal mở lại
watch(() => props.modelValue, (open) => {
    if (open) {
        draft.value = buildEmptyDraft()
        localErrors.value = {}   // ← thêm dòng này vào watch đã có
    }
})

function onFieldUpdate(key: string, value: unknown) {
    draft.value[key] = value
    delete localErrors.value[key]
    props.onFieldChange?.(key, value, draft.value)
}

function onFieldBlur(key: string) {
    props.onFieldBlur?.(key, draft.value)
}

// ── Chỉ hiện field được tạo mới: không readonly, không hideInTable=false chủ động ──
// readonly = true → field do backend tự gen (id, createdAt...) → bỏ qua khi create
const creatableColumns = computed(() =>
    props.columns.filter((c) => !c.readonly && !c.hideInCreate)
)
// ── Draft — reset mỗi lần modal mở ───────────────────────────────────────────
const draft = ref<Record<string, unknown>>({})

function buildEmptyDraft(): Record<string, unknown> {
    return Object.fromEntries(
        creatableColumns.value.map((c) => {
            // enum: pre-select option đầu tiên
            if (c.type === 'enum') {
                const firstOpt = c.options?.[0]
                const defaultVal = typeof firstOpt === 'object' && firstOpt !== null
                    ? (firstOpt as any).value ?? ''
                    : firstOpt ?? ''
                return [c.key, defaultVal]
            }
            // relation: null để FieldRenderer biết "chưa chọn" (placeholder select)
            if (c.type === 'relation') return [c.key, null]
            // boolean: false
            if (c.type === 'boolean') return [c.key, false]
            if (c.type === 'multiselect') return [c.key, []]
            return [c.key, '']
        }),
    )
}

// Reset draft mỗi khi modal mở
watch(
    () => props.modelValue,
    (open) => {
        if (open) {
            draft.value = buildEmptyDraft()
            localErrors.value = {}
        }
    },
    { immediate: true },
)

// ── depValuesMap — truyền deps xuống FieldRenderer cho dependent fields ────────
//
// Với mỗi column có dependsOn, tính object { [depKey]: draft[depKey] }.
// Ví dụ: roomLayoutId.dependsOn = ['roomId', 'startTime']
//   → depValuesMap['roomLayoutId'] = { roomId: draft.roomId, startTime: draft.startTime }
//
// FieldRenderer watch prop này, re-fetch dependentLoader khi deps thay đổi.
// Với column không có dependsOn → depValuesMap[key] = undefined → ignored.
const depValuesMap = computed(() => {
    const result: Record<string, Record<string, unknown>> = {}
    const draftObj = draft.value

    for (const col of creatableColumns.value) {
        if (col.dependsOn?.length) {
            result[col.key] = {}
            for (const dep of col.dependsOn) {
                result[col.key][dep] = draftObj[dep]  // Luôn lấy từ draft hiện tại
            }
        }
    }
    return result
})

// ── Validate required fields ──────────────────────────────────────────────────
const emptyFields = computed(() =>
    creatableColumns.value
        .filter((c) => {
            if (c.required === false) return false
            const val = draft.value[c.key]
            // Multiselect bắt buộc phải có ít nhất 1 phần tử
            if (c.type === 'multiselect') {
                return !Array.isArray(val) || val.length === 0
            }
            return val === '' || val == null
        })
        .map((c) => c.label),
)

const canSubmit = computed(() => emptyFields.value.length === 0 && !props.isLoading)

// ── Actions ───────────────────────────────────────────────────────────────────
function onSubmit() {
    if (!canSubmit.value) return
    emit('submit', { ...draft.value })
}

function onEnter(event: KeyboardEvent) {
    // Không submit nếu đang focus vào textarea (Enter để xuống dòng)
    const activeEl = document.activeElement
    if (activeEl?.tagName === 'TEXTAREA') return
    if (canSubmit.value) {
        onSubmit()
    }
}

function onClose() {
    emit('update:modelValue', false)
}
</script>