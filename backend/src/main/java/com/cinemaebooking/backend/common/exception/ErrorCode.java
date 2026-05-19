package com.cinemaebooking.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ErrorCode - Centralized error definition for entire system (ENTERPRISE V2 - CLEAN REFACTOR).
 * STRICT RULES (unchanged):
 * 1. ONLY Backend Lead/Maintainer is allowed to add or modify error codes.
 * 2. DO NOT create new error enums inside any domain packages.
 * 3. DO NOT modify existing error code numbers once they are released.
 * 4. ALL exceptions MUST be thrown via corresponding Exception Factories.
 * 5. NEVER throw raw RuntimeException, new BaseException(...) directly,
 *    or use ErrorCode directly in business code.
 * 6. Message in ErrorCode is the default. It can be overridden when throwing.
 * ─────────────────────────────────────────────────────────────────
 * CODE RANGE REGISTRY (V2)
 * ┌──────────────┬──────────────────────────────┬───────────────┬────────────────┐
 * │ Group        │ Domain                       │ Sub-range     │ Block          │
 * ├──────────────┼──────────────────────────────┼───────────────┼────────────────┤
 * │ SYSTEM       │ audit_log                    │ 1001–1099     │ 1000–1099      │
 * ├──────────────┼──────────────────────────────┼───────────────┼────────────────┤
 * │ COMMON       │ common                       │ 1100–1199     │ 1100–1199      │
 * ├──────────────┼──────────────────────────────┼───────────────┼────────────────┤
 * │ CORE         │ user                         │ 2001–2008     │ 2000–2199      │
 * │              │ loyalty                      │ 2009–2019     │                │
 * │              │ (membership_tier,            │               │                │
 * │              │  loyalty_account,            │               │                │
 * │              │  loyalty_earning_rule)       │               │                │
 * ├──────────────┼──────────────────────────────┼───────────────┼────────────────┤
 * │ CATALOG      │ movie                        │ 3001–3004     │ 3000–3399      │
 * │              │ cinema                       │ 3005–3007     │                │
 * │              │ room                         │ 3008–3011     │                │
 * │              │ seat                         │ 3012–3020     │                │
 * │              │ (incl. seat_type)            │               │                │
 * │              │ showtime                     │ 3021–3028     │                │
 * │              │ (incl. showtime_format)      │               │                │
 * │              │ showtime_seat                │ 3029–3031     │                │
 * ├──────────────┼──────────────────────────────┼───────────────┼────────────────┤
 * │ PRODUCT      │ combo                        │ 3401–3404     │ 3400–3599      │
 * │              │ coupon                       │ 3405–3409     │                │
 * ├──────────────┼──────────────────────────────┼───────────────┼────────────────┤
 * │ BOOKING      │ booking                      │ 4001–4009     │ 4000–4499      │
 * │              │ booking_combo                │ 4010–4011     │                │
 * │              │ booking_coupon               │ 4012–4014     │                │
 * │              │ seat_lock                    │ 4015–4018     │                │
 * │              │ ticket                       │ 4019–4022     │                │
 * ├──────────────┼──────────────────────────────┼───────────────┼────────────────┤
 * │ PAYMENT      │ payment                      │ 4501–4509     │ 4500–4699      │
 * │              │ refund                       │ 4510–4513     │                │
 * ├──────────────┼──────────────────────────────┼───────────────┼────────────────┤
 * │ INTERACTION  │ review                       │ 4701–4704     │ 4700–4899      │
 * │              │ notification                 │ 4705–4707     │                │
 * ├──────────────┼──────────────────────────────┼───────────────┼────────────────┤
 * │ SUPPORTING   │ user_coupon                  │ 4901–4904     │ 4900–4999      │
 * └──────────────┴──────────────────────────────┴───────────────┴────────────────┘
 * V2 CLEAN IMPROVEMENTS:
 * • Naming 100% consistent: DOMAIN_ACTION_OBJECT
 * • Messages ngắn gọn, rõ ràng
 * • Added ErrorType: BUSINESS / TECHNICAL
 * • Loại bỏ ambiguity, validation naming thống nhất
 * V3 IMPROVEMENTS:
 * • chuyển toàn bộ message sang tiếng Việt
 * @author Hieu Nguyen
 * @since 2026 (V3 Clean Refactored)
 */
@Getter
public enum ErrorCode {

    // ===================== SYSTEM =====================
    AUDIT_LOG_NOT_FOUND         (1001, "Không tìm thấy audit log",                         HttpStatus.NOT_FOUND,             ErrorType.TECHNICAL),
    AUDIT_LOG_WRITE_FAILED      (1002, "Ghi audit log thất bại",                           HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.TECHNICAL),
    AUDIT_LOG_INVALID_TYPE      (1003, "Loại audit log không hợp lệ",                      HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),

    // ===================== COMMON =====================
    UNCATEGORIZED_EXCEPTION     (1100, "Lỗi hệ thống không xác định",                      HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.TECHNICAL),
    INVALID_INPUT               (1101, "Dữ liệu đầu vào không hợp lệ",                    HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    INVALID_REQUEST             (1102, "Yêu cầu không hợp lệ",                            HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    UNAUTHORIZED                (1103, "Chưa xác thực",                                    HttpStatus.UNAUTHORIZED,          ErrorType.BUSINESS),
    FORBIDDEN                   (1104, "Không có quyền thực hiện thao tác này",            HttpStatus.FORBIDDEN,             ErrorType.BUSINESS),
    RESOURCE_NOT_FOUND          (1105, "Không tìm thấy tài nguyên",                        HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    RESOURCE_ALREADY_EXISTS     (1106, "Tài nguyên đã tồn tại",                            HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    CONCURRENCY_CONFLICT        (1107, "Xung đột đồng thời, vui lòng thử lại",             HttpStatus.CONFLICT,              ErrorType.BUSINESS),

    // ===================== CORE =====================
    // user: 2001–2007
    USER_NOT_FOUND              (2001, "Không tìm thấy người dùng",                        HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    USER_INVALID_CREDENTIALS    (2002, "Tên đăng nhập hoặc mật khẩu không đúng",           HttpStatus.UNAUTHORIZED,          ErrorType.BUSINESS),
    USER_ACCOUNT_DISABLED       (2003, "Tài khoản người dùng đã bị vô hiệu hóa",           HttpStatus.FORBIDDEN,             ErrorType.BUSINESS),
    USER_INVALID_STATUS         (2004, "Trạng thái người dùng không hợp lệ",               HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    USER_INVALID_EMAIL          (2005, "Định dạng email không hợp lệ",                     HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    USER_INVALID_PASSWORD       (2006, "Mật khẩu không đáp ứng yêu cầu",                  HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    USER_NOT_VERIFIED           (2007, "Tài khoản người dùng chưa được xác minh",          HttpStatus.FORBIDDEN,             ErrorType.BUSINESS),

    // loyalty: 2009–2019
    LOYALTY_ACCOUNT_NOT_FOUND      (2009, "Không tìm thấy tài khoản tích điểm",            HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    LOYALTY_ACCOUNT_ALREADY_EXISTS (2010, "Tài khoản tích điểm đã tồn tại",                HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    LOYALTY_ACCOUNT_LOCKED         (2011, "Tài khoản tích điểm đang bị khóa",              HttpStatus.FORBIDDEN,             ErrorType.BUSINESS),
    LOYALTY_INSUFFICIENT_POINTS    (2012, "Điểm tích lũy không đủ",                        HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    LOYALTY_POINTS_EXPIRED         (2013, "Điểm tích lũy đã hết hạn",                      HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    LOYALTY_TIER_INVALID           (2014, "Hạng thành viên không hợp lệ",                  HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    LOYALTY_TIER_NOT_ELIGIBLE      (2015, "Người dùng không đủ điều kiện cho hạng này",    HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    LOYALTY_RULE_INVALID           (2016, "Quy tắc tích điểm không hợp lệ",                HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    LOYALTY_RULE_CONFLICT          (2017, "Quy tắc tích điểm bị xung đột",                 HttpStatus.CONFLICT,              ErrorType.BUSINESS),

    // ===================== CATALOG =====================
    // movie: 3001–3004
    MOVIE_NOT_FOUND             (3001, "Không tìm thấy phim",                              HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),

    // cinema: 3005–3007
    CINEMA_NOT_FOUND            (3005, "Không tìm thấy rạp chiếu",                         HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    CINEMA_HAS_UNDELETED_ROOMS  (3006, "Không thể xóa rạp vì vẫn còn phòng chiếu",         HttpStatus.CONFLICT,              ErrorType.BUSINESS),

    // room: 3008–3011
    ROOM_NOT_FOUND              (3008, "Không tìm thấy phòng chiếu",                       HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    ROOM_DELETE_BLOCKED_BY_SHOWTIME (3009, "Không thể xoá phòng vì vẫn còn suất chiếu đang hoạt động",
            HttpStatus.CONFLICT,
            ErrorType.BUSINESS),
    // seat: 3012–3020
    /*SEAT_NOT_FOUND              (3012, "Không tìm thấy ghế",                               HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    SEAT_ALREADY_EXISTS         (3013, "Ghế đã tồn tại trong phòng chiếu này",             HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    SEAT_INVALID_STATUS         (3014, "Trạng thái ghế không hợp lệ",                      HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    SEAT_ALREADY_BOOKED         (3015, "Ghế đã được đặt",                                  HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    SEAT_NOT_AVAILABLE          (3016, "Ghế không còn khả dụng",                           HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    SEAT_ALREADY_SELECTED_BY_USER (3017, "Ghế đã được người dùng chọn",                   HttpStatus.CONFLICT,              ErrorType.BUSINESS),*/
    SEAT_TYPE_NOT_FOUND         (3018, "Không tìm thấy loại ghế",                          HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    SEAT_TYPE_ALREADY_EXISTS    (3019, "Loại ghế đã tồn tại",                              HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    SEAT_TYPE_INVALID           (3020, "Loại ghế không hợp lệ",                            HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),

    // showtime: 3021–3028
    SHOWTIME_NOT_FOUND          (3021, "Không tìm thấy suất chiếu",                        HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    SHOWTIME_INVALID_STATUS     (3022, "Trạng thái suất chiếu không hợp lệ",               HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    SHOWTIME_EXPIRED            (3023, "Suất chiếu đã kết thúc",                           HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    SHOWTIME_NOT_BOOKABLE       (3024, "Suất chiếu không thể đặt vé",                      HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    SHOWTIME_FORMAT_NOT_FOUND   (3025, "Không tìm thấy định dạng suất chiếu",              HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    SHOWTIME_FORMAT_INVALID     (3026, "Định dạng suất chiếu không hợp lệ",                HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),

    // showtime_seat: 3029–3031
    SHOWTIME_SEAT_NOT_FOUND      (3029, "Không tìm thấy ghế trong suất chiếu",             HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    SHOWTIME_SEAT_ALREADY_LOCKED (3030, "Ghế trong suất chiếu đã bị khóa",                 HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    SHOWTIME_SEAT_UNAVAILABLE    (3031, "Ghế trong suất chiếu không khả dụng",             HttpStatus.CONFLICT,              ErrorType.BUSINESS),

    // genre: 3032–3033
    GENRE_NOT_FOUND             (3032, "Không tìm thấy thể loại phim",                     HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),

    // room_layout: 3034–3038
    ROOM_LAYOUT_NOT_FOUND              (3034, "Không tìm thấy layout của phòng",                      HttpStatus.NOT_FOUND,   ErrorType.BUSINESS),
    ROOM_LAYOUT_ALREADY_EXISTS         (3035, "Layout cho phòng này đã được tạo",                    HttpStatus.CONFLICT,     ErrorType.BUSINESS),
    ROOM_LAYOUT_INVALID_VERSION        (3036, "Số phiên bản layout không hợp lệ",                   HttpStatus.BAD_REQUEST,  ErrorType.BUSINESS),
    ROOM_LAYOUT_NO_CURRENT             (3037, "Không có layout hiện tại cho phòng vào thời điểm này",HttpStatus.NOT_FOUND,   ErrorType.BUSINESS),
    ROOM_LAYOUT_INVALID_EFFECTIVE_DATE (3038, "Ngày hiệu lực của layout không hợp lệ",               HttpStatus.BAD_REQUEST,  ErrorType.BUSINESS),

    // room_layout_seat: 3039–3043
    ROOM_LAYOUT_SEAT_NOT_FOUND              (3039, "Không tìm thấy ghế trong layout",                 HttpStatus.NOT_FOUND,   ErrorType.BUSINESS),
    ROOM_LAYOUT_SEAT_DUPLICATE_POSITION     (3040, "Vị trí ghế bị trùng trong layout",                HttpStatus.CONFLICT,     ErrorType.BUSINESS),
    ROOM_LAYOUT_SEAT_INVALID_POSITION       (3041, "Vị trí ghế không hợp lệ (hàng/cột vượt quá giới hạn)", HttpStatus.BAD_REQUEST, ErrorType.BUSINESS),
    ROOM_LAYOUT_SEAT_NOT_BELONG_TO_CURRENT  (3042, "Ghế không thuộc layout hiện tại của phòng",       HttpStatus.BAD_REQUEST,  ErrorType.BUSINESS),
    ROOM_LAYOUT_SEAT_COUPLE_GROUP_INVALID   (3043, "Nhóm ghế đôi không hợp lệ (không đủ 2 ghế hoặc không kề nhau)", HttpStatus.BAD_REQUEST, ErrorType.BUSINESS),

    // ===================== PRODUCT =====================
    COMBO_NOT_FOUND             (3401, "Không tìm thấy combo",                             HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    COMBO_ALREADY_EXISTS        (3402, "Combo đã tồn tại",                                 HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    COMBO_INVALID_STATUS        (3403, "Trạng thái combo không hợp lệ",                    HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    COMBO_OUT_OF_STOCK          (3404, "Combo đã hết hàng",                                HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),

    COUPON_NOT_FOUND            (3405, "Không tìm thấy mã giảm giá",                       HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    COUPON_ALREADY_EXISTS       (3406, "Mã giảm giá đã tồn tại",                           HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    COUPON_EXPIRED              (3407, "Mã giảm giá đã hết hạn",                           HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    COUPON_INVALID              (3408, "Mã giảm giá không áp dụng được cho đơn này",       HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    COUPON_MAX_USAGE_REACHED    (3409, "Mã giảm giá đã đạt giới hạn sử dụng",              HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),

    // ===================== BOOKING =====================
    BOOKING_NOT_FOUND           (4001, "Không tìm thấy đặt vé",                            HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    BOOKING_ALREADY_EXISTS      (4002, "Đặt vé đã tồn tại",                                HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    BOOKING_INVALID_STATUS      (4003, "Trạng thái đặt vé không hợp lệ",                   HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    BOOKING_CANCELLED           (4004, "Đặt vé đã bị hủy",                                 HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    BOOKING_EXPIRED             (4005, "Phiên đặt vé đã hết hạn",                          HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    BOOKING_PROCESS_FAILED      (4006, "Xử lý đặt vé thất bại",                            HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.TECHNICAL),
    BOOKING_ALREADY_PAID        (4007, "Đặt vé đã được thanh toán",                        HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    BOOKING_NOT_OWNED_BY_USER   (4008, "Đặt vé không thuộc về người dùng hiện tại",        HttpStatus.FORBIDDEN,             ErrorType.BUSINESS),
    BOOKING_TIMEOUT             (4009, "Đặt vé đã hết thời gian chờ",                      HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),

    BOOKING_COMBO_NOT_FOUND        (4010, "Không tìm thấy combo trong đặt vé",             HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    BOOKING_COMBO_INVALID_QUANTITY (4011, "Số lượng combo trong đặt vé không hợp lệ",      HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),

    BOOKING_COUPON_NOT_FOUND       (4012, "Không tìm thấy mã giảm giá trong đặt vé",       HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    BOOKING_COUPON_ALREADY_APPLIED (4013, "Mã giảm giá đã được áp dụng cho đặt vé này",   HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    BOOKING_COUPON_NOT_APPLICABLE  (4014, "Mã giảm giá không áp dụng được cho đặt vé này",HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),

    SEAT_LOCK_NOT_FOUND         (4015, "Không tìm thấy khóa ghế",                          HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    SEAT_LOCK_EXPIRED           (4016, "Khóa ghế đã hết hạn",                              HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    SEAT_LOCK_CONFLICT          (4017, "Ghế đã được người khác khóa",                      HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    SEAT_LOCK_FAILED            (4018, "Khóa ghế thất bại",                                HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.TECHNICAL),

    TICKET_NOT_FOUND            (4019, "Không tìm thấy vé",                                HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    TICKET_ALREADY_USED         (4020, "Vé đã được sử dụng",                               HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    TICKET_INVALID_STATUS       (4021, "Trạng thái vé không hợp lệ",                       HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    TICKET_GENERATION_FAILED    (4022, "Tạo vé thất bại",                                  HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.TECHNICAL),

    // ===================== PAYMENT =====================
    PAYMENT_NOT_FOUND           (4501, "Không tìm thấy thanh toán",                        HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    PAYMENT_ALREADY_EXISTS      (4502, "Thanh toán đã tồn tại cho đặt vé này",             HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    PAYMENT_FAILED              (4503, "Xử lý thanh toán thất bại",                        HttpStatus.BAD_GATEWAY,           ErrorType.TECHNICAL),
    PAYMENT_INVALID_AMOUNT      (4504, "Số tiền thanh toán không hợp lệ",                  HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    PAYMENT_EXPIRED             (4505, "Phiên thanh toán đã hết hạn",                      HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    PAYMENT_INVALID_STATUS      (4506, "Trạng thái thanh toán không hợp lệ",               HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    PAYMENT_METHOD_NOT_SUPPORTED(4507, "Phương thức thanh toán không được hỗ trợ",         HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    PAYMENT_DUPLICATE_REQUEST   (4508, "Yêu cầu thanh toán bị trùng lặp",                  HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    PAYMENT_GATEWAY_TIMEOUT     (4509, "Cổng thanh toán không phản hồi",                   HttpStatus.BAD_GATEWAY,           ErrorType.TECHNICAL),

    REFUND_NOT_FOUND            (4510, "Không tìm thấy yêu cầu hoàn tiền",                 HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    REFUND_ALREADY_PROCESSED    (4511, "Yêu cầu hoàn tiền đã được xử lý",                  HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    REFUND_FAILED               (4512, "Xử lý hoàn tiền thất bại",                         HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.TECHNICAL),
    REFUND_NOT_ELIGIBLE         (4513, "Đặt vé không đủ điều kiện hoàn tiền",              HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),

    // ===================== INTERACTION =====================
    REVIEW_NOT_FOUND            (4701, "Không tìm thấy đánh giá",                          HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    REVIEW_ALREADY_EXISTS       (4702, "Đánh giá đã tồn tại cho đặt vé này",               HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    REVIEW_INVALID_RATING       (4703, "Điểm đánh giá không hợp lệ",                       HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    REVIEW_NOT_ELIGIBLE         (4704, "Người dùng không đủ điều kiện đánh giá phim này",  HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    REVIEW_NOT_OWNED_BY_USER    (4705, "Đánh giá không thuộc về người dùng hiện tại",    HttpStatus.FORBIDDEN,             ErrorType.BUSINESS),
    REVIEW_CANNOT_EDIT          (4706, "Đánh giá không thể chỉnh sửa",                    HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    REVIEW_AI_REJECTED          (4707, "Bình luận chứa nội dung không phù hợp và bị từ chối", HttpStatus.FORBIDDEN,             ErrorType.BUSINESS),

    NOTIFICATION_NOT_FOUND      (4708, "Không tìm thấy thông báo",                         HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    NOTIFICATION_SEND_FAILED    (4709, "Gửi thông báo thất bại",                           HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.TECHNICAL),
    NOTIFICATION_INVALID_TYPE   (4710, "Loại thông báo không hợp lệ",                      HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),

    // ===================== SUPPORTING =====================
    USER_COUPON_NOT_FOUND       (4901, "Không tìm thấy mã giảm giá của người dùng",        HttpStatus.NOT_FOUND,             ErrorType.BUSINESS),
    USER_COUPON_ALREADY_EXISTS  (4902, "Người dùng đã có mã giảm giá này",                 HttpStatus.CONFLICT,              ErrorType.BUSINESS),
    USER_COUPON_EXPIRED         (4903, "Mã giảm giá của người dùng đã hết hạn",            HttpStatus.BAD_REQUEST,           ErrorType.BUSINESS),
    USER_COUPON_ALREADY_USED    (4904, "Mã giảm giá của người dùng đã được sử dụng",       HttpStatus.CONFLICT,              ErrorType.BUSINESS);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
    private final ErrorType type;

    ErrorCode(int code, String message, HttpStatus httpStatus, ErrorType type) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
        this.type = type;
    }
}