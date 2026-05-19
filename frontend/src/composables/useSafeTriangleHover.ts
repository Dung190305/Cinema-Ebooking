// useSafeTriangleHover.ts
import { ref } from 'vue'

interface Point {
  x: number
  y: number
}

export function useSafeTriangleHover(hideDelay = 450) {
  const showDropdown = ref(false)
  let dropdownElement: HTMLElement | null = null
  let triggerElement: HTMLElement | null = null

  let hideTimer: ReturnType<typeof setTimeout> | null = null
  let globalMouseMoveListener: ((e: MouseEvent) => void) | null = null

  // ----- Helpers -----
  function isPointInTriangle(p: Point, a: Point, b: Point, c: Point): boolean {
    const area = (b.y - c.y) * (a.x - c.x) + (c.x - b.x) * (a.y - c.y)
    if (Math.abs(area) < 0.0001) return false
    const s = ((b.y - c.y) * (p.x - c.x) + (c.x - b.x) * (p.y - c.y)) / area
    const t = ((c.y - a.y) * (p.x - c.x) + (a.x - c.x) * (p.y - c.y)) / area
    return s >= 0 && t >= 0 && s + t <= 1
  }

  function getDropdownRect(): DOMRect | null {
    if (
      dropdownElement &&
      typeof dropdownElement.getBoundingClientRect === 'function'
    ) {
      return dropdownElement.getBoundingClientRect()
    }
    // Fallback: dự đoán dựa trên trigger
    if (!triggerElement) return null
    const triggerRect = triggerElement.getBoundingClientRect()
    return {
      left: triggerRect.right - 224,   // w-56 = 224px
      right: triggerRect.right,
      top: triggerRect.bottom + 8,     // mt-2 = 8px
      bottom: triggerRect.bottom + 8 + 200,
      width: 224,
      height: 200,
    } as DOMRect
  }

  function getTriangleVertices(): { top: Point; left: Point; right: Point } | null {
    if (!triggerElement) return null
    const triggerRect = triggerElement.getBoundingClientRect()
    const dropdownRect = getDropdownRect()
    if (!dropdownRect) return null

    const top: Point = {
      x: triggerRect.left + triggerRect.width / 2,
      y: triggerRect.bottom,
    }
    const left: Point = { x: dropdownRect.left, y: dropdownRect.top }
    const right: Point = { x: dropdownRect.right, y: dropdownRect.top }
    return { top, left, right }
  }

  function isMouseInSafeZone(e: MouseEvent): boolean {
    const mouse = { x: e.clientX, y: e.clientY }
    const vertices = getTriangleVertices()
    if (!vertices) return false
    return isPointInTriangle(mouse, vertices.top, vertices.left, vertices.right)
  }

  function onGlobalMouseMove(e: MouseEvent) {
    if (!showDropdown.value || !hideTimer) return
    if (isMouseInSafeZone(e)) {
      if (hideTimer) {
        clearTimeout(hideTimer)
        hideTimer = null
      }
      if (globalMouseMoveListener) {
        document.removeEventListener('mousemove', globalMouseMoveListener)
        globalMouseMoveListener = null
      }
    }
  }

  // ----- Handlers -----
  function handleTriggerEnter() {
    if (hideTimer) {
      clearTimeout(hideTimer)
      hideTimer = null
    }
    if (globalMouseMoveListener) {
      document.removeEventListener('mousemove', globalMouseMoveListener)
      globalMouseMoveListener = null
    }
    showDropdown.value = true
  }

  function handleTriggerLeave(e: MouseEvent) {
    if (isMouseInSafeZone(e)) return

    if (!globalMouseMoveListener) {
      globalMouseMoveListener = onGlobalMouseMove
      document.addEventListener('mousemove', globalMouseMoveListener)
    }

    hideTimer = setTimeout(() => {
      showDropdown.value = false
      hideTimer = null
      if (globalMouseMoveListener) {
        document.removeEventListener('mousemove', globalMouseMoveListener)
        globalMouseMoveListener = null
      }
    }, hideDelay)
  }

  function handleDropdownEnter() {
    if (hideTimer) {
      clearTimeout(hideTimer)
      hideTimer = null
    }
    if (globalMouseMoveListener) {
      document.removeEventListener('mousemove', globalMouseMoveListener)
      globalMouseMoveListener = null
    }
  }

  function handleDropdownLeave(e: MouseEvent) {
    if (isMouseInSafeZone(e)) return

    if (globalMouseMoveListener) {
      document.removeEventListener('mousemove', globalMouseMoveListener)
      globalMouseMoveListener = null
    }
    globalMouseMoveListener = onGlobalMouseMove
    document.addEventListener('mousemove', globalMouseMoveListener)

    hideTimer = setTimeout(() => {
      showDropdown.value = false
      hideTimer = null
      if (globalMouseMoveListener) {
        document.removeEventListener('mousemove', globalMouseMoveListener)
        globalMouseMoveListener = null
      }
    }, hideDelay)
  }

  function setDropdownElement(el: HTMLElement | null) {
    if (el && !(el instanceof HTMLElement)) {
      console.warn('[useSafeTriangleHover] setDropdownElement: not an HTMLElement, ignoring.')
      dropdownElement = null
    } else {
      dropdownElement = el
    }
  }

  function setTriggerElement(el: HTMLElement | null) {
    if (el && !(el instanceof HTMLElement)) {
      console.warn('[useSafeTriangleHover] setTriggerElement: not an HTMLElement, ignoring.')
      triggerElement = null
    } else {
      triggerElement = el
    }
  }

  function cleanup() {
    if (hideTimer) clearTimeout(hideTimer)
    if (globalMouseMoveListener) {
      document.removeEventListener('mousemove', globalMouseMoveListener)
    }
  }

  return {
    showDropdown,
    handleTriggerEnter,
    handleTriggerLeave,
    handleDropdownEnter,
    handleDropdownLeave,
    setDropdownElement,
    setTriggerElement,
    cleanup,
  }
}