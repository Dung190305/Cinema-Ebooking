import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Mock Payment Gateway",
  description: "Xác nhận thanh toán đơn hàng - MoMo, VNPay, ZaloPay",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="vi">
      <body className="bg-gray-50 antialiased" suppressHydrationWarning>
        {children}
      </body>
    </html>
  );
}
