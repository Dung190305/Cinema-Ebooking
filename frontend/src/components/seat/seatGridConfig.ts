// ─── Seat type style ──────────────────────────────────────────────────────────
// Key = seatTypeId từ backend. Sửa id cho đúng với data thực tế.
// Hiện tại giả định: 1 = STANDARD, 2 = VIP, 3 = COUPLE

export interface SeatTypeConfig {
  label: string

  // Admin: bg thể hiện loại ghế, hover brightness-110
  adminBg:     string   // Tailwind bg class
  adminBorder: string   // Tailwind border class
  adminText:   string   // Tailwind text class

  // Web: border thể hiện loại ghế, bg chung là dark
  // khi selected → webSelectedBg được tô
  webBorder:       string
  webSelectedBg:   string
  webSelectedText: string
}

// ── Màu từng loại ghế ─────────────────────────────────────

export const SEAT_TYPE_CONFIGS: Record<number, SeatTypeConfig> = {
  1: {
    label:           'Ghế thường',
    adminBg:         'bg-slate-200',
    adminBorder:     'border-slate-400',
    adminText:       'text-slate-700',
    webBorder:       'border-white/30',
    webSelectedBg:   'bg-amber-500',
    webSelectedText: 'text-white',
  },
  2: {
    label:           'Ghế VIP',
    adminBg:         'bg-amber-100',
    adminBorder:     'border-amber-400',
    adminText:       'text-amber-800',
    webBorder:       'border-amber-400',
    webSelectedBg:   'bg-amber-500',
    webSelectedText: 'text-white',
  },
  3: {
    label:           'Ghế đôi',
    adminBg:         'bg-violet-100',
    adminBorder:     'border-violet-400',
    adminText:       'text-violet-800',
    webBorder:       'border-violet-500',
    webSelectedBg:   'bg-violet-500',
    webSelectedText: 'text-white',
  },
}

export const SEAT_TYPE_FALLBACK: SeatTypeConfig = {
  label:           'Unknown',
  adminBg:         'bg-gray-200',
  adminBorder:     'border-gray-300',
  adminText:       'text-gray-500',
  webBorder:       'border-gray-500',
  webSelectedBg:   'bg-gray-500',
  webSelectedText: 'text-white',
}

// ─── Grid config ──────────────────────────────────────────────────────────────

export interface SeatGridConfig {
  mode: 'admin' | 'web'
  seatSize: string   
  seatRadius: string   
  screenPosition: 'top' | 'bottom'
  rtl: boolean 
  showRowLabel: boolean
  rowLabelBothSides: boolean
  showColNumber: boolean  
  gap: string  
  cellWidth: string
  cellHeight: string
  legendSize: string       
  legendSizeWide: string  

    // --- Style cho các trạng thái ghế (không phụ thuộc loại ghế) ---
  // Admin mode
  adminSelectedClass?: string   // ghế được chọn (thường dùng khi click chọn ghế trong admin)
  adminBookedClass?: string     // ghế đã đặt (booked)
  adminLockedClass?: string     // ghế bị khóa (locked)
  adminInactiveClass?: string   // ghế không hoạt động (INACTIVE)
  
  // Web mode
  webBookedClass?: string       // ghế đã đặt
  webLockedClass?: string       // ghế bị khóa
  webInactiveClass?: string     // ghế không hoạt động
  
  // Legend classes (cho mục "Đã đặt" và "Đã khóa" trong admin legend)
  legendBookedClass?: string
  legendLockedClass?: string
}

// ── Admin config ──────────────────────────────────────────────────────────────
export const adminSeatGridConfig: SeatGridConfig = {
  mode: 'admin',
  seatSize: 'h-8 w-8',
  seatRadius: 'rounded-md',
  screenPosition: 'bottom',
  rtl: true,
  showRowLabel: true,
  rowLabelBothSides: true,
  showColNumber: true,
  gap: 'gap-1.5',
  cellWidth: '2rem',
  cellHeight: '2rem',
  legendSize: 'h-5 w-5',     
  legendSizeWide: 'h-5 w-9',

  adminSelectedClass: 'bg-blue-500 border-blue-600 text-white shadow-md ring-2 ring-blue-300',
  adminInactiveClass: 'bg-slate-50 border-slate-200 text-slate-300 opacity-50',
  adminBookedClass: 'bg-gray-300 border-gray-400 text-gray-600 line-through opacity-80',
  adminLockedClass: 'bg-red-100 border-red-400 text-red-700 opacity-80',
  legendBookedClass: 'bg-gray-300 border border-gray-400 rounded-md',
  legendLockedClass: 'bg-red-100 border border-red-400 rounded-md',
}

// ── Web config ────────────────────────────────────────────────────────────────
export const webSeatGridConfig: SeatGridConfig = {
  mode: 'web',
  seatSize: 'h-8 w-8',
  seatRadius: 'rounded-md',
  screenPosition: 'bottom',
  rtl: true,
  showRowLabel: true,
  rowLabelBothSides: true,
  showColNumber: true,
  gap: 'gap-2',
  cellWidth: '2rem',
  cellHeight: '2rem',
  legendSize: 'h-5 w-5',
  legendSizeWide: 'h-5 w-9',

  webBookedClass: 'bg-gray-600/40 border-gray-500 text-gray-300 line-through pointer-events-none',   
  webLockedClass: 'bg-amber-500/20 border-amber-500/50 text-amber-300 pointer-events-none',            
  webInactiveClass: 'bg-white/5 border-white/10 text-white/20 opacity-40 pointer-events-none',
}

// ── Preview config (dùng trong form tạo/sửa suất chiếu) ───────────────────────
export const previewSeatGridConfig: SeatGridConfig = {
  mode: 'admin',
  seatSize: 'h-5 w-5',     
  seatRadius: 'rounded-sm', 
  screenPosition: 'bottom',
  rtl: true,
  showRowLabel: true,
  rowLabelBothSides: false, 
  showColNumber: true,
  gap: 'gap-2',              
  cellWidth: '1.25rem',
  cellHeight: '1.25rem',
  legendSize: 'h-4 w-4',       
  legendSizeWide: 'h-4 w-7',
    
  adminSelectedClass: 'bg-blue-400 border-blue-500 text-white ring-1 ring-blue-200',
  adminBookedClass: 'bg-gray-200 border-gray-300 text-gray-500 line-through opacity-70',
  adminLockedClass: 'bg-red-50 border-red-300 text-red-500 opacity-70',
  adminInactiveClass: 'bg-slate-50 border-slate-200 text-slate-300 opacity-40',
  legendBookedClass: 'bg-gray-200 border border-gray-300 rounded-md',
  legendLockedClass: 'bg-red-50 border border-red-300 rounded-md',

}