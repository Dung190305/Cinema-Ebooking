// src/utils/dateFormat.ts

/**
 * Chuyển Date thành chuỗi ISO dạng YYYY-MM-DD hoặc YYYY-MM-DDTHH:mm
 */
export function dateToISOString(date: Date, includeTime: boolean = false): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  const base = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
  return includeTime
    ? `${base}T${pad(date.getHours())}:${pad(date.getMinutes())}`
    : base
}

/**
 * Parse chuỗi ISO (YYYY-MM-DD hoặc YYYY-MM-DDTHH:mm) sang Date.
 * Trả về null nếu không hợp lệ.
 */
export function parseISODate(value: string): Date | null {
  const d = new Date(value)
  return isNaN(d.getTime()) ? null : d
}