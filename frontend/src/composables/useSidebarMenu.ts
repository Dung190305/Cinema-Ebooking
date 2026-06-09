import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Component } from 'vue'

// Shape sau khi composable xử lý xong
export interface SidebarChild {
  key: string
  label: string
  to: string
}

export interface SidebarMenuEntry {
  key: string
  label: string
  icon?: Component
  to?: string        // chỉ có nếu là direct link
  children?: SidebarChild[] // chỉ có nếu là group
}

export function useSidebarMenu(adminPath = '/admin') {
  const router = useRouter()

  const menu = computed<SidebarMenuEntry[]>(() => {
    // 1. Tìm route /admin trong config
    const adminRoute = router.options.routes.find((r) => r.path === adminPath)
    if (!adminRoute?.children) return []

    // 2. Chỉ lấy những route có meta.sidebar VÀ không bị hidden
    return adminRoute.children
      .filter((route) => route.meta?.sidebar && !route.meta?.hidden)
      .map((route) => {
        const { label, icon } = route.meta!.sidebar as { label: string; icon?: Component }

        // 3. Sub-routes có meta.sidebar → đây là group
        const sidebarChildren = route.children?.filter((c) => c.meta?.sidebar && !c.meta?.hidden)

        if (sidebarChildren?.length) {
          // Chỉ 1 child có sidebar meta → unwrap thành direct link
          // Tránh render SidebarGroup với duy nhất 1 item không có ý nghĩa
          if (sidebarChildren.length === 1) {
            return {
              key: route.path,
              label,
              icon,
              to: router.resolve({ name: sidebarChildren[0].name }).path,
            }
          }

          // Nhiều hơn 1 → group bình thường
          return {
            key: route.path,
            label,
            icon,
            children: sidebarChildren.map((child) => ({
              key: String(child.name ?? child.path),
              label: (child.meta!.sidebar as { label: string }).label,
              to: router.resolve({ name: child.name }).path,
            })),
          }
        }

        // 4. Không có sub-routes → direct link
        return {
          key: route.path,
          label,
          icon,
          to: router.resolve({ name: route.name }).path,
        }
      })
  })

  return menu
}