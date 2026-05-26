<script setup lang="ts">
import { computed } from 'vue'

type Variant = 'primary' | 'secondary' | 'ghost'
type Size = 'sm' | 'md' | 'lg' | '2xl' | '4xl'   // thêm '4xl' để khớp với config
type Rounded = 'none' | 'sm' | 'md' | 'lg' | '2xl' | 'full'

const props = defineProps<{
  variant?: Variant
  size?: Size
  rounded?: Rounded
  iconOnly?: boolean
  isAdmin?: boolean
  disabled?: boolean
  customClass?: string
}>()

/* ================= BASE ================= */
const base =
  'inline-flex items-center justify-center transition focus:outline-none'

/* ================= CONFIG CƠ BẢN ================= */
const sizes: Record<Size, string> = {
  sm: 'px-3 py-1 text-sm',
  md: 'px-4 py-2 text-md',
  lg: 'px-6 py-3 text-base',
  '2xl': 'px-8 py-4 text-lg',
  '4xl': 'px-16 py-6 text-body'
}

const roundeds: Record<Rounded, string> = {
  none: 'rounded-none',
  sm: 'rounded-sm',
  md: 'rounded-md',
  lg: 'rounded-lg',
  '2xl': 'rounded-2xl',
  full: 'rounded-full'
}

/* ================= VARIANT CLASS (ĐÃ NÂNG CẤP) ================= */
const variantClass = computed(() => {
  const v = props.variant ?? 'primary'
  let cls = ''

  // ----- PRIMARY PREMIUM -----
  if (v === 'primary') {
    cls = [
      'bg-accent text-text-on-accent',
      'shadow-sm hover:shadow-lg hover:shadow-accent/30', // bóng đổ tăng dần, màu accent
      'hover:scale-105 active:scale-95',                  // phóng to nhẹ khi hover, thu nhỏ khi click
      'transition-all duration-300 ease-out'              // mượt mà
    ].join(' ')
  }

  // ----- SECONDARY (KHÔNG GIỐNG PRIMARY KHI HOVER) -----
  else if (v === 'secondary') {
    cls = [
      'border border-accent text-text-secondary',
      'hover:text-accent hover:bg-accent/10 hover:border-accent/80', // nền accent rất nhạt, chữ và viền accent
      'hover:shadow-sm',                                            // bóng nhẹ
      'transition-all duration-300 ease-out'
    ].join(' ')
  }

  // ----- GHOST -----
  else if (v === 'ghost') {
    cls = [
      'text-text-secondary',
      'hover:text-text-primary hover:bg-gray-100/50',
      'transition-colors duration-200'
    ].join(' ')
  }

  // ----- ĐIỀU CHỈNH CHO ADMIN -----
  if (props.isAdmin) {
    if (v === 'secondary') {
      cls = cls.replace('text-text-secondary', 'text-text-admin-secondary')
      // hover:text-accent vẫn giữ vì thường admin cũng dùng màu accent
    }
    if (v === 'ghost') {
      cls = cls.replace('text-text-secondary', 'text-text-admin-secondary')
      cls = cls.replace('hover:text-text-primary', 'hover:text-text-admin-primary')
      cls = cls.replace('hover:bg-gray-100/50', 'hover:bg-overlay-light-10')
    }
    // primary không cần thay đổi màu chữ vì text-on-accent đã tương phản tốt
  }

  return cls
})

/* ============ COMPOUND VARIANTS (GIỮ NGUYÊN LOGIC ICON-ONLY) ============ */
function getCompoundClasses() {
  const classes: string[] = []

  const overlay = props.isAdmin
    ? 'hover:bg-overlay-light-10'
    : 'hover:bg-overlay-dark-30'

  if (props.iconOnly) {
    classes.push('aspect-square')

    if (props.size === 'sm') classes.push('w-8 h-8')
    if (props.size === 'md') classes.push('w-10 h-10')
    if (props.size === 'lg') classes.push('w-12 h-12')
    if (props.size === '2xl') classes.push('w-16 h-16')

    if (props.variant !== 'ghost') {
      classes.push('bg-transparent')
    }

    if (props.variant === 'primary') {
      classes.push(overlay)
    }

    if (props.variant === 'secondary') {
      classes.push(`${overlay} border-none`)
    }

    if (props.variant === 'ghost' || !props.variant) {
      classes.push(overlay)
    }
  }

  return classes
}

/* ================= FINAL CLASS ================= */
const buttonClass = computed(() => [
  base,
  variantClass.value,
  !props.iconOnly && sizes[props.size ?? 'md'],
  roundeds[props.rounded ?? 'md'],
  ...getCompoundClasses(),
  props.disabled && 'opacity-50 pointer-events-none',
  props.customClass,
])
</script>

<template>
  <button :class="buttonClass" :disabled="disabled">
    <slot />
  </button>
</template>