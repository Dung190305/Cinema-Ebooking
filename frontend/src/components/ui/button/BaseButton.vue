<script setup lang="ts">
import { computed } from 'vue'

type Variant = 'primary' | 'secondary' | 'ghost'
type Size = 'sm' | 'md' | 'lg' | '2xl'
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

/* ================= CONFIG ================= */

const base =
  'inline-flex items-center justify-center transition focus:outline-none'

const variants: Record<Variant, string> = {
  primary: 'bg-accent text-text-on-accent hover:brightness-110',
  secondary: 'border border-accent text-text-secondary hover:bg-accent hover:text-text-on-accent',
  ghost: 'text-text-secondary hover:text-text-primary'
}

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

/* ============ COMPOUND VARIANTS ============ */

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

    // ✅ CHỈ thêm bg-transparent khi variant KHÔNG phải ghost
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

const variantClass = computed(() => {
  const v = props.variant ?? 'primary'

  // base class từ config
  let cls = variants[v]

  if (props.isAdmin) {
    if (v === 'secondary' || v === 'ghost') {
      cls = cls.replace('text-text-primary', 'text-text-admin-primary')
    }

    if (v === 'ghost') {
      cls = cls.replace('text-text-secondary', 'text-text-admin-secondary')
    }
  }

  return cls
})
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