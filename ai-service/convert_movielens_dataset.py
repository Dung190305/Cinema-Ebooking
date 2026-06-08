from pathlib import Path
import pandas as pd


BASE_DIR = Path(__file__).parent

RATINGS_PATH = BASE_DIR / "datasets_raw" / "ratings.csv"
MOVIES_PATH = BASE_DIR / "datasets_raw" / "movies.csv"

OUTPUT_DIR = BASE_DIR / "datasets"
OUTPUT_PATH = OUTPUT_DIR / "user_movie_dataset.csv"

# Hệ thống Cinema hiện tại của bạn đang có 10 phim local
LOCAL_MOVIE_COUNT = 10

# Dùng 50.000 dòng là đủ cho demo AI, SVD, Collaborative
MAX_ROWS = 50000


def convert_rating_to_10_scale(rating_5):
    """
    MovieLens dùng rating 0.5 -> 5.0.
    Hệ thống của bạn dùng rating 1 -> 10.
    """
    return float(rating_5) * 2


def rating_to_weight(rating_10):
    """
    Rule AI hiện tại:
    - Rating > 6        => +8
    - Rating = 5 hoặc 6 => +5
    - Rating < 5        => -3
    """
    if rating_10 > 6:
        return 8

    if rating_10 == 5 or rating_10 == 6:
        return 5

    return -3


def map_movielens_movie_to_local(movie_id):
    """
    MovieLens movieId không trùng với movie_id trong database Cinema.

    Cách map mô phỏng:
    MovieLens movieId -> movie_id local từ 1 đến 10
    """
    return ((int(movie_id) - 1) % LOCAL_MOVIE_COUNT) + 1


def main():
    if not RATINGS_PATH.exists():
        raise FileNotFoundError(f"Không tìm thấy ratings.csv tại: {RATINGS_PATH}")

    ratings_df = pd.read_csv(RATINGS_PATH)

    print("Ratings columns:", ratings_df.columns.tolist())
    print("Ratings rows before:", len(ratings_df))

    required_columns = ["userId", "movieId", "rating"]

    for col in required_columns:
        if col not in ratings_df.columns:
            raise ValueError(f"Thiếu cột bắt buộc trong ratings.csv: {col}")

    ratings_df = ratings_df[required_columns].dropna()

    # Lấy mẫu để không làm AI train quá lâu
    if len(ratings_df) > MAX_ROWS:
        ratings_df = ratings_df.sample(
            n=MAX_ROWS,
            random_state=42
        )

    output_rows = []

    for _, row in ratings_df.iterrows():
        # Cộng 100000 để tránh trùng user thật trong database của bạn
        user_id = int(row["userId"]) + 100000

        movie_id = map_movielens_movie_to_local(row["movieId"])

        rating_10 = convert_rating_to_10_scale(row["rating"])
        rating_10 = round(rating_10)

        weight = rating_to_weight(rating_10)

        output_rows.append({
            "user_id": user_id,
            "movie_id": movie_id,
            "rating": rating_10,
            "weight": weight,
            "action_type": f"RATING_{rating_10}"
        })

    output_df = pd.DataFrame(output_rows)

    # Tránh 1 user có quá nhiều dòng trùng cùng 1 phim local sau khi map
    output_df = output_df.drop_duplicates(
        subset=["user_id", "movie_id"],
        keep="last"
    )

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    output_df.to_csv(
        OUTPUT_PATH,
        index=False,
        encoding="utf-8"
    )

    print("Rows after:", len(output_df))
    print("Saved to:", OUTPUT_PATH)
    print(output_df.head(20))

    print("\nDistribution by movie_id:")
    print(output_df["movie_id"].value_counts().sort_index())

    print("\nDistribution by weight:")
    print(output_df["weight"].value_counts().sort_index())


if __name__ == "__main__":
    main()