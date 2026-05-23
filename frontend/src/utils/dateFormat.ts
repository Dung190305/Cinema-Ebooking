// src/utils/dateFormat.ts

const VIETNAM_TZ = 'Asia/Ho_Chi_Minh';

/**
 * Convert Date (local VN) → UTC ISO string (dùng để gửi lên Backend)
 */
export function toUTCInstant(date: Date): string {
    if (!date || isNaN(date.getTime())) return '';
    return date.toISOString(); // Luôn ra UTC + Z
}

/**
 * Parse UTC ISO string → Date object (dùng để hiển thị/edit)
 */
export function fromUTCToLocal(utcString: string): Date | null {
    if (!utcString) return null;
    const date = new Date(utcString);
    return isNaN(date.getTime()) ? null : date;
}

/**
 * Format ngày giờ đầy đủ theo múi giờ Việt Nam (dùng cho hiển thị bảng, readonly)
 */
export function formatDateTimeVN(date: Date | string): string {
    if (!date) return '—';
    
    const d = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(d.getTime())) return '—';

    return d.toLocaleString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        timeZone: VIETNAM_TZ
    });
}

export function formatDateHeaderVN(dateStr: string): string {
  const date = new Date(dateStr);
  const weekdays = ['Chủ nhật', 'Thứ hai', 'Thứ ba', 'Thứ tư', 'Thứ năm', 'Thứ sáu', 'Thứ bảy'];
  const weekday = weekdays[date.getDay()];
  const day = date.getDate().toString().padStart(2, '0');
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const year = date.getFullYear();
  return `${weekday}, ${day}/${month}/${year}`;
}

export function getDateKeyVN(instant: string): string {
  return new Date(instant).toLocaleDateString('sv-SE', { timeZone: VIETNAM_TZ }); // YYYY-MM-DD
}

/**
 * Format chỉ ngày theo múi giờ Việt Nam
 */
export function formatDateVN(date: Date | string): string {
    if (!date) return '—';
    
    const d = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(d.getTime())) return '—';

    return d.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        timeZone: VIETNAM_TZ
    });
}

/**
 * Format chỉ giờ (HH:mm) theo múi giờ Việt Nam
 */
export function formatTimeVN(date: Date | string): string {
    if (!date) return '';
    
    const d = typeof date === 'string' ? new Date(date) : date;
    if (isNaN(d.getTime())) return '';

    return d.toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
        timeZone: VIETNAM_TZ
    });
}

// ====================== GIỮ LẠI CÁC HÀM CŨ ĐỂ TƯƠNG THÍCH ======================

export function dateToISOString(date: Date, includeTime: boolean = false): string {
    if (!date || isNaN(date.getTime())) return '';

    if (!includeTime) {
        // Trả về YYYY-MM-DD theo local date (không bị lệch)
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    return date.toISOString(); // giữ nguyên cho datetime
}

export function parseISODate(value: string): Date | null {
    if (!value) return null;
    
    // Date-only
    if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
        const [year, month, day] = value.split('-').map(Number);
        const d = new Date(year, month - 1, day);
        return isNaN(d.getTime()) ? null : d;
    }
    
    // Datetime
    const d = new Date(value);
    return isNaN(d.getTime()) ? null : d;
}

export function createLocalDate(year: number, month: number, day: number, hour = 0, minute = 0): Date {
    const date = new Date(year, month, day, hour, minute, 0, 0);
    
    // Bảo vệ nghiêm ngặt chống lệch ngày do timezone
    if (date.getFullYear() !== year || date.getMonth() !== month || date.getDate() !== day) {
        console.warn(`[createLocalDate] Fallback UTC: ${year}-${month+1}-${day}`);
        return new Date(Date.UTC(year, month, day, hour, minute));
    }
    return date;
}

export function parseDateSafe(value: Date | string | null): Date | null {
    if (!value) return null;
    if (value instanceof Date) {
        // Clone để tránh mutation
        return new Date(value.getTime());
    }

    const str = String(value).trim();

    // YYYY-MM-DD
    if (/^\d{4}-\d{2}-\d{2}$/.test(str)) {
        const [year, month, day] = str.split('-').map(Number);
        return createLocalDate(year, month - 1, day);
    }

    // DD/MM/YYYY HH:mm hoặc DD/MM/YYYY
    if (/^\d{1,2}\/\d{1,2}\/\d{4}/.test(str)) {
        const parts = str.split(/[/\s:]+/).map(Number);
        return createLocalDate(parts[2], parts[1] - 1, parts[0], parts[3] || 0, parts[4] || 0);
    }

    const d = new Date(str);
    return isNaN(d.getTime()) ? null : d;
}