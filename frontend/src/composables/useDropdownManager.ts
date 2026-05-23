import { ref, computed, readonly } from 'vue'

type DropdownKey = string | symbol | number

const activeKey = ref<DropdownKey | null>(null)

export function useDropdownManager() {
  const register = (key: DropdownKey) => {
    // isActive là computed, reactive
    const isActive = computed(() => activeKey.value === key)

    const activate = () => {
      if (activeKey.value !== key) {
        activeKey.value = key
      }
    }

    const deactivate = () => {
      if (activeKey.value === key) {
        activeKey.value = null
      }
    }

    return { isActive, activate, deactivate }
  }

  return { register }
}