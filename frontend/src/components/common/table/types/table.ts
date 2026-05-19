// ─── Column definition ───────────────────────────────────────────────────────
// Mỗi column khai báo một lần, dùng lại ở cả table lẫn detail panel

export type FieldType = 'text' | 'number' | 'email' | 'enum' | 'date' | 'datetime' | 'textarea' | 'multiselect' | 'boolean' | 'currency' | 'relation'

export interface ColumnDef<T = Record<string, unknown>> {
  key: keyof T & string
  label: string
  type: FieldType
  width?: string
  required?: boolean
  readonly?: boolean
  readonlyInEdit?: ReadonlyResolver<T>
  hideInCreate?: boolean
  hideInTable?: boolean
  displayKey?: string

  options?: { value: string | number; label: string }[]

// ── Relation-specific ─────────────────────────────────────────────────────
 
  /**
   * Static options — không fetch API.
   * Dùng cho format (2D/3D/IMAX) vì không có endpoint.
   *
   * @example
   * staticOptions: [
   *   { id: 1, label: '2D'   },
   *   { id: 2, label: '3D'   },
   *   { id: 3, label: 'IMAX' },
   * ]
   */
  staticOptions?: RelationOption[]
 
  /**
   * Async loader — fetch một lần, cache theo function reference ở module level.
   * Dùng cho movie (getAll) và room (getByCinema).
   *
   * @example
   * optionsLoader: () =>
   *   movieApi.getAll().then(list =>
   *     list.map(m => ({ id: m.id, label: m.title }))
   *   )
   */
  optionsLoader?: () => Promise<RelationOption[]>
 
  /**
   * Dependent loader — re-fetch mỗi khi các field trong `dependsOn` thay đổi.
   * Dùng cho roomLayoutId (phụ thuộc roomId + startTime).
   * Nếu kết quả trả về đúng 1 option → FieldRenderer tự động chọn và emit.
   *
   * @example
   * dependsOn: ['roomId', 'startTime'],
   * dependentLoader: ({ roomId, startTime }) =>
   *   roomLayoutApi.getOptionsForShowtime(roomId as number, startTime as string)
   */
  dependsOn?:       string[]
  dependentLoader?: (depValues: Record<string, unknown>) => Promise<RelationOption[]>
 
  /**
   * Custom display resolver — dùng ở table cell và detail display mode.
   * Ưu tiên hơn auto-resolve từ loaded options.
   * Nên dùng khi page có sẵn map data (tránh fetch lại chỉ để hiển thị).
   *
   * @example
   * displayFn: (val) => FORMAT_MAP[val as number] ?? String(val)
   * displayFn: (val, row) => movieMap.get(val as number)?.title ?? '...'
   */
  displayFn?: (value: unknown, row: T) => string

  typeResolver?: (depValues: Record<string, unknown>) => FieldType

  pastOnly?: boolean

  futureOnly?: boolean

  autoSelectToday?: boolean 
  /**
   * Số ngày cộng thêm vào hiện tại để làm mốc `minDate`.
   * - Mặc định = 0 (hôm nay).
   * - Ví dụ: 1 → ngày mai, -1 → hôm qua.
   */
  minDateOffset?: number
}

// ─── Row item ─────────────────────────────────────────────────────────────────
// T là kiểu data cụ thể của từng domain (Cinema, Movie, User, ...)
// Bắt buộc có id để làm key và xử lý delete/update

export type RowItem = { id: string | number; [key: string]: unknown }

// ─── Emit events của DataTable ────────────────────────────────────────────────

export interface DataTableEmits<T extends RowItem> {
  create: []
  delete: [item: T]
  save: [item: T, done: () => void]
}