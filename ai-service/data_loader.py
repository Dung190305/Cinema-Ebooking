import os
from pathlib import Path

import pandas as pd
from sqlalchemy import create_engine
from dotenv import load_dotenv


load_dotenv()


# ==============================
# Database connection
# ==============================
def get_engine():
    """
    Tạo kết nối tới MySQL.

    Ưu tiên đọc biến môi trường:
    DB_HOST
    DB_PORT
    DB_NAME
    DB_USERNAME hoặc DB_USER
    DB_PASSWORD
    """

    host = os.getenv("DB_HOST", "localhost")
    port = os.getenv("DB_PORT", "3306")
    db_name = os.getenv("DB_NAME", "cinema_ebooking_db")

    user = os.getenv("DB_USERNAME") or os.getenv("DB_USER") or "root"
    password = os.getenv("DB_PASSWORD", "")

    url = f"mysql+pymysql://{user}:{password}@{host}:{port}/{db_name}?charset=utf8mb4"

    return create_engine(url)


# ==============================
# Common SQL conditions
# ==============================
def active_movie_condition(alias: str = "m") -> str:
    """
    Database hiện tại không có movies.status.

    Phim còn hoạt động được hiểu là:
    - deleted = false
    - showing_end_date IS NULL hoặc showing_end_date >= hôm nay

    Nếu showing_end_date < CURDATE() thì xem như phim đã dừng hoạt động,
    AI không được train/gợi ý phim đó.
    """

    return f"""
    {alias}.deleted = false
    AND (
        {alias}.showing_end_date IS NULL
        OR {alias}.showing_end_date >= CURDATE()
    )
    """


# ==============================
# Load movies for Content-Based
# ==============================
def load_movies():
    """
    Load danh sách phim còn hoạt động từ database.

    Dùng cho Content-Based Filtering:
    - title
    - description
    - director
    - actors
    - age_rating
    - genres

    Không lấy phim đã dừng hoạt động:
    - deleted = true
    - showing_end_date < CURDATE()
    """

    engine = get_engine()

    query = f"""
    SELECT
        m.id AS movie_id,
        m.title,
        COALESCE(m.description, '') AS description,
        COALESCE(m.director, '') AS director,
        COALESCE(m.actors, '') AS actors,
        COALESCE(m.age_rating, '') AS age_rating,
        COALESCE(GROUP_CONCAT(g.name SEPARATOR ' '), '') AS genres
    FROM movies m
    LEFT JOIN movie_genres mg
        ON m.id = mg.movie_id
    LEFT JOIN genres g
        ON mg.genre_id = g.id
    WHERE {active_movie_condition("m")}
    GROUP BY
        m.id,
        m.title,
        m.description,
        m.director,
        m.actors,
        m.age_rating
    """

    df = pd.read_sql(query, engine)

    if df.empty:
        print("Loaded active movies for AI: 0")
        return pd.DataFrame(
            columns=[
                "movie_id",
                "title",
                "description",
                "director",
                "actors",
                "age_rating",
                "genres",
                "content",
            ]
        )

    df["movie_id"] = df["movie_id"].astype(int)

    df["title"] = df["title"].fillna("")
    df["description"] = df["description"].fillna("")
    df["director"] = df["director"].fillna("")
    df["actors"] = df["actors"].fillna("")
    df["age_rating"] = df["age_rating"].fillna("")
    df["genres"] = df["genres"].fillna("")

    df["content"] = (
        df["title"] + " " +
        df["description"] + " " +
        df["director"] + " " +
        df["actors"] + " " +
        df["age_rating"] + " " +
        df["genres"]
    )

    print(f"Loaded active movies for AI: {len(df)}")

    return df


# ==============================
# Load active movie ids
# ==============================
def load_active_movie_ids():
    """
    Lấy danh sách movie_id còn hoạt động.

    Dùng để:
    - Lọc dataset MovieLens đã convert.
    - Tránh train Collaborative/SVD trên phim đã dừng hoạt động.
    """

    engine = get_engine()

    query = f"""
    SELECT
        m.id AS movie_id
    FROM movies m
    WHERE {active_movie_condition("m")}
    """

    try:
        df = pd.read_sql(query, engine)

        if df.empty:
            return set()

        return set(int(movie_id) for movie_id in df["movie_id"].dropna().tolist())

    except Exception as e:
        print("Load active movie ids failed:")
        print(e)

        return set()


# ==============================
# Load user interactions from DB
# ==============================
def load_user_interactions(user_id: int):
    """
    Load interaction thật của 1 user từ database.

    Quy tắc trọng số:
    - Booking CONFIRMED nhưng chưa đánh giá  => +5
    - Rating > 6                             => +8
    - Rating = 5 hoặc 6                      => +5
    - Rating < 5                             => -3
    - Booking CANCELLED                      => 0

    Lưu ý:
    - Nếu phim đã có rating thì lấy trọng số theo rating.
    - Không cộng chồng booking + rating.
    - Không dùng showtimes.status vì database không có cột này.
    - Chỉ lấy suất chiếu chưa bị hủy: s.cancelled = false.
    - Chỉ lấy phim còn hoạt động theo showing_end_date.
    """

    engine = get_engine()

    query = f"""
    SELECT
        b.user_id,
        s.movie_id,

        CASE
            WHEN MAX(r.rating) IS NOT NULL AND MAX(r.rating) > 6 THEN 8
            WHEN MAX(r.rating) IS NOT NULL AND MAX(r.rating) BETWEEN 5 AND 6 THEN 5
            WHEN MAX(r.rating) IS NOT NULL AND MAX(r.rating) < 5 THEN -3

            WHEN SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END) > 0 THEN 5

            ELSE 0
        END AS weight,

        CASE
            WHEN MAX(r.rating) IS NOT NULL THEN CONCAT('RATING_', MAX(r.rating))
            WHEN SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END) > 0 THEN 'BOOKING_CONFIRMED'
            WHEN SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) > 0 THEN 'BOOKING_CANCELLED'
            ELSE 'NO_SIGNAL'
        END AS action_type

    FROM bookings b
    JOIN showtimes s
        ON b.showtime_id = s.id
    JOIN movies m
        ON s.movie_id = m.id

    LEFT JOIN reviews r
        ON r.user_id = b.user_id
       AND r.movie_id = s.movie_id
       AND r.deleted = false
       AND r.rating IS NOT NULL

    WHERE b.user_id = {int(user_id)}
      AND b.status IN ('CONFIRMED', 'CANCELLED')
      AND b.deleted = false
      AND s.deleted = false
      AND s.cancelled = false
      AND s.movie_id IS NOT NULL
      AND {active_movie_condition("m")}

    GROUP BY
        b.user_id,
        s.movie_id

    HAVING weight != 0
    """

    try:
        interactions = pd.read_sql(query, engine)

        if not interactions.empty:
            interactions["user_id"] = interactions["user_id"].astype(int)
            interactions["movie_id"] = interactions["movie_id"].astype(int)
            interactions["weight"] = interactions["weight"].astype(float)

        return interactions

    except Exception as e:
        print("Load user interactions failed:")
        print(e)

        return pd.DataFrame(
            columns=["user_id", "movie_id", "weight", "action_type"]
        )


# ==============================
# Load booked movies to exclude
# ==============================
def load_user_booked_movie_ids(user_id: int):
    """
    Lấy danh sách movie_id mà user đã đặt vé thành công.

    Những phim này sẽ bị loại khỏi danh sách gợi ý.

    Chỉ xét:
    - Booking CONFIRMED
    - Showtime chưa bị hủy
    - Phim còn hoạt động
    """

    engine = get_engine()

    query = f"""
    SELECT DISTINCT
        s.movie_id
    FROM bookings b
    JOIN showtimes s
        ON b.showtime_id = s.id
    JOIN movies m
        ON s.movie_id = m.id
    WHERE b.user_id = {int(user_id)}
      AND b.status = 'CONFIRMED'
      AND b.deleted = false
      AND s.deleted = false
      AND s.cancelled = false
      AND s.movie_id IS NOT NULL
      AND {active_movie_condition("m")}
    """

    try:
        df = pd.read_sql(query, engine)

        if not df.empty:
            df["movie_id"] = df["movie_id"].astype(int)

        return df

    except Exception as e:
        print("Load user booked movie ids failed:")
        print(e)

        return pd.DataFrame(columns=["movie_id"])


# ==============================
# Load dataset interactions
# ==============================
def load_dataset_interactions():
    """
    Đọc dataset interaction từ file CSV đã convert.

    File:
    ai-service/datasets/user_movie_dataset.csv

    Cột cần có:
    - user_id
    - movie_id
    - weight
    - action_type

    Dataset này dùng để bổ sung dữ liệu cho:
    - Collaborative Filtering
    - Truncated SVD

    Dataset không thay thế dữ liệu thật của hệ thống.
    """

    dataset_path = Path(__file__).parent / "datasets" / "user_movie_dataset.csv"

    if not dataset_path.exists():
        print("Dataset file not found:", dataset_path)

        return pd.DataFrame(
            columns=["user_id", "movie_id", "weight", "action_type"]
        )

    try:
        df = pd.read_csv(dataset_path)

        required_columns = {"user_id", "movie_id", "weight"}

        if not required_columns.issubset(df.columns):
            print("Dataset missing required columns:", required_columns)

            return pd.DataFrame(
                columns=["user_id", "movie_id", "weight", "action_type"]
            )

        if "action_type" not in df.columns:
            df["action_type"] = "DATASET"

        df = df[["user_id", "movie_id", "weight", "action_type"]]

        df = df.dropna(subset=["user_id", "movie_id", "weight"])

        df["user_id"] = df["user_id"].astype(int)
        df["movie_id"] = df["movie_id"].astype(int)
        df["weight"] = df["weight"].astype(float)

        print(f"Loaded dataset interactions: {len(df)} rows")

        return df

    except Exception as e:
        print("Load dataset interactions failed:")
        print(e)

        return pd.DataFrame(
            columns=["user_id", "movie_id", "weight", "action_type"]
        )


# ==============================
# Load all interactions for training
# ==============================
def load_all_interactions():
    """
    Lấy toàn bộ dữ liệu interaction để train:
    - Item-Item Collaborative Filtering
    - Truncated SVD

    Nguồn dữ liệu:
    1. Database thật: bookings + reviews
    2. Dataset CSV: datasets/user_movie_dataset.csv

    Quy tắc trọng số database:
    - Booking CONFIRMED nhưng chưa đánh giá  => +5
    - Rating > 6                             => +8
    - Rating = 5 hoặc 6                      => +5
    - Rating < 5                             => -3
    - Booking CANCELLED                      => 0

    Chỉ train trên:
    - Phim còn hoạt động.
    - Showtime chưa bị hủy.
    """

    engine = get_engine()

    query = f"""
    SELECT
        b.user_id,
        s.movie_id,

        CASE
            WHEN MAX(r.rating) IS NOT NULL AND MAX(r.rating) > 6 THEN 8
            WHEN MAX(r.rating) IS NOT NULL AND MAX(r.rating) BETWEEN 5 AND 6 THEN 5
            WHEN MAX(r.rating) IS NOT NULL AND MAX(r.rating) < 5 THEN -3

            WHEN SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END) > 0 THEN 5

            ELSE 0
        END AS weight,

        CASE
            WHEN MAX(r.rating) IS NOT NULL THEN CONCAT('RATING_', MAX(r.rating))
            WHEN SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END) > 0 THEN 'BOOKING_CONFIRMED'
            WHEN SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) > 0 THEN 'BOOKING_CANCELLED'
            ELSE 'NO_SIGNAL'
        END AS action_type

    FROM bookings b
    JOIN showtimes s
        ON b.showtime_id = s.id
    JOIN movies m
        ON s.movie_id = m.id

    LEFT JOIN reviews r
        ON r.user_id = b.user_id
       AND r.movie_id = s.movie_id
       AND r.deleted = false
       AND r.rating IS NOT NULL

    WHERE b.status IN ('CONFIRMED', 'CANCELLED')
      AND b.deleted = false
      AND s.deleted = false
      AND s.cancelled = false
      AND s.movie_id IS NOT NULL
      AND {active_movie_condition("m")}

    GROUP BY
        b.user_id,
        s.movie_id

    HAVING weight != 0
    """

    try:
        db_interactions = pd.read_sql(query, engine)

        if not db_interactions.empty:
            db_interactions["user_id"] = db_interactions["user_id"].astype(int)
            db_interactions["movie_id"] = db_interactions["movie_id"].astype(int)
            db_interactions["weight"] = db_interactions["weight"].astype(float)

    except Exception as e:
        print("Load database interactions failed:")
        print(e)

        db_interactions = pd.DataFrame(
            columns=["user_id", "movie_id", "weight", "action_type"]
        )

    dataset_interactions = load_dataset_interactions()

    # Lọc dataset theo phim còn hoạt động trong database.
    active_movie_ids = load_active_movie_ids()

    if not dataset_interactions.empty:
        before_count = len(dataset_interactions)

        if active_movie_ids:
            dataset_interactions = dataset_interactions[
                dataset_interactions["movie_id"].isin(active_movie_ids)
            ]
        else:
            dataset_interactions = dataset_interactions.iloc[0:0]

        after_count = len(dataset_interactions)

        print(
            "Dataset interactions filtered by active movies: "
            f"{before_count} -> {after_count}"
        )

    all_interactions = pd.concat(
        [db_interactions, dataset_interactions],
        ignore_index=True
    )

    if all_interactions.empty:
        print("No interactions found for training.")

        return pd.DataFrame(
            columns=["user_id", "movie_id", "weight", "action_type"]
        )

    all_interactions = all_interactions.dropna(
        subset=["user_id", "movie_id", "weight"]
    )

    all_interactions["user_id"] = all_interactions["user_id"].astype(int)
    all_interactions["movie_id"] = all_interactions["movie_id"].astype(int)
    all_interactions["weight"] = all_interactions["weight"].astype(float)

    print(f"Database interactions: {len(db_interactions)}")
    print(f"Dataset interactions: {len(dataset_interactions)}")
    print(f"Total interactions for training: {len(all_interactions)}")

    return all_interactions


# ==============================
# Load hot movies for popular fallback
# ==============================
def load_hot_movies():
    """
    Lấy danh sách phim đang hot cho popular_fallback.

    Tính dựa trên:
    - booking_count: số lượt đặt vé thành công
    - avg_rating: điểm đánh giá trung bình
    - review_count: số lượt đánh giá

    Chỉ lấy:
    - Phim còn hoạt động.
    - Showtime chưa bị hủy.

    Công thức:
    hot_score = booking_count * 0.5
              + avg_rating * 0.3
              + review_count * 0.2
    """

    engine = get_engine()

    query = f"""
    SELECT
        m.id AS movie_id,
        m.title,

        COUNT(DISTINCT CASE
            WHEN b.status = 'CONFIRMED' AND b.deleted = false
            THEN b.id
        END) AS booking_count,

        COALESCE(AVG(CASE
            WHEN r.deleted = false
            THEN r.rating
        END), 0) AS avg_rating,

        COUNT(DISTINCT CASE
            WHEN r.deleted = false
            THEN r.id
        END) AS review_count,

        (
            COUNT(DISTINCT CASE
                WHEN b.status = 'CONFIRMED' AND b.deleted = false
                THEN b.id
            END) * 0.5

            + COALESCE(AVG(CASE
                WHEN r.deleted = false
                THEN r.rating
            END), 0) * 0.3

            + COUNT(DISTINCT CASE
                WHEN r.deleted = false
                THEN r.id
            END) * 0.2
        ) AS hot_score

    FROM movies m

    LEFT JOIN showtimes s
        ON s.movie_id = m.id
       AND s.deleted = false
       AND s.cancelled = false

    LEFT JOIN bookings b
        ON b.showtime_id = s.id
       AND b.deleted = false

    LEFT JOIN reviews r
        ON r.movie_id = m.id
       AND r.deleted = false

    WHERE {active_movie_condition("m")}

    GROUP BY
        m.id,
        m.title

    ORDER BY
        hot_score DESC,
        booking_count DESC,
        avg_rating DESC,
        review_count DESC,
        m.id DESC
    """

    try:
        df = pd.read_sql(query, engine)

        if not df.empty:
            df["movie_id"] = df["movie_id"].astype(int)
            df["booking_count"] = df["booking_count"].astype(int)
            df["avg_rating"] = df["avg_rating"].astype(float)
            df["review_count"] = df["review_count"].astype(int)
            df["hot_score"] = df["hot_score"].astype(float)

        return df

    except Exception as e:
        print("Load hot movies failed:")
        print(e)

        return pd.DataFrame(
            columns=[
                "movie_id",
                "title",
                "booking_count",
                "avg_rating",
                "review_count",
                "hot_score",
            ]
        )