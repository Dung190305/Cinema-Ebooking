export default function Home() {
  const logoUrls: Record<string, string> = {
    momo: "https://res.cloudinary.com/diae3v9nm/image/upload/v1779817715/png-transparent-momo-hd-logo-thumbnail_n11goz.png",
    vnpay:
      "https://res.cloudinary.com/diae3v9nm/image/upload/v1779817717/vnpay-logo-vinadesign-25-12-57-55_pjsyns.jpg",
    zalopay:
      "https://res.cloudinary.com/diae3v9nm/image/upload/v1779817716/Logo-ZaloPay-Square-1024x1024_ajzbzi.webp",
  };

  return (
    <div className="min-h-screen bg-linear-to-br from-gray-900 to-gray-800 text-white">
      <div className="max-w-2xl mx-auto py-16 px-6">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold mb-3">Mock Payment Gateway</h1>
          <p className="text-gray-400 text-lg">
            Giao diện thanh toán mô phỏng MoMo, VNPay, ZaloPay
          </p>
        </div>

        <div className="space-y-4">
          <a
            href="/payment/momo?paymentCode=TEST123&amount=150000&bookingId=BOOKING001"
            className="block bg-white/10 hover:bg-white/20 border border-white/20 rounded-2xl p-6 transition-all hover:scale-[1.02]"
          >
            <div className="flex items-center gap-4">
              <img
                src={logoUrls.momo}
                alt="MoMo"
                className="w-12 h-12 bg-white rounded-2xl p-1.5 object-contain shadow-inner"
              />
              <div>
                <h3 className="font-semibold text-xl">MoMo</h3>
                <p className="text-gray-400">150.000đ - BOOKING001</p>
              </div>
            </div>
          </a>

          <a
            href="/payment/vnpay?paymentCode=TEST456&amount=250000&bookingId=BOOKING002"
            className="block bg-white/10 hover:bg-white/20 border border-white/20 rounded-2xl p-6 transition-all hover:scale-[1.02]"
          >
            <div className="flex items-center gap-4">
              <img
                src={logoUrls.vnpay}
                alt="VNPAY"
                className="w-12 h-12 bg-white rounded-2xl p-1.5 object-contain shadow-inner"
              />
              <div>
                <h3 className="font-semibold text-xl">VNPAY</h3>
                <p className="text-gray-400">250.000đ - BOOKING002</p>
              </div>
            </div>
          </a>

          <a
            href="/payment/zalopay?paymentCode=TEST789&amount=98000&bookingId=BOOKING003"
            className="block bg-white/10 hover:bg-white/20 border border-white/20 rounded-2xl p-6 transition-all hover:scale-[1.02]"
          >
            <div className="flex items-center gap-4">
              <img
                src={logoUrls.zalopay}
                alt="ZaloPay"
                className="w-12 h-12 bg-white rounded-2xl p-1.5 object-contain shadow-inner"
              />
              <div>
                <h3 className="font-semibold text-xl">ZaloPay</h3>
                <p className="text-gray-400">98.000đ - BOOKING003</p>
              </div>
            </div>
          </a>
        </div>
      </div>
    </div>
  );
}
