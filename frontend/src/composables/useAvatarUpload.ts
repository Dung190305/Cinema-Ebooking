import { ref } from 'vue'
import { userApi } from '@/api/user.api'
import type { UserProfile } from '@/types/auth.types'

declare const cloudinary: any

interface CloudinaryConfig {
  cloudName: string
  uploadPreset: string
  folder?: string
}

export function useAvatarUpload(config: CloudinaryConfig) {
  const uploading = ref(false)
  const error = ref<string | null>(null)

  const openWidget = (onWidgetReady?: () => void): Promise<UserProfile | null> => {
    return new Promise((resolve) => {
      let readyCalled = false
      let timeoutId: ReturnType<typeof setTimeout> | null = null

      const widget = cloudinary.createUploadWidget(
        {
          cloudName: config.cloudName,
          uploadPreset: config.uploadPreset,
          sources: ['local', 'url', 'camera'],
          multiple: false,
          maxFiles: 1,
          clientAllowedFormats: ['png', 'jpeg', 'jpg', 'webp'],
          maxFileSize: 5_000_000,
          folder: config.folder ?? 'avatars',
          cropping: true,
          croppingAspectRatio: 1,
          showSkipCropButton: false,
        },
        async (cbError: any, result: any) => {  
          if (cbError) {
            error.value = cbError.message || 'Upload thất bại'
            uploading.value = false
            return
          }

          if (result?.event === 'show' && !readyCalled) {
            readyCalled = true
            if (timeoutId) clearTimeout(timeoutId)
            onWidgetReady?.()   // Ẩn loading overlay
          }

          if (result?.event === 'success') {
            uploading.value = true
            try {
              const res = await userApi.updateAvatar({ avatarUrl: result.info.secure_url })
              resolve(res)
            } catch (err: any) {
              error.value = err?.message || 'Không thể cập nhật avatar'
              resolve(null)
            } finally {
              uploading.value = false
            }
          }

          if (result?.event === 'close') {
            resolve(null)
          }
        }
      )
        
      widget.open()
      
      timeoutId = setTimeout(() => {
        if (!readyCalled) {
          readyCalled = true
          onWidgetReady?.()
        }
      }, 3000)
    })

  }


  return { uploading, error, openWidget }
}