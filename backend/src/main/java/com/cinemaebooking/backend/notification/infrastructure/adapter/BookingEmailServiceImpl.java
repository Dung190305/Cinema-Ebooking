package com.cinemaebooking.backend.notification.infrastructure.adapter;

import com.cinemaebooking.backend.notification.application.dto.BookingEmailData;
import com.cinemaebooking.backend.notification.application.port.BookingEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEmailServiceImpl implements BookingEmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from:noreply@cinema-ebooking.com}")
    private String fromEmail;

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm").withZone(ZoneId.systemDefault());
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US));

    @Override
    public void sendPaymentSuccessEmail(String to, String userName, BookingEmailData booking, String qrCodeBase64) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("[Cinema E-Booking] Xác nhận thanh toán - " + booking.getBookingCode());

            String htmlContent = buildPaymentSuccessHtml(userName, booking, qrCodeBase64);
            helper.setText(htmlContent, true);

            if (qrCodeBase64 != null && !qrCodeBase64.isBlank()) {
                byte[] qrBytes = java.util.Base64.getDecoder().decode(qrCodeBase64);
                helper.addInline("qrCode", new ByteArrayResource(qrBytes), "image/png");
            }

            mailSender.send(message);
            log.info("Payment success email sent to {} for booking {}", to, booking.getBookingCode());

        } catch (Exception e) {
            log.error("Failed to send payment success email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    public void sendReminderEmail(String to, String userName, BookingEmailData booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("[Cinema E-Booking] Nhắc nhở: " + booking.getMovieTitle() + " sắp chiếu!");

            String htmlContent = buildReminderHtml(userName, booking);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Reminder email sent to {} for booking {}", to, booking.getBookingCode());

        } catch (Exception e) {
            log.error("Failed to send reminder email to {}: {}", to, e.getMessage());
        }
    }

    private String buildPaymentSuccessHtml(String userName, BookingEmailData booking, String qrCodeBase64) {
        String seatsHtml = buildSeatsSection(booking);
        String combosHtml = buildCombosSection(booking);
        String couponHtml = buildCouponSection(booking);
        String showtime = DATETIME_FORMATTER.format(booking.getShowtimeStartTime());
        String qrSection = buildQRCodeSection(qrCodeBase64);
        String priceFormatted = PRICE_FORMAT.format(booking.getFinalAmount());

        return "<!DOCTYPE html>\n" +
                "<html lang=\"vi\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Xác nhận thanh toán</title>\n" +
                "</head>\n" +
                "<body style=\"margin:0;padding:0;background-color:#f0f2f5;font-family:'Segoe UI',Arial,Helvetica,sans-serif;\">\n" +
                "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f0f2f5;padding:30px 16px;\">\n" +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\"\n" +
                "               style=\"background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.12);\">\n" +

                "          <!-- HEADER -->\n" +
                "          <tr>\n" +
                "            <td style=\"background:linear-gradient(135deg,#e53935 0%,#c62828 100%);padding:32px 40px 28px;text-align:center;\">\n" +
                "              <p style=\"margin:0;color:rgba(255,255,255,0.65);font-size:11px;letter-spacing:3px;text-transform:uppercase;\">CINEMA E-BOOKING</p>\n" +
                "              <h1 style=\"color:#ffffff;margin:8px 0 0;font-size:26px;font-weight:800;letter-spacing:0.5px;\">Xác nhận đặt vé</h1>\n" +
                "              <p style=\"color:rgba(255,255,255,0.8);margin:6px 0 0;font-size:14px;\">Thanh toán thành công &bull; Vé của bạn đã sẵn sàng</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +

                "          <!-- GREETING -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding:24px 40px 0;\">\n" +
                "              <p style=\"margin:0;color:#333;font-size:15px;\">Xin chào <strong style=\"color:#e53935;\">" + userName + "</strong>,</p>\n" +
                "              <p style=\"margin:6px 0 0;color:#666;font-size:14px;\">Cảm ơn bạn đã đặt vé. Dưới đây là thông tin chi tiết:</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +

                "          <!-- TWO COLUMN: LEFT (CODE) + RIGHT (QR) -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding:20px 40px 24px;\">\n" +
                "              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                <tr>\n" +

                "                  <!-- LEFT: Booking Code Card -->\n" +
                "                  <td width=\"50%\" style=\"padding-right:10px;vertical-align:top;\">\n" +
                "                    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"\n" +
                "                           style=\"background-color:#1a1a2e;border-radius:14px;overflow:hidden;\">\n" +
                "                      <tr>\n" +
                "                        <td style=\"padding:20px 18px 22px;text-align:center;\">\n" +
                "                          <p style=\"margin:0 0 4px;color:rgba(255,255,255,0.45);font-size:10px;letter-spacing:2px;text-transform:uppercase;\">Mã đặt vé</p>\n" +
                "                          <p style=\"margin:0 0 16px;color:#ffffff;font-size:22px;font-weight:800;letter-spacing:3px;\">" + booking.getBookingCode() + "</p>\n" +
                "                          <div style=\"width:40px;height:2px;background:#e53935;margin:0 auto 14px;border-radius:2px;\"></div>\n" +
                "                          <p style=\"margin:0 0 3px;color:rgba(255,255,255,0.4);font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Phim</p>\n" +
                "                          <p style=\"margin:0 0 12px;color:#ffffff;font-size:13px;font-weight:600;line-height:1.3;\">" + booking.getMovieTitle() + "</p>\n" +
                "                          <p style=\"margin:0 0 3px;color:rgba(255,255,255,0.4);font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Thời gian</p>\n" +
                "                          <p style=\"margin:0;color:#ffffff;font-size:13px;\">" + showtime + "</p>\n" +
                "                        </td>\n" +
                "                      </tr>\n" +
                "                    </table>\n" +
                "                  </td>\n" +

                "                  <!-- RIGHT: QR Code Card -->\n" +
                "                  <td width=\"50%\" style=\"padding-left:10px;vertical-align:top;\">\n" +
                "                    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"\n" +
                "                           style=\"background-color:#f8f9fa;border-radius:14px;border:1px solid #e8e8e8;overflow:hidden;\">\n" +
                "                      <tr>\n" +
                "                        <td style=\"padding:20px 18px 22px;text-align:center;\">\n" +
                "                          <p style=\"margin:0 0 12px;color:#888;font-size:11px;letter-spacing:1px;text-transform:uppercase;\">Quét để check-in</p>\n" +
                qrSection + "\n" +
                "                          <p style=\"margin:10px 0 0;color:#aaa;font-size:11px;\">Quét tại quầy hoặc cổng</p>\n" +
                "                        </td>\n" +
                "                      </tr>\n" +
                "                    </table>\n" +
                "                  </td>\n" +

                "                </tr>\n" +
                "              </table>\n" +
                "            </td>\n" +
                "          </tr>\n" +

                "          <!-- INFO TABLE -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding:0 40px 20px;\">\n" +
                "              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"\n" +
                "                     style=\"background-color:#fafafa;border-radius:12px;border:1px solid #e8e8e8;overflow:hidden;\">\n" +
                "                <tr>\n" +
                "                  <td width=\"50%\" style=\"padding:14px 18px;border-right:1px solid #e8e8e8;vertical-align:top;\">\n" +
                "                    <p style=\"margin:0 0 3px;color:#aaa;font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Rạp</p>\n" +
                "                    <p style=\"margin:0;color:#222;font-size:14px;font-weight:600;\">" + booking.getCinemaName() + "</p>\n" +
                "                  </td>\n" +
                "                  <td width=\"50%\" style=\"padding:14px 18px;vertical-align:top;\">\n" +
                "                    <p style=\"margin:0 0 3px;color:#aaa;font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Phòng chiếu</p>\n" +
                "                    <p style=\"margin:0;color:#222;font-size:14px;font-weight:600;\">" + booking.getRoomName() + "</p>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                seatsHtml + "\n" +
                combosHtml + "\n" +
                couponHtml + "\n" +
                "                <tr>\n" +
                "                  <td colspan=\"2\" style=\"padding:14px 18px;background-color:#fff0f0;text-align:right;\">\n" +
                "                    <span style=\"color:#888;font-size:12px;\">Tổng cộng&nbsp;&nbsp;</span>\n" +
                "                    <span style=\"color:#e53935;font-size:22px;font-weight:800;\">" + priceFormatted + " VND</span>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </td>\n" +
                "          </tr>\n" +

                "          <!-- FOOTER -->\n" +
                "          <tr>\n" +
                "            <td style=\"background-color:#f5f5f5;padding:18px 40px;text-align:center;border-top:1px solid #e8e8e8;\">\n" +
                "              <p style=\"margin:0;color:#999;font-size:12px;\">Quý khách vui lòng đến trước <strong style=\"color:#e53935;\">15 phút</strong> để check-in.</p>\n" +
                "              <p style=\"margin:6px 0 0;color:#bbb;font-size:11px;\">Cinema E-Booking &mdash; Trải nghiệm điện ảnh đẳng cấp</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +

                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";
    }

    private String buildReminderHtml(String userName, BookingEmailData booking) {
        String showtime = DATETIME_FORMATTER.format(booking.getShowtimeStartTime());

        return "<!DOCTYPE html>\n" +
                "<html lang=\"vi\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Nhắc nhở lịch chiếu</title>\n" +
                "</head>\n" +
                "<body style=\"margin:0;padding:0;background-color:#f0f2f5;font-family:'Segoe UI',Arial,Helvetica,sans-serif;\">\n" +
                "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f0f2f5;padding:30px 16px;\">\n" +
                "    <tr>\n" +
                "      <td align=\"center\">\n" +
                "        <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\"\n" +
                "               style=\"background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.12);\">\n" +

                "          <!-- HEADER -->\n" +
                "          <tr>\n" +
                "            <td style=\"background:linear-gradient(135deg,#ff9800 0%,#f57c00 100%);padding:32px 40px 28px;text-align:center;\">\n" +
                "              <p style=\"margin:0;color:rgba(255,255,255,0.6);font-size:11px;letter-spacing:3px;text-transform:uppercase;\">CINEMA E-BOOKING</p>\n" +
                "              <h1 style=\"color:#ffffff;margin:8px 0 0;font-size:26px;font-weight:800;letter-spacing:0.5px;\">Nhắc nhở lịch chiếu</h1>\n" +
                "              <p style=\"color:rgba(255,255,255,0.8);margin:6px 0 0;font-size:14px;\">Phim của bạn sắp được chiếu!</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +

                "          <!-- COUNTDOWN BANNER -->\n" +
                "          <tr>\n" +
                "            <td style=\"background:#fff8f0;padding:16px 40px;text-align:center;border-bottom:1px solid #ffe0b2;\">\n" +
                "              <p style=\"margin:0;color:#e65100;font-size:14px;font-weight:600;\">Phim sẽ được chiếu trong <strong>2 giờ nữa</strong>!</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +

                "          <!-- GREETING -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding:24px 40px 0;\">\n" +
                "              <p style=\"margin:0;color:#333;font-size:15px;\">Xin chào <strong style=\"color:#ff9800;\">" + userName + "</strong>,</p>\n" +
                "              <p style=\"margin:6px 0 0;color:#666;font-size:14px;\">Đừng quên đến đúng giờ để không bỏ lỡ khoảnh khắc tuyệt vời nhé!</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +

                "          <!-- MOVIE INFO CARD -->\n" +
                "          <tr>\n" +
                "            <td style=\"padding:20px 40px;\">\n" +
                "              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"\n" +
                "                     style=\"background-color:#fafafa;border-radius:12px;border:1px solid #e8e8e8;overflow:hidden;\">\n" +
                "                <tr>\n" +
                "                  <td width=\"50%\" style=\"padding:16px 18px;border-right:1px solid #e8e8e8;vertical-align:top;\">\n" +
                "                    <p style=\"margin:0 0 4px;color:#aaa;font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Phim</p>\n" +
                "                    <p style=\"margin:0;color:#222;font-size:15px;font-weight:700;\">" + booking.getMovieTitle() + "</p>\n" +
                "                  </td>\n" +
                "                  <td width=\"50%\" style=\"padding:16px 18px;vertical-align:top;\">\n" +
                "                    <p style=\"margin:0 0 4px;color:#aaa;font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Thời gian</p>\n" +
                "                    <p style=\"margin:0;color:#222;font-size:15px;font-weight:600;\">" + showtime + "</p>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                  <td width=\"50%\" style=\"padding:16px 18px;border-right:1px solid #e8e8e8;vertical-align:top;\">\n" +
                "                    <p style=\"margin:0 0 4px;color:#aaa;font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Rạp</p>\n" +
                "                    <p style=\"margin:0;color:#222;font-size:14px;font-weight:600;\">" + booking.getCinemaName() + "</p>\n" +
                "                  </td>\n" +
                "                  <td width=\"50%\" style=\"padding:16px 18px;vertical-align:top;\">\n" +
                "                    <p style=\"margin:0 0 4px;color:#aaa;font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Phòng chiếu</p>\n" +
                "                    <p style=\"margin:0;color:#222;font-size:14px;font-weight:600;\">" + booking.getRoomName() + "</p>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                  <td colspan=\"2\" style=\"padding:14px 18px;background-color:#fff3e0;text-align:center;\">\n" +
                "                    <p style=\"margin:0;color:#e65100;font-size:13px;font-weight:600;\">Đến sớm 15 phút để check-in và chọn ghế tốt nhất!</p>\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </td>\n" +
                "          </tr>\n" +

                "          <!-- FOOTER -->\n" +
                "          <tr>\n" +
                "            <td style=\"background-color:#f5f5f5;padding:18px 40px;text-align:center;border-top:1px solid #e8e8e8;\">\n" +
                "              <p style=\"margin:0;color:#bbb;font-size:11px;\">Cinema E-Booking &mdash; Trải nghiệm điện ảnh đẳng cấp</p>\n" +
                "            </td>\n" +
                "          </tr>\n" +

                "        </table>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";
    }

    private String buildSeatsSection(BookingEmailData booking) {
        if (booking.getSeats() == null || booking.getSeats().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>\n");
        sb.append("  <td colspan=\"2\" style=\"padding:12px 18px;border-top:1px solid #e8e8e8;\">\n");
        sb.append("    <p style=\"margin:0 0 4px;color:#aaa;font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Ghế</p>\n");
        sb.append("    <p style=\"margin:0;color:#222;font-size:13px;font-weight:600;\">");
        for (int i = 0; i < booking.getSeats().size(); i++) {
            BookingEmailData.SeatData seat = booking.getSeats().get(i);
            if (i > 0) sb.append(", ");
            sb.append(seat.getSeatName()).append(" (").append(seat.getSeatType()).append(")");
        }
        sb.append("</p>\n");
        sb.append("  </td>\n");
        sb.append("</tr>\n");
        return sb.toString();
    }

    private String buildCombosSection(BookingEmailData booking) {
        if (booking.getCombos() == null || booking.getCombos().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>\n");
        sb.append("  <td colspan=\"2\" style=\"padding:12px 18px;border-top:1px solid #e8e8e8;\">\n");
        sb.append("    <p style=\"margin:0 0 4px;color:#aaa;font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Combo</p>\n");
        for (BookingEmailData.ComboData combo : booking.getCombos()) {
            sb.append("<p style=\"margin:0 0 2px;color:#222;font-size:13px;font-weight:600;\">")
              .append(combo.getComboName()).append(" x").append(combo.getQuantity()).append("</p>\n");
        }
        sb.append("  </td>\n");
        sb.append("</tr>\n");
        return sb.toString();
    }

    private String buildCouponSection(BookingEmailData booking) {
        if (booking.getCoupon() == null) {
            return "";
        }
        String discount = PRICE_FORMAT.format(booking.getCoupon().getDiscountAmount());
        return "<tr>\n" +
               "  <td colspan=\"2\" style=\"padding:12px 18px;border-top:1px solid #e8e8e8;background-color:#f0fff4;\">\n" +
               "    <p style=\"margin:0 0 4px;color:#aaa;font-size:10px;letter-spacing:1px;text-transform:uppercase;\">Mã giảm giá</p>\n" +
               "    <p style=\"margin:0;color:#2e7d32;font-size:13px;font-weight:600;\">" + booking.getCoupon().getCode() + " &nbsp;&ndash;&nbsp; &minus;" + discount + " VND</p>\n" +
               "  </td>\n" +
               "</tr>\n";
    }

    private String buildQRCodeSection(String qrCodeBase64) {
        if (qrCodeBase64 == null || qrCodeBase64.isBlank()) {
            return "<p style=\"margin:0;color:#ccc;font-size:12px;text-align:center;\">QR không khả dụng</p>";
        }
        return "<img src=\"cid:qrCode\" alt=\"QR Code\" width=\"220\" height=\"220\" " +
               "style=\"border-radius:12px;box-shadow:0 4px 16px rgba(229,57,53,0.15);display:block;margin:0 auto;\">";
    }
}
