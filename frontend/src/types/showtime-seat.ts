export type showtimeSeatStatus = 'AVAILABLE' | 'BOOKED' | 'LOCKED';

export interface ShowtimeSeatResponse {
    id: number,
    roomLayoutSeatId: number,
    seatNumber: string,
    rowIndex: number,
    colIndex: number,
    seatTypeId: number,
    isActive: boolean,
    status: showtimeSeatStatus,
    price: number,
    coupleGroupId: number | null,
}

export interface ShowtimeSeatLayoutResponse {
    showtimeId: number,
    rows: (ShowtimeSeatResponse | null)[][],
    totalRows: number,
    totalCols: number,
}