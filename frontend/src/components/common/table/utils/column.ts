import type {
  ColumnDef,
  RowItem,
} from '@/components/common/table/types/table'

export function isReadonlyInEdit<T = RowItem>(
  column: ColumnDef<T>,
  row: T,
): boolean {

  if (typeof column.readonlyInEdit === 'function') {
    return column.readonlyInEdit(row)
  }

  return column.readonlyInEdit ?? false
}