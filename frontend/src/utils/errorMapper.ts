import type { ApiError, ApiErrorDetail } from '@/types/api.types'

// ─── Category map ──────────────────────────────────────────────
type MessageFn = (params?: Record<string, any>) => string

const CATEGORY_MESSAGES: Record<string, MessageFn> = {
    REQUIRED:             ()  => 'Không được để trống',
    INVALID_FORMAT:       ()  => 'Định dạng không hợp lệ',
    INVALID_VALUE:        ()  => 'Giá trị không hợp lệ',
    DUPLICATE:            ()  => 'Giá trị này đã được sử dụng',
    INVALID_LENGTH_MIN:   (p) => `Tối thiểu ${p?.min} ký tự`,
    INVALID_LENGTH_MAX:   (p) => `Tối đa ${p?.max} ký tự`,
    INVALID_LENGTH_RANGE: (p) => `Từ ${p?.min} đến ${p?.max} ký tự`,
    INVALID_AGE_MIN:      (p) => `Phải đủ ${p?.min} tuổi`,
}

// ─── Error code map ────────────────────────────────────────────
const ERROR_CODE_MESSAGES: Record<number, MessageFn> = {
    1001: () => 'Dữ liệu không hợp lệ',
    2002: () => 'Email hoặc mật khẩu không đúng',

    3006: (p) => `Không thể xóa rạp ${p?.cinemaName} vì vẫn còn phòng chưa bị xóa`,
    3009: (p) => `Không thể xoá phòng ${p?.roomName} vì vẫn còn suất chiếu đã lên lịch hoặc đang diễn ra`,

    // ========== SHOWTIME (3021–3028) ==========
    3021: (p) => `Không tìm thấy suất chiếu${p?.showtimeId ? ` #${p.showtimeId}` : ''}`,
    3022: () => 'Trạng thái suất chiếu không hợp lệ',
    3023: () => 'Suất chiếu đã kết thúc',
    3024: () => 'Suất chiếu không thể đặt vé',
    3025: () => 'Không tìm thấy định dạng suất chiếu',
    3026: () => 'Định dạng suất chiếu không hợp lệ',

    // ========== SHOWTIME SEAT (3029–3031) ==========
    3029: () => 'Không tìm thấy ghế trong suất chiếu',
    3030: () => 'Ghế trong suất chiếu đã bị khóa',
    3031: () => 'Ghế trong suất chiếu không khả dụng',
}

// ─── Field + Category override ─────────────────────────────────
// Khi cần message riêng cho field cụ thể, thêm vào đây
// key: `${field}.${category}`

const FIELD_CATEGORY_MESSAGES: Record<string, MessageFn> = {
  'address.DUPLICATE': () => 'Địa chỉ trong thành phố này đã được sử dụng',
}

// ─── Config: Field nào sẽ ưu tiên dùng reason từ backend ─────────
const USE_REASON_FOR_FIELD: Record<string, boolean | string[]> = {
  'endTime': true,                // luôn dùng reason cho endTime
  'startTime': true,           // luôn dùng reason cho startTime    
};

function shouldUseReason(field: string, category: string): boolean {
  const config = USE_REASON_FOR_FIELD[field];
  if (config === true) return true;
  if (Array.isArray(config)) return config.includes(category);
  return false;
}

// ─── Resolve message cho 1 detail ─────────────────────────────
function resolveDetailMessage(detail: ApiErrorDetail): string {
  // 1. Ưu tiên đặc biệt: nếu field được cấu hình dùng reason
  if (shouldUseReason(detail.field, detail.category)) {
    return detail.reason;
  }
  // 2. Ưu tiên field-specific override
  const fieldKey = `${detail.field}.${detail.category}`
  const fieldOverride = FIELD_CATEGORY_MESSAGES[fieldKey]
  if (fieldOverride) return fieldOverride(detail.params ?? undefined)

  // 3. Fallback về category map
  const messageFn = CATEGORY_MESSAGES[detail.category]
  if (messageFn) return messageFn(detail.params ?? undefined)

  // 4. Fallback về reason từ backend
  return detail.reason
}

function resolveGlobalMessage(error: ApiError): string {
  const messageFn = error.code ? ERROR_CODE_MESSAGES[error.code] : null

  return messageFn
    ? messageFn(error.params ?? undefined)
    : error.message
}

// ─── Main export ───────────────────────────────────────────────
export function mapFieldErrors(error: ApiError | null) {
    const fieldErrors: Record<string, string> = {}
    const globalErrors: string[] = []

    if (!error) return { fieldErrors, globalErrors }
    console.log(error)
    if (Array.isArray(error.details) && error.details.length > 0) {
        for (const detail of error.details) {
            if (detail.field && !(detail.field in fieldErrors)) {
                // Lấy lỗi đầu tiên mỗi field, BE đã sort theo priority
                fieldErrors[detail.field] = resolveDetailMessage(detail)
            } else if (!detail.field) {
                globalErrors.push(resolveDetailMessage(detail))
            }
        }
    } else {
        globalErrors.push(resolveGlobalMessage(error))
    }

    return { fieldErrors, globalErrors }
}

export function extractOrphanError(
  error: unknown
): { orphanCount: number; orphanSeats: string[]; message: string } | null {
  const apiError = (error as any)?.raw as ApiError | undefined
  if (!apiError) return null
 
  const detail = apiError.details?.find(
    (d: ApiErrorDetail) => d.field === 'seats' && d.category === 'INVALID_VALUE'
  )
  if (!detail) return null
 
  const params = detail.params as Record<string, any> | undefined
  const orphanCount: number = params?.orphanCount ?? 0
  const orphanSeats: string[] = params?.orphanSeats ?? []
 
  if (orphanCount === 0) return null
 
  return {
    orphanCount,
    orphanSeats,
    message: detail.reason,
  }
}
 