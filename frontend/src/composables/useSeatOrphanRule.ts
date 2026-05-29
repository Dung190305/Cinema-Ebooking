import type { SeatResponse, RoomLayoutResponse } from '@/types/seat'

export interface OrphanCheckResult {
  hasOrphan: boolean
  orphanCount: number
  message: string
}

export function useSeatOrphanRule() {

  function findOrphanIds(
    grid: RoomLayoutResponse,
    takenIds: Set<number>,
  ): Set<number> {
    const result = new Set<number>()
    for (const row of grid.rows) {
      if (!row) continue
      for (const id of findOrphansInRow(row, takenIds)) {
        result.add(id)
      }
    }
    return result
  }

  function checkOrphan(
    grid: RoomLayoutResponse,
    bookedIds: Set<number>,
    lockedIds: Set<number>,
    proposedSelectedIds: Set<number>,
  ): OrphanCheckResult {
    const baseTaken = new Set([...bookedIds, ...lockedIds])

    // Orphan tồn tại trước khi user chọn bất kỳ ghế nào
    const orphansBefore = findOrphanIds(grid, baseTaken)

    // Orphan sau khi áp dụng selection
    const takenAfter = new Set([...baseTaken, ...proposedSelectedIds])
    const orphansAfter = findOrphanIds(grid, takenAfter)

    // Chỉ tính orphan MỚI do selection này tạo ra
    const newOrphanIds = [...orphansAfter].filter(id => !orphansBefore.has(id))
    const orphanCount = newOrphanIds.length

    return {
      hasOrphan: orphanCount > 0,
      orphanCount,
      message: orphanCount > 0
        ? `Lựa chọn hiện tại tạo ra ${orphanCount} ghế đơn lẻ. Hãy chọn ghế liền kề hoặc bỏ chọn.`
        : '',
    }
  }
  function findOrphansInRow(
    row: (SeatResponse | null)[],
    takenIds: Set<number>,
  ): number[] {
    const orphanIds: number[] = []
    let singleRun: SeatResponse[] = []

    const flush = () => {
      if (singleRun.length === 1) {
        orphanIds.push(singleRun[0].id)
      }
      singleRun = []
    }

    for (let c = 0; c < row.length; c++) {
      const seat = row[c]

      if (!seat) {
        flush()                    // Gap vật lý
        continue
      }

      const isTaken = takenIds.has(seat.id)
      const isCouple = seat.seatTypeId === 3

      // Ghế đã chiếm HOẶC ghế đôi → đều là divider cho ghế đơn
      if (isTaken || isCouple) {
        flush()
        continue
      }

      // Ghế đơn + available → thêm vào run
      singleRun.push(seat)
    }

    flush() // Xử lý run cuối dòng

    return orphanIds
  }

  return { checkOrphan }
}