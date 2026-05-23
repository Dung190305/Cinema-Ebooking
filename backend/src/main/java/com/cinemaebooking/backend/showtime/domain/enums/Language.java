package com.cinemaebooking.backend.showtime.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

/**
 * Language: Enum định nghĩa các ngôn ngữ sử dụng trong hệ thống.
 *
 * <p>
 * Chịu trách nhiệm:
 * <ul>
 *     <li>Định nghĩa danh sách các ngôn ngữ hỗ trợ (EN, VI, JA, KO, ZH, FR, DE, ES, ...)</li>
 *     <li>Dùng cho audioLanguage và subtitleLanguage trong Showtime</li>
 * </ul>
 *
 * <p>
 * Lưu ý:
 * <ul>
 *     <li>Sử dụng mã ngôn ngữ dạng ISO đơn giản (EN, VI,...)</li>
 *     <li>displayName dùng để hiển thị UI</li>
 *     <li>Phù hợp cho MVP, nếu cần dynamic có thể chuyển sang entity</li>
 * </ul>
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Getter
public enum Language {

    /**
     * Tiếng Anh
     */
    EN("English"),

    /**
     * Tiếng Việt
     */
    VI("Vietnamese"),

    /**
     * Tiếng Nhật
     */
    JA("Japanese"),

    /**
     * Tiếng Hàn
     */
    KO("Korean"),

    /**
     * Tiếng Trung (Phổ thông)
     */
    ZH("Chinese"),

    /**
     * Tiếng Pháp
     */
    FR("French"),

    /**
     * Tiếng Đức
     */
    DE("German"),

    /**
     * Tiếng Tây Ban Nha
     */
    ES("Spanish"),

    /**
     * Tiếng Thái
     */
    TH("Thai"),

    /**
     * Tiếng Nga
     */
    RU("Russian"),

    /**
     * Tiếng Ý
     */
    IT("Italian"),

    /**
     * Tiếng Bồ Đào Nha
     */
    PT("Portuguese");

    /**
     * Tên hiển thị của ngôn ngữ
     */
    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

}