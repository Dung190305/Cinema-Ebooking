"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

interface PaymentClientProps {
  method: string;
  paymentCode: string;
  amount: number;
  bookingId: string;
  callbackUrl?: string;
  showtimeId: number;
}

export default function PaymentClient({
  method,
  paymentCode,
  amount,
  bookingId,
  callbackUrl,
  showtimeId,
}: PaymentClientProps) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [debugInfo, setDebugInfo] = useState<any>(null);

  // Logo và brand name helpers giữ nguyên
  const logoUrls: Record<string, string> = {
    momo: "https://res.cloudinary.com/diae3v9nm/image/upload/v1779817715/png-transparent-momo-hd-logo-thumbnail_n11goz.png",
    vnpay:
      "https://res.cloudinary.com/diae3v9nm/image/upload/v1779817717/vnpay-logo-vinadesign-25-12-57-55_pjsyns.jpg",
    zalopay:
      "https://res.cloudinary.com/diae3v9nm/image/upload/v1779817716/Logo-ZaloPay-Square-1024x1024_ajzbzi.webp",
  };

  const getBrandColor = () => {
    switch (method.toLowerCase()) {
      case "momo":
        return "bg-pink-600 hover:bg-pink-700";
      case "vnpay":
        return "bg-blue-700 hover:bg-blue-800";
      case "zalopay":
        return "bg-emerald-600 hover:bg-emerald-700";
      default:
        return "bg-blue-600 hover:bg-blue-700";
    }
  };

  const getBrandName = () => {
    switch (method.toLowerCase()) {
      case "momo":
        return "MoMo";
      case "vnpay":
        return "VNPAY";
      case "zalopay":
        return "ZaloPay";
      default:
        return method.toUpperCase();
    }
  };

  const currentMethod = method.toLowerCase();
  const logoUrl = logoUrls[currentMethod] || "";

  // Chuyển hướng về callback URL hoặc về trang chủ
  const redirectToCallback = (
    success: boolean,
    transactionId: string = "",
    showtimeId: number,
    isCancel = false,
  ) => {
    if (!callbackUrl) {
      // Dùng alert để thông báo kết quả
      alert(
        success
          ? "✅ Thanh toán thành công!"
          : isCancel
            ? "❌ Đã huỷ thanh toán."
            : "❌ Thanh toán thất bại!",
      );
      router.push("/");
      return;
    }

    const separator = callbackUrl.includes("?") ? "&" : "?";
    const params = new URLSearchParams();
    params.set("status", isCancel ? "cancel" : success ? "success" : "failed");
    params.set("paymentCode", paymentCode);
    params.set("bookingId", bookingId);
    if (success && transactionId) params.set("transactionId", transactionId);
    params.set("showtimeId", showtimeId.toString());

    window.location.href = `${callbackUrl}${separator}${params.toString()}`;
  };

  // Hủy thanh toán – sử dụng confirm thay vì modal
  const cancelPayment = () => {
    if (cancelling) return;

    if (
      !confirm("Bạn có chắc muốn huỷ thanh toán? Ghế đã chọn sẽ được trả lại.")
    ) {
      return;
    }

    setCancelling(true);

    // Truyền đúng tham số: success = false, showtimeId = hiện tại
    redirectToCallback(false, "", showtimeId, true);
  };

  // Xử lý nút back trình duyệt – dùng confirm thay vì modal
  useEffect(() => {
    window.history.pushState({ isPaymentPage: true }, "");

    const handlePopState = (event: PopStateEvent) => {
      if (loading || cancelling) return;

      // Ngăn rời trang ngay lập tức
      window.history.pushState(
        { isPaymentPage: true },
        "",
        window.location.href,
      );

      cancelPayment();
    };

    window.addEventListener("popstate", handlePopState);
    return () => window.removeEventListener("popstate", handlePopState);
  }, [loading, cancelling]);

  const handlePayment = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch("/api/complete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ paymentCode }),
      });

      const raw = await response.text();
      let data: any = {};
      try {
        data = JSON.parse(raw);
      } catch {}

      setDebugInfo({ httpStatus: response.status, rawBody: raw, ...data });

      if (response.ok && data.status === "SUCCESS") {
        redirectToCallback(
          true,
          data.transactionId ?? "",
          data.showtimeId ?? showtimeId,
        );
      } else {
        setError(data.message || "Thanh toán thất bại.");
      }
    } catch (err: any) {
      setDebugInfo({ error: err.message });
      setError("Có lỗi xảy ra khi xử lý thanh toán.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-3xl shadow-2xl overflow-hidden">
        {/* Header */}
        <div className={`${getBrandColor()} text-white p-8`}>
          <div className="flex items-center gap-4">
            {logoUrl && (
              <img
                src={logoUrl}
                alt={getBrandName()}
                className="w-14 h-14 bg-white rounded-2xl p-2 object-contain shadow-inner"
              />
            )}
            <div>
              <h1 className="text-2xl font-semibold">
                Thanh toán {getBrandName()}
              </h1>
              <p className="text-sm opacity-90 mt-1">
                Xác nhận giao dịch an toàn
              </p>
            </div>
          </div>
        </div>

        {/* Body */}
        <div className="p-8 space-y-8">
          <div className="text-center">
            <p className="text-gray-500 text-sm uppercase tracking-widest">
              SỐ TIỀN THANH TOÁN
            </p>
            <p className="text-5xl font-bold text-gray-900 mt-3">
              {amount.toLocaleString("vi-VN")}
              <span className="text-3xl font-normal text-gray-500"> đ</span>
            </p>
          </div>

          <div className="bg-gray-50 rounded-2xl p-6 space-y-4 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Mã đơn hàng</span>
              <span className="font-semibold text-gray-900">{bookingId}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Mã thanh toán</span>
              <span className="font-mono font-medium text-gray-900">
                {paymentCode}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Phương thức</span>
              <span className="font-semibold">{getBrandName()}</span>
            </div>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-600 p-4 rounded-2xl text-sm">
              {error}
            </div>
          )}

          {debugInfo && (
            <pre className="bg-gray-100 p-3 text-xs overflow-auto rounded-xl">
              {JSON.stringify(debugInfo, null, 2)}
            </pre>
          )}

          <div className="flex gap-3">
            <button
              onClick={handlePayment}
              disabled={loading || cancelling}
              className={`flex-1 py-4 px-6 rounded-2xl text-white font-semibold text-lg transition-all flex items-center justify-center gap-3 ${getBrandColor()} ${
                loading || cancelling
                  ? "opacity-75 cursor-not-allowed"
                  : "hover:shadow-xl active:scale-[0.985]"
              }`}
            >
              {loading ? (
                <>
                  <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Đang xử lý...
                </>
              ) : (
                `Xác nhận thanh toán ${amount.toLocaleString("vi-VN")}đ`
              )}
            </button>

            <button
              onClick={cancelPayment}
              disabled={loading || cancelling}
              className="flex-1 py-4 px-6 rounded-2xl bg-gray-200 text-gray-800 font-semibold text-lg hover:bg-gray-300 active:bg-gray-400 transition disabled:opacity-50"
            >
              {cancelling ? "Đang huỷ..." : "Huỷ thanh toán"}
            </button>
          </div>

          <div className="text-center text-xs text-gray-400">
            🔒 Thanh toán an toàn với chuẩn SSL
          </div>
        </div>
      </div>
    </div>
  );
}
