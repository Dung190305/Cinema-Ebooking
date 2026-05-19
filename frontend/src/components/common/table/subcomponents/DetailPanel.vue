<template>
    <!-- Backdrop -->
    <Teleport to="body">
        <Transition enter-from-class="opacity-0" enter-active-class="transition-opacity duration-200"
            leave-to-class="opacity-0" leave-active-class="transition-opacity duration-200">
            <div v-if="item" class="fixed inset-0 z-40 bg-black/20" @click="tryClose" />
        </Transition>

        <!-- Panel -->
        <Transition enter-from-class="translate-x-full" enter-active-class="transition-transform duration-250 ease-out"
            leave-to-class="translate-x-full" leave-active-class="transition-transform duration-250 ease-in">
            <div v-if="item" class="fixed inset-y-0 right-0 z-50 flex w-full max-w-md flex-col bg-white shadow-2xl">
                <!-- Header -->
                <div class="flex items-center justify-between border-b border-slate-100 px-5 py-4">
                    <h2 class="text-sm font-semibold text-slate-900">Chi tiết</h2>
                    <button
                        class="rounded-md p-1 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600"
                        @click="tryClose">
                        <X class="size-4" />
                    </button>
                </div>

                <!-- Fields -->
                <div class="flex-1 overflow-y-auto px-5 py-4">
                    <div class="flex flex-col gap-4">
                        <!--
                            readonlyInEdit: field editable trong Create nhưng readonly trong Detail.
                            Spread column và override readonly = true khi readonlyInEdit được set.
                        -->
                        <FieldRenderer v-for="col in editableColumns" :key="col.key" :column="isReadonlyInEdit(col, draft)
                            ? { ...col, readonly: true }
                            : col
                            " :modelValue="draft[col.key]" :error="localErrors[col.key]"
                            :depValues="depValuesMap[col.key]" @update:modelValue="onFieldUpdate(col.key, $event)" />
                    </div>
                </div>

                <!-- Footer -->
                <div class="border-t border-slate-100 px-5 pb-8 pt-4 flex flex-col gap-2">
                    <!-- Validation error summary -->
                    <p v-if="emptyFields.length" class="text-xs text-red-500">
                        Vui lòng điền: {{ emptyFields.join(', ') }}
                    </p>

                    <!-- Slot cho actions bổ sung từ parent (vd: nút "Quản lý phòng") -->
                    <slot name="actions" :item="draft" :close="tryClose" />

                    <BaseButton v-if="showSave" variant="primary" size="md" rounded="lg" class="w-full"
                        :class="!canSave && 'opacity-50 cursor-not-allowed pointer-events-none'" :disabled="!canSave"
                        @click="onSaveClick">
                        Lưu thay đổi
                    </BaseButton>
                </div>
            </div>
        </Transition>
    </Teleport>

    <!-- Confirm save dialog -->
    <ConfirmDialog v-model="showConfirm" title="Xác nhận cập nhật"
        description="Bạn có chắc muốn lưu các thay đổi này không?" confirmLabel="Lưu" @confirm="doSave" />
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { X } from 'lucide-vue-next'
import BaseButton from '@/components/ui/button/BaseButton.vue'
import FieldRenderer from '@/components/common/table/subcomponents/FieldRenderer.vue'
import ConfirmDialog from '@/components/common/table/subcomponents/ConfirmDialog.vue'
import type { ColumnDef, RowItem } from '@/components/common/table/types/table'
import { isReadonlyInEdit }
    from '@/components/common/table/utils/column'

const props = defineProps<{
    item: RowItem | null
    columns: ColumnDef[]
    errors?: Record<string, string>
    showSave?: boolean
}>()

const emit = defineEmits<{
    close: []
    save: [item: RowItem]
}>()

// ── Draft state ───────────────────────────────────────────────────────────────
const draft = ref<Record<string, unknown>>({})

watch(
    () => props.item,
    (val) => {
        if (val) {
            draft.value = { ...val }
            localErrors.value = {}
        }
    },
    { immediate: true },
)

// ── Columns hiển thị trong Detail Panel ──────────────────────────────────────
//
// Logic: hiển thị TẤT CẢ các column trừ những field vừa readonly vừa hideInTable.
// - readonly=true + hideInTable=true  → thuần system field (id nội bộ, computed) → ẩn
// - readonly=true + hideInTable=false → field đọc được (status, cinemaId) → hiện (dạng readonly)
// - readonly=false + hideInTable=true → field chỉ dùng khi create (roomLayoutId) → hiện (dạng readonly vì readonlyInEdit)
// - readonly=false + hideInTable=false → field bình thường → hiện và editable
//
// NOTE: Khác với filter cũ (!c.readonly || c.hideInTable !== true) — logic cũ vô tình
// ẩn mất các relation field có readonly=false + hideInTable=true (như roomLayoutId).
const editableColumns = computed(() =>
    props.columns.filter((c) => !(c.readonly && c.hideInTable))
)

// ── depValuesMap — giống CreateModal, truyền deps cho dependent fields ─────────
//
// Trong Detail Panel, các dependent field thường là readonlyInEdit=true
// (roomLayoutId không cho sửa sau khi tạo), nên depValues chủ yếu dùng để
// FieldRenderer resolve label hiển thị readonly — không trigger re-fetch.
//
// Tuy nhiên nếu sau này có field editable với dependentLoader trong panel,
// cơ chế này hoạt động đúng ngay mà không cần thay đổi.
const depValuesMap = computed<Record<string, Record<string, unknown>>>(() => {
    const result: Record<string, Record<string, unknown>> = {}
    for (const col of props.columns) {
        if (col.dependsOn?.length) {
            result[col.key] = Object.fromEntries(
                col.dependsOn.map((dep) => [dep, draft.value[dep]])
            )
        }
    }
    return result
})

// ── Local errors ──────────────────────────────────────────────────────────────
const localErrors = ref<Record<string, string>>({})

watch(() => props.errors, (val) => {
    localErrors.value = { ...val }
}, { deep: true })

function onFieldUpdate(key: string, value: unknown) {
    draft.value[key] = value
    delete localErrors.value[key]
}

// ── Dirty check ───────────────────────────────────────────────────────────────
const isDirty = computed(() => {
    if (!props.item) return false
    return props.columns.some((c) => draft.value[c.key] !== props.item![c.key])
})

// ── Required validation (bỏ qua readonlyInEdit — user không thể sửa) ─────────
const emptyFields = computed(() =>
    props.columns
        .filter((c) => {
            if (c.readonly || isReadonlyInEdit(c, draft.value) || c.required === false) return false
            const val = draft.value[c.key]
            if (c.type === 'multiselect') {
                return !Array.isArray(val) || val.length === 0
            }
            return val === '' || val == null
        })
        .map((c) => c.label),
)

const canSave = computed(() => isDirty.value && emptyFields.value.length === 0)

// ── Confirm dialog ────────────────────────────────────────────────────────────
const showConfirm = ref(false)

function onSaveClick() {
    if (!canSave.value) return
    showConfirm.value = true
}

function doSave() {
    emit('save', draft.value as RowItem)
}

// ── Close ─────────────────────────────────────────────────────────────────────
function tryClose() {
    emit('close')
}
</script>