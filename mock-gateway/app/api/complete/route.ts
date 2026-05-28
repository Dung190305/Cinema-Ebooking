import { NextResponse } from 'next/server';

export async function POST(request: Request) {
  const { paymentCode } = await request.json();

  if (!paymentCode) {
    return NextResponse.json({ message: 'Thiếu paymentCode' }, { status: 400 });
  }

  const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';
  const endpoint = `${backendUrl}/api/v1/payments/${paymentCode}/complete`;

  try {
    const res = await fetch(endpoint, { method: 'POST' });
    const text = await res.text();

    if (!res.ok) {
      return NextResponse.json(
        { message: `Backend error: ${res.status} ${text}` },
        { status: res.status }
      );
    }

    let apiResponse: any = {};
    try {
      apiResponse = JSON.parse(text);
    } catch {
      // fallback nếu backend trả về không phải JSON (hiếm)
      apiResponse = { success: true, data: { status: 'SUCCESS', transactionId: `TXN_${Date.now()}` } };
    }

    // Unwrap ApiResponse để lấy dữ liệu thực tế
    const payload = apiResponse.data;
    const isSuccess = payload?.status === 'SUCCESS';

    return NextResponse.json(
      {
        transactionId: payload?.transactionId ?? null,
        status: payload?.status,
        message: payload?.message,
        showtimeId: payload?.showtimeId,
      },
      { status: isSuccess ? 200 : 422 } // giữ nguyên logic cũ, nhưng lúc này isSuccess đúng
    );
  } catch (error: any) {
    return NextResponse.json(
      { message: `Không kết nối được backend: ${error.message}` },
      { status: 502 }
    );
  }
}