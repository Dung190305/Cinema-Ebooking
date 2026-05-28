import { notFound } from "next/navigation";
import PaymentClient from "@/components/PaymentClient";

const allowedMethods = ["momo", "vnpay", "zalopay"];

export default async function PaymentPage({
  params,
  searchParams,
}: {
  params: { method: string };
  searchParams: { [key: string]: string | undefined };
}) {
  const { method } = await params;
  const resolvedSearchParams = await searchParams;

  if (!allowedMethods.includes(method.toLowerCase())) {
    notFound();
  }

  const paymentCode = resolvedSearchParams.paymentCode || "";
  const amount = resolvedSearchParams.amount || "0";
  const bookingId = resolvedSearchParams.bookingId || "";
  const callbackUrl = resolvedSearchParams.callbackUrl || "";
  const showtimeId = resolvedSearchParams.showtimeId || "0";

  if (!paymentCode || !bookingId) {
    return (
      <div className="min-h-screen flex items-center justify-center text-red-600">
        Thiếu thông tin thanh toán (paymentCode, bookingId)
      </div>
    );
  }

  return (
    <PaymentClient
      method={method}
      paymentCode={paymentCode}
      amount={parseFloat(amount)}
      bookingId={bookingId}
      callbackUrl={callbackUrl}
      showtimeId={parseInt(showtimeId)}
    />
  );
}
