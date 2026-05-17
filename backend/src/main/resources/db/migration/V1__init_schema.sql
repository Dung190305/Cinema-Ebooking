-- ============================================================
-- V1__init_schema.sql
-- Cinema E-Booking — Full Schema Init
-- ============================================================

-- ================== CINEMAS ==================
CREATE TABLE IF NOT EXISTS cinemas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT,

    -- Generated columns
    name_active VARCHAR(255)
        GENERATED ALWAYS AS (IF(deleted = FALSE, name, NULL)) VIRTUAL,

    address_city_active VARCHAR(512)
        GENERATED ALWAYS AS (
            IF(deleted = FALSE, CONCAT(address, '_', city), NULL)
        ) VIRTUAL,

    PRIMARY KEY (id),
    UNIQUE (name_active),
    UNIQUE (address_city_active),
    INDEX idx_cinemas_deleted (deleted)
);

-- ================== ROOMS ==================
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    cinema_id BIGINT NOT NULL,

    number_of_rows INT NOT NULL,
    number_of_cols INT NOT NULL,
    total_seats INT NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT,

    room_name_active VARCHAR(255)
        GENERATED ALWAYS AS (
            IF(deleted = FALSE, CONCAT(cinema_id, '_', name), NULL)
        ) VIRTUAL,

    PRIMARY KEY (id),
    UNIQUE (room_name_active),
    FOREIGN KEY (cinema_id) REFERENCES cinemas(id),
    INDEX idx_rooms_deleted (deleted)
);

-- ================== SEAT TYPES ==================
CREATE TABLE IF NOT EXISTS seat_types (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255)    NOT NULL,
    base_price  BIGINT          NOT NULL,

    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    deleted_at  DATETIME,
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     BIGINT,

    -- 🔥 Generated column thay cho (name, deleted)
    name_active VARCHAR(255)
        GENERATED ALWAYS AS (
            IF(deleted = FALSE, name, NULL)
        ) VIRTUAL,

    PRIMARY KEY (id),

    -- ✅ Unique mới
    UNIQUE (name_active),

    -- ✅ Index hỗ trợ filter
    INDEX idx_seat_types_deleted (deleted)
);

-- ================== SEATS ==================
CREATE TABLE IF NOT EXISTS seats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    row_index INT NOT NULL,
    col_index INT NOT NULL,
    label VARCHAR(20) NOT NULL,

    room_id BIGINT NOT NULL,
    seat_type_id BIGINT,

    status VARCHAR(30) NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT,

    -- 🔥 chính là V2 gộp vào
    active_position TINYINT
        GENERATED ALWAYS AS (
            IF(deleted = FALSE, 1, NULL)
        ) VIRTUAL,

    PRIMARY KEY (id),

    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (seat_type_id) REFERENCES seat_types(id),

    UNIQUE (room_id, row_index, col_index, active_position),

    INDEX idx_seats_deleted (deleted)
);

-- ================== USERS ==================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT,

    email_active VARCHAR(255)
        GENERATED ALWAYS AS (IF(deleted = FALSE, email, NULL)) VIRTUAL,

    PRIMARY KEY (id),
    UNIQUE (email_active),
    INDEX idx_users_deleted (deleted)
);

-- ================== MEMBERSHIP TIERS ==================
CREATE TABLE IF NOT EXISTS membership_tiers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    min_spending_required DECIMAL(15,2) NOT NULL,
    discount_percent DECIMAL(5,2),
    benefits_description VARCHAR(500),
    tier_level INT NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT,

    PRIMARY KEY (id),
    UNIQUE (name),

    INDEX idx_membership_tiers_deleted (deleted)
);

-- ================== LOYALTY ACCOUNTS ==================
CREATE TABLE IF NOT EXISTS loyalty_accounts (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    loyalty_number      VARCHAR(50),
    total_spending      DECIMAL(15,2)   NOT NULL DEFAULT 0,
    lifetime_points     DECIMAL(12,2)   NOT NULL DEFAULT 0,
    current_points      DECIMAL(12,2)   NOT NULL DEFAULT 0,
    membership_tier_id  BIGINT,
    last_activity_date  DATETIME,
    joined_date         DATETIME,
    status              VARCHAR(30)     NOT NULL,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    deleted_at          DATETIME,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_loyalty_accounts_user_id
        UNIQUE (user_id),
    CONSTRAINT uk_loyalty_accounts_loyalty_number
        UNIQUE (loyalty_number),
    CONSTRAINT fk_loyalty_accounts_tier
        FOREIGN KEY (membership_tier_id) REFERENCES membership_tiers (id)
);

-- ================== LOYALTY EARNING RULES ==================
CREATE TABLE IF NOT EXISTS loyalty_earning_rules (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    membership_tier_id  BIGINT          NOT NULL,
    earning_type        VARCHAR(100)    NOT NULL,
    multiplier          DECIMAL(5,4)    NOT NULL,
    fixed_points        DECIMAL(12,2),
    description         VARCHAR(500),
    conditions          VARCHAR(1000),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    priority            INT             NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    deleted_at          DATETIME,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_loyalty_earning_rules_tier
        FOREIGN KEY (membership_tier_id) REFERENCES membership_tiers (id)
);

-- ================== COUPONS ==================
CREATE TABLE IF NOT EXISTS coupons (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    code                    VARCHAR(100)    NOT NULL,
    type                    VARCHAR(20)     NOT NULL,
    discount_value          DECIMAL(10,2)   NOT NULL,
    usage_limit             INT             NOT NULL,
    per_user_usage          INT             NOT NULL,
    points_to_redeem        INT             NOT NULL,
    minimum_booking_value   DECIMAL(12,2),
    maximum_discount_amount DECIMAL(12,2),
    start_date              DATE            NOT NULL,
    end_date                DATE            NOT NULL,
    created_at              DATETIME        NOT NULL,
    updated_at              DATETIME        NOT NULL,
    deleted_at              DATETIME,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_coupons_code_deleted
        UNIQUE (code, deleted)
);

-- ================== USER COUPONS ==================
CREATE TABLE IF NOT EXISTS user_coupons (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    user_id         BIGINT      NOT NULL,
    coupon_id       BIGINT      NOT NULL,
    received_at     DATETIME    NOT NULL,
    usage_remain    INT         NOT NULL,
    used_at         DATETIME,
    expired_at      DATETIME    NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      DATETIME    NOT NULL,
    updated_at      DATETIME    NOT NULL,
    deleted_at      DATETIME,
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    version         BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_coupons_user_id_coupon_id_deleted
        UNIQUE (user_id, coupon_id, deleted)
);

-- ================== COMBOS ==================
CREATE TABLE IF NOT EXISTS combos (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(150)    NOT NULL,
    description     VARCHAR(500),
    price           DECIMAL(12,2)   NOT NULL,
    original_price  DECIMAL(12,2),
    image_url       VARCHAR(500),
    status          VARCHAR(30)     NOT NULL,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    deleted_at      DATETIME,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_combos_name_deleted
        UNIQUE (name, deleted)
);

-- ================== GENRES ==================
CREATE TABLE IF NOT EXISTS genres (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    deleted_at  DATETIME,
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_genres_name_deleted
        UNIQUE (name, deleted)
);

-- ================== MOVIES ==================
CREATE TABLE IF NOT EXISTS movies (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    title           VARCHAR(255)    NOT NULL,
    description     VARCHAR(1000),
    duration        INT             NOT NULL,
    age_rating      VARCHAR(10)     NOT NULL,
    release_date    DATE            NOT NULL,
    status          VARCHAR(20)     NOT NULL,
    poster_url      VARCHAR(500),
    banner_url      VARCHAR(500),
    director        VARCHAR(255),
    actors          VARCHAR(500),
    rating          DOUBLE          NOT NULL DEFAULT 0,
    rating_count    INT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    deleted_at      DATETIME,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_movies_title_deleted
        UNIQUE (title, deleted)
);

-- ================== MOVIE GENRES (join table) ==================
CREATE TABLE IF NOT EXISTS movie_genres (
    movie_id    BIGINT NOT NULL,
    genre_id    BIGINT NOT NULL,
    PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT fk_movie_genres_movie
        FOREIGN KEY (movie_id) REFERENCES movies (id),
    CONSTRAINT fk_movie_genres_genre
        FOREIGN KEY (genre_id) REFERENCES genres (id)
);

-- ================== SHOWTIME FORMATS ==================
CREATE TABLE IF NOT EXISTS showtime_formats (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL,
    extra_price BIGINT      NOT NULL,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME    NOT NULL,
    deleted_at  DATETIME,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    version     BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_showtime_formats_name_deleted
        UNIQUE (name, deleted)
);

-- ================== SHOWTIMES ==================
CREATE TABLE IF NOT EXISTS showtimes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,

    movie_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    format_id BIGINT NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT,

    showtime_active_key VARCHAR(255)
        GENERATED ALWAYS AS (
            IF(deleted = FALSE, CONCAT(room_id, '_', start_time), NULL)
        ) VIRTUAL,

    PRIMARY KEY (id),
    UNIQUE (showtime_active_key),

    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (format_id) REFERENCES showtime_formats(id),

    INDEX idx_showtimes_deleted (deleted)
);

-- ================== SHOWTIME SEATS ==================
CREATE TABLE IF NOT EXISTS showtime_seats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seat_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT,

    showtime_seat_active VARCHAR(255)
        GENERATED ALWAYS AS (
            IF(deleted = FALSE, CONCAT(showtime_id, '_', seat_id), NULL)
        ) VIRTUAL,

    PRIMARY KEY (id),
    UNIQUE (showtime_seat_active),

    FOREIGN KEY (seat_id) REFERENCES seats(id),
    FOREIGN KEY (showtime_id) REFERENCES showtimes(id),

    INDEX idx_showtime_seats_deleted (deleted)
);

-- ================== BOOKINGS ==================
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_code VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,

    final_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT,

    booking_code_active VARCHAR(100)
        GENERATED ALWAYS AS (
            IF(deleted = FALSE, booking_code, NULL)
        ) VIRTUAL,

    PRIMARY KEY (id),
    UNIQUE (booking_code_active),

    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_bookings_deleted (deleted)
);

-- ================== BOOKING COMBOS ==================
CREATE TABLE IF NOT EXISTS booking_combos (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id  BIGINT          NOT NULL,
    combo_id    BIGINT          NOT NULL,
    combo_name  VARCHAR(150)    NOT NULL,
    unit_price  DECIMAL(12,2)   NOT NULL,
    quantity    INT             NOT NULL,
    total_price DECIMAL(12,2)   NOT NULL,
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    deleted_at  DATETIME,
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_booking_combos_booking_id_combo_id_deleted
        UNIQUE (booking_id, combo_id, deleted),
    CONSTRAINT fk_booking_combos_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id)
);

-- ================== BOOKING COUPONS ==================
CREATE TABLE IF NOT EXISTS booking_coupons (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id      BIGINT          NOT NULL,
    user_coupon_id  BIGINT          NOT NULL,
    coupon_code     VARCHAR(50)     NOT NULL,
    discount_amount DECIMAL(12,2)   NOT NULL,
    applied_at      DATETIME        NOT NULL,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    deleted_at      DATETIME,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_booking_coupons_booking_id_user_coupon_id_deleted
        UNIQUE (booking_id, user_coupon_id, deleted),
    CONSTRAINT fk_booking_coupons_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id),
    CONSTRAINT fk_booking_coupons_user_coupon
        FOREIGN KEY (user_coupon_id) REFERENCES user_coupons (id)
);

-- ================== PAYMENTS ==================
CREATE TABLE IF NOT EXISTS payments (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL,
    amount              DECIMAL(12,2)   NOT NULL,
    method              VARCHAR(20)     NOT NULL,
    status              VARCHAR(20)     NOT NULL,
    transaction_id      VARCHAR(100),
    provider_response   TEXT,
    paid_at             DATETIME,
    payment_code        VARCHAR(100)    NOT NULL,
    expired_at          DATETIME        NOT NULL,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    deleted_at          DATETIME,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_payments_transaction_id_deleted
        UNIQUE (transaction_id, deleted),
    CONSTRAINT uk_payments_payment_code_deleted
        UNIQUE (payment_code, deleted),
    CONSTRAINT fk_payments_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id)
);

-- ================== REFUNDS ==================
CREATE TABLE IF NOT EXISTS refunds (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL,
    original_amount     DECIMAL(12,2)   NOT NULL,
    refund_amount       DECIMAL(12,2)   NOT NULL,
    refund_percentage   INT             NOT NULL,
    status              VARCHAR(20)     NOT NULL,
    requested_at        DATETIME        NOT NULL,
    processed_at        DATETIME,
    reason              VARCHAR(500),
    admin_note          VARCHAR(500),
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    deleted_at          DATETIME,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_refunds_booking_id_deleted
        UNIQUE (booking_id, deleted),
    CONSTRAINT fk_refunds_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id)
);

-- ================== TICKETS ==================
CREATE TABLE IF NOT EXISTS tickets (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL,
    showtime_seat_id    BIGINT          NOT NULL,
    ticket_code         VARCHAR(100)    NOT NULL,
    price               DECIMAL(12,2)   NOT NULL,
    seat_number         VARCHAR(10)     NOT NULL,
    seat_type           VARCHAR(30)     NOT NULL,
    status              VARCHAR(20)     NOT NULL,
    checked_in_at       DATETIME,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    deleted_at          DATETIME,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_tickets_showtime_seat_id_deleted
        UNIQUE (showtime_seat_id, deleted),
    CONSTRAINT uk_tickets_ticket_code_deleted
        UNIQUE (ticket_code, deleted),
    CONSTRAINT fk_tickets_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id),
    CONSTRAINT fk_tickets_showtime_seat
        FOREIGN KEY (showtime_seat_id) REFERENCES showtime_seats (id)
);

-- ================== SEAT LOCKS ==================
CREATE TABLE IF NOT EXISTS seat_locks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    showtime_seat_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,

    expired_at DATETIME NOT NULL,

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    seat_lock_active BIGINT
        GENERATED ALWAYS AS (
            IF(deleted = FALSE, showtime_seat_id, NULL)
        ) VIRTUAL,

    PRIMARY KEY (id),
    UNIQUE (seat_lock_active),

    FOREIGN KEY (showtime_seat_id) REFERENCES showtime_seats(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- ================== NOTIFICATIONS ==================
CREATE TABLE IF NOT EXISTS notifications (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    title       VARCHAR(200)    NOT NULL,
    message     VARCHAR(1000)   NOT NULL,
    type        VARCHAR(30)     NOT NULL,
    is_read     BOOLEAN         NOT NULL,
    booking_id  BIGINT,
    payment_id  BIGINT,
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    deleted_at  DATETIME,
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    version     BIGINT,
    PRIMARY KEY (id),
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_user_id_is_read (user_id, is_read),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_notifications_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id),
    CONSTRAINT fk_notifications_payment
        FOREIGN KEY (payment_id) REFERENCES payments (id)
);

-- ================== REVIEWS ==================
CREATE TABLE IF NOT EXISTS reviews (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    movie_id    BIGINT      NOT NULL,
    booking_id  BIGINT      NOT NULL,
    rating      INT         NOT NULL,
    comment     TEXT        NOT NULL,
    sentiment   VARCHAR(20) NOT NULL,
    edited_at   DATETIME,
    status      VARCHAR(20) NOT NULL,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME    NOT NULL,
    deleted_at  DATETIME,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    version     BIGINT,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT chk_reviews_rating CHECK (rating >= 1 AND rating <= 10),

    PRIMARY KEY (id),
    INDEX idx_reviews_movie_id (movie_id),
    INDEX idx_reviews_user_id (user_id),
    INDEX idx_reviews_booking_id (booking_id),
    INDEX idx_reviews_status (status),
    INDEX idx_reviews_deleted (deleted),

    -- Mỗi user chỉ review 1 phim 1 lần (soft-delete aware)
    UNIQUE KEY uk_reviews_user_movie_deleted (user_id, movie_id, deleted),
    -- Mỗi booking chỉ review 1 lần (soft-delete aware)
    UNIQUE KEY uk_reviews_booking_deleted (booking_id, deleted)
);

-- ================== LOYALTY TRANSACTIONS ==================
CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    loyalty_account_id  BIGINT      NOT NULL,
    type                VARCHAR(30) NOT NULL,
    change_point        INT         NOT NULL,
    balance_after       INT         NOT NULL,
    booking_id          BIGINT,
    payment_id          BIGINT,
    coupon_id           BIGINT,
    user_coupon_id      BIGINT,
    change_date         DATETIME    NOT NULL,
    created_at          DATETIME    NOT NULL,
    updated_at          DATETIME    NOT NULL,
    deleted_at          DATETIME,
    deleted             BOOLEAN     NOT NULL DEFAULT FALSE,
    version             BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_loyalty_transactions_payment_id_type_deleted
        UNIQUE (payment_id, type, deleted),
    CONSTRAINT uk_loyalty_transactions_booking_id_type_deleted
        UNIQUE (booking_id, type, deleted),
    CONSTRAINT uk_loyalty_transactions_user_coupon_id_type_deleted
        UNIQUE (user_coupon_id, type, deleted),
    CONSTRAINT fk_loyalty_transactions_account
        FOREIGN KEY (loyalty_account_id) REFERENCES loyalty_accounts (id),
    CONSTRAINT fk_loyalty_transactions_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id),
    CONSTRAINT fk_loyalty_transactions_payment
        FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_loyalty_transactions_coupon
        FOREIGN KEY (coupon_id) REFERENCES coupons (id),
    CONSTRAINT fk_loyalty_transactions_user_coupon
        FOREIGN KEY (user_coupon_id) REFERENCES user_coupons (id)
);