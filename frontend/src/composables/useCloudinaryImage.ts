interface ImageTransformOptions {
  width?: number
  height?: number
  crop?: 'fill' | 'fit' | 'scale' | 'pad'
  gravity?: 'auto' | 'face' | 'auto:face' | 'auto:subject' | 'center'
  quality?: string
  format?: string
  background?: string
}

export function useCloudinaryImage() {
  function getTransformedUrl(
    originalUrl: string | undefined | null,
    options: ImageTransformOptions = {}
  ): string {
    if (!originalUrl) return ''

    const {
      width = 600,
      height,
      crop = 'fill',
      gravity = 'auto',
      quality = 'auto:good',
      format = 'auto',
      background,
    } = options

    let transforms = `c_${crop},w_${width}`
    if (height) transforms += `,h_${height}`
    transforms += `,g_${gravity},q_${quality},f_${format}`
    if (background) transforms += `,b_${background}`

    return originalUrl.replace('/upload/', `/upload/${transforms}/`)
  }

  return { getTransformedUrl }
}