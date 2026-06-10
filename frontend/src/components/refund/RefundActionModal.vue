<template>
  <Transition name="modal-fade">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="handleCancel"
    >
      <div
        class="w-full max-w-md rounded-2xl bg-white shadow-2xl"
        role="dialog"
        :aria-labelledby="`modal-title-${action}`"
      >
        <!-- Header -->
        <div class="flex items-center gap-3 border-b border-slate-100 px-6 py-4">
          <div
            class="flex size-10 shrink-0 items-center justify-center rounded-full"
            :class="iconBgClass"
          >
            <component :is="iconComponent" class="size-5" :class="iconTextClass" />
          </div>
          <div>
            <h2 :id="`modal-title-${action}`" class="text-base font-semibold text-slate-900">
              {{ title }}
            </h2>
            <p class="mt-0.5 text-xs text-slate-500">
              {{ subtitle }}
            </p>
          </div>
          <button
            class="ml-auto rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600"
            @click="handleCancel"
          >
            <X class="size-4" />
          </button>
        </div>

        <!-- Info banner -->
        <div class="mx-6 mt-4 rounded-xl bg-slate-50 p-4">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <p class="text-xs font-medium text-slate-500">Người yêu cầu</p>
              <p class="mt-0.5 text-sm font-semibold text-slate-800">{{ refund?.bookingCode || '—' }}</p>
            </div>
            <div>
              <p class="text-xs font-medium text-slate-500">Mã đặt vé</p>
              <p class="mt-0.5 text-sm font-medium text-slate-800">{{ refund?.bookingCode || '—' }}</p>
            </div>
            <div>
              <p class="text-xs font-medium text-slate-500">Tiền gốc</p>
              <p class="mt-0.5 text-sm font-medium text-slate-700">
                {{ formatCurrency(Number(refund?.originalAmount || 0)) }}
              </p>
            </div>
            <div>
              <p class="text-xs font-medium text-slate-500">Tiền hoàn</p>
              <p class="mt-0.5 text-sm font-semibold" :class="amountClass">
                {{ formatCurrency(Number(refund?.refundAmount || 0)) }}
              </p>
            </div>
          </div>
          <div v-if="refund?.reason" class="mt-3 border-t border-slate-200/80 pt-3">
            <p class="text-xs font-medium text-slate-500">Lý do người dùng</p>
            <p class="mt-0.5 text-sm text-slate-700">{{ refund.reason }}</p>
          </div>
        </div>

        <!-- Warning for complete action -->
        <div
          v-if="action === 'complete'"
          class="mx-6 mt-3 flex items-start gap-2.5 rounded-xl border border-blue-100 bg-blue-50 p-3"
        >
          <AlertTriangle class="mt-0.5 size-4 shrink-0 text-blue-500" />
          <p class="text-xs text-blue-700">
            Sau khi hoàn tất, ghế sẽ được giải phóng và booking chuyển sang
            <strong>CANCELLED</strong>. Hành động này không thể hoàn tác.
          </p>
        </div>

        <!-- Admin note -->
        <div class="px-6 py-4">
          <label class="mb-1.5 block text-sm font-medium text-slate-700">
            Ghi chú của admin
            <span class="ml-1 font-normal text-slate-400">(tùy chọn)</span>
          </label>
          <textarea
            v-model="adminNote"
            rows="3"
            :placeholder="placeholder"
            class="w-full resize-none rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-800 outline-none transition placeholder:text-slate-400 focus:border-accent focus:ring-2 focus:ring-slate-100"
          />
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3 border-t border-slate-100 px-6 py-4">
          <button
            class="rounded-lg border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
            :disabled="isLoading"
            @click="handleCancel"
          >
            Hủy
          </button>
          <button
            class="rounded-lg px-4 py-2 text-sm font-medium text-white transition disabled:cursor-not-allowed disabled:opacity-60"
            :class="buttonClass"
            :disabled="isLoading"
            @click="handleConfirm"
          >
            <span v-if="isLoading" class="inline-flex items-center gap-1.5">
              <Loader2 class="size-3.5 animate-spin" />
              Đang xử lý...
            </span>
            <span v-else>{{ confirmLabel }}</span>
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { AlertTriangle, CheckCircle2, Loader2, ShieldX, UndoDot, X } from 'lucide-vue-next'
import type { RefundResponse } from '@/types/refund.types'

type Action = 'approve' | 'reject' | 'complete'

const props = defineProps<{
  modelValue: boolean
  action: Action
  refund: RefundResponse | null
  isLoading: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [adminNote: string | null]
}>()

const adminNote = ref('')

// Reset note when modal opens or action changes
watch(
  () => [props.modelValue, props.action],
  () => {
    if (props.modelValue) {
      adminNote.value = getDefaultNote()
    }
  },
)

function getDefaultNote() {
  switch (props.action) {
    case 'approve': return 'Yêu cầu hợp lệ, đồng ý hoàn tiền.'
    case 'reject': return 'Yêu cầu không hợp lệ.'
    case 'complete': return 'Đã hoàn tất xử lý hoàn tiền.'
  }
}

function handleCancel() {
  emit('update:modelValue', false)
}

function handleConfirm() {
  emit('confirm', adminNote.value || null)
  // Close after emit — parent will reset isLoading via props
}

const title = computed(() => {
  switch (props.action) {
    case 'approve': return 'Duyệt yêu cầu hoàn tiền'
    case 'reject': return 'Từ chối yêu cầu hoàn tiền'
    case 'complete': return 'Hoàn tất hoàn tiền'
  }
})

const subtitle = computed(() => {
  const code = props.refund?.bookingCode || 'đơn này'
  switch (props.action) {
    case 'approve': return `Xác nhận duyệt refund cho đơn ${code}`
    case 'reject': return `Từ chối refund của đơn ${code}`
    case 'complete': return `Hoàn tất refund cho đơn ${code}`
  }
})

const confirmLabel = computed(() => {
  switch (props.action) {
    case 'approve': return 'Xác nhận duyệt'
    case 'reject': return 'Xác nhận từ chối'
    case 'complete': return 'Hoàn tất refund'
  }
})

const placeholder = computed(() => {
  switch (props.action) {
    case 'approve': return 'Ví dụ: Đã xác minh thông tin vé, đồng ý hoàn tiền...'
    case 'reject': return 'Ví dụ: Không tìm thấy vé, yêu cầu không hợp lệ...'
    case 'complete': return 'Ví dụ: Đã chuyển khoản thành công, booking đã hủy...'
  }
})

const iconBgClass = computed(() => {
  switch (props.action) {
    case 'approve': return 'bg-emerald-100'
    case 'reject': return 'bg-red-100'
    case 'complete': return 'bg-blue-100'
  }
})

const iconTextClass = computed(() => {
  switch (props.action) {
    case 'approve': return 'text-emerald-600'
    case 'reject': return 'text-red-600'
    case 'complete': return 'text-blue-600'
  }
})

const buttonClass = computed(() => {
  switch (props.action) {
    case 'approve': return 'bg-emerald-600 hover:bg-emerald-700'
    case 'reject': return 'bg-red-600 hover:bg-red-700'
    case 'complete': return 'bg-blue-600 hover:bg-blue-700'
  }
})

const amountClass = computed(() => {
  switch (props.action) {
    case 'approve': return 'text-emerald-700'
    case 'reject': return 'text-red-700'
    case 'complete': return 'text-blue-700'
  }
})

const iconComponent = computed(() => {
  switch (props.action) {
    case 'approve': return CheckCircle2
    case 'reject': return ShieldX
    case 'complete': return UndoDot
  }
})

function formatCurrency(value: number) {
  return `${new Intl.NumberFormat('vi-VN').format(value)} ₫`
}
</script>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
.modal-fade-enter-active > div,
.modal-fade-leave-active > div {
  transition: transform 0.2s ease;
}
.modal-fade-enter-from > div,
.modal-fade-leave-to > div {
  transform: scale(0.95);
}
</style>
