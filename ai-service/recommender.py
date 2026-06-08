from data_loader import (
    load_movies,
    load_user_interactions,
    load_user_booked_movie_ids,
    load_all_interactions,
    load_hot_movies,
)

from engines.content_based import ContentBasedEngine
from engines.collaborative import CollaborativeEngine
from engines.svd_collaborative import SvdCollaborativeEngine
from engines.hybrid import HybridEngine
from qdrant_client_service import QdrantVectorService


class MovieRecommender:
    def __init__(self):
        self.movies_df = None

        # Engine 1: Content-Based Filtering
        self.content_engine = ContentBasedEngine()

        # Engine 2: Item-Item Collaborative Filtering
        self.collaborative_engine = CollaborativeEngine()

        # Engine 3: Truncated SVD Collaborative Filtering
        self.svd_engine = SvdCollaborativeEngine(latent_dim=8)

        # Engine 4: Hybrid Model
        self.hybrid_engine = HybridEngine()

        # Vector Database: Qdrant
        self.qdrant_service = QdrantVectorService()

    def train(self):
        """
        Train toàn bộ hệ thống AI:
        1. Load phim từ MySQL.
        2. Train Content-Based TF-IDF.
        3. Đồng bộ vector content lên Qdrant.
        4. Load toàn bộ interaction từ booking/review.
        5. Train Item-Item Collaborative Filtering.
        6. Train SVD Collaborative Filtering.
        """

        self.movies_df = load_movies()

        if self.movies_df.empty:
            raise Exception("Không có dữ liệu phim để train AI.")

        # ==========================
        # 1. Train Content-Based
        # ==========================
        self.content_engine.train(self.movies_df)

        # ==========================
        # 2. Sync content vectors to Qdrant
        # ==========================
        if self.content_engine.vector_size > 0:
            dense_content_vectors = self.content_engine.get_dense_movie_vectors()

            try:
                self.qdrant_service.recreate_collection(
                    collection_name=self.qdrant_service.content_collection,
                    vector_size=self.content_engine.vector_size
                )

                self.qdrant_service.upsert_vectors(
                    collection_name=self.qdrant_service.content_collection,
                    vector_dict=dense_content_vectors
                )

            except Exception as e:
                print("Qdrant sync failed. AI will continue using in-memory vectors.")
                print(e)

        # ==========================
        # 3. Train Collaborative Engines
        # ==========================
        all_interactions = load_all_interactions()

        self.collaborative_engine.train(all_interactions)

        self.svd_engine.train(all_interactions)

        print("MovieRecommender trained successfully.")
        print(f"Total movies: {len(self.movies_df)}")

    def get_booked_movie_ids(self, user_id: int):
        """
        Lấy danh sách phim user đã đặt vé thành công.
        Các phim này sẽ bị loại khỏi danh sách gợi ý.
        """

        booked_df = load_user_booked_movie_ids(user_id)

        if booked_df.empty:
            return set()

        return set(
            int(movie_id)
            for movie_id in booked_df["movie_id"].dropna().tolist()
        )

    def get_popular_or_latest_movies(self, limit: int = 10, excluded_movie_ids=None):
        """
        Fallback cuối cùng cho user mới hoặc khi model không tạo được kết quả.

        Lấy phim đang hot dựa trên:
        - booking_count: số lượt đặt vé thành công
        - avg_rating: điểm đánh giá trung bình
        - review_count: số lượt đánh giá

        Đồng thời loại bỏ phim user đã đặt vé thành công.
        """

        if excluded_movie_ids is None:
            excluded_movie_ids = set()

        hot_movies_df = load_hot_movies()

        # Nếu query hot movie bị lỗi hoặc không có dữ liệu thì quay về danh sách phim mặc định
        if hot_movies_df.empty:
            result = self.movies_df[
                ~self.movies_df["movie_id"].isin(excluded_movie_ids)
            ].head(limit)

            return [
                {
                    "movieId": int(row["movie_id"]),
                    "score": 0.5,
                    "source": "default_fallback",
                    "reason": "Gợi ý phim mặc định do chưa có dữ liệu phim hot."
                }
                for _, row in result.iterrows()
            ]

        # Loại phim user đã đặt
        hot_movies_df = hot_movies_df[
            ~hot_movies_df["movie_id"].isin(excluded_movie_ids)
        ]

        # Chỉ lấy limit phim hot nhất
        hot_movies_df = hot_movies_df.head(limit)

        results = []

        for _, row in hot_movies_df.iterrows():
            movie_id = int(row["movie_id"])
            hot_score = float(row["hot_score"])
            booking_count = int(row["booking_count"])
            avg_rating = float(row["avg_rating"])
            review_count = int(row["review_count"])

            # Chuẩn hóa score fallback về khoảng dễ nhìn.
            # Đây chỉ là điểm tương đối cho fallback.
            normalized_score = hot_score / 10

            if normalized_score > 1:
                normalized_score = 1.0

            if normalized_score < 0:
                normalized_score = 0.0

            results.append({
                "movieId": movie_id,
                "score": normalized_score,
                "source": "popular_fallback",
                "reason": (
                    f"Phim đang hot với {booking_count} lượt đặt vé, "
                    f"điểm đánh giá trung bình {round(avg_rating, 1)}/10 "
                    f"từ {review_count} lượt đánh giá."
                )
            })

        return results

    def recommend_for_user(self, user_id: int, limit: int = 10):
        """
        Gợi ý phim cho user.

        Fallback strategy:
        1. Hybrid: nếu có cả content_score và collaborative_score.
        2. Content-Based: nếu chỉ có content_score.
        3. Collaborative: nếu chỉ có collaborative_score.
        4. Popular Fallback: nếu user mới hoặc không có kết quả.
        """

        if self.movies_df is None:
            self.train()

        # ==========================
        # 1. Lấy phim đã đặt để loại khỏi gợi ý
        # ==========================
        booked_movie_ids = self.get_booked_movie_ids(user_id)

        print(f"User {user_id} booked movie ids:", booked_movie_ids)

        # ==========================
        # 2. Lấy interaction của user
        # ==========================
        interactions = load_user_interactions(user_id)

        print(f"User {user_id} interactions:")
        print(interactions)

        # User mới hoàn toàn → fallback popular/latest
        if interactions.empty:
            return self.get_popular_or_latest_movies(
                limit=limit,
                excluded_movie_ids=booked_movie_ids
            )

        # ==========================
        # 3. Tạo user vector cho Content-Based
        # ==========================
        user_vector = self.content_engine.build_user_vector(interactions)

        # Nếu không tạo được user vector → fallback popular/latest
        if not user_vector:
            return self.get_popular_or_latest_movies(
                limit=limit,
                excluded_movie_ids=booked_movie_ids
            )

        results = []

        # ==========================
        # 4. Tính điểm cho từng phim ứng viên
        # ==========================
        for _, row in self.movies_df.iterrows():
            movie_id = int(row["movie_id"])

            # Không gợi ý lại phim user đã đặt vé thành công
            if movie_id in booked_movie_ids:
                continue

            # --------------------------
            # Content-Based score
            # --------------------------
            content_score = self.content_engine.score(
                user_vector=user_vector,
                movie_id=movie_id
            )

            # --------------------------
            # Item-Item CF score
            # --------------------------
            item_cf_score = self.collaborative_engine.score(
                candidate_movie_id=movie_id,
                user_interactions=interactions
            )

            # --------------------------
            # SVD Collaborative score
            # --------------------------
            svd_score = self.svd_engine.score(
                candidate_movie_id=movie_id,
                user_interactions=interactions
            )

            # Gộp 2 nhánh collaborative:
            # - Item-Item CF trực tiếp
            # - SVD latent factor
            collaborative_score = 0.5 * item_cf_score + 0.5 * svd_score

            # --------------------------
            # Decide source
            # --------------------------
            source = self.hybrid_engine.decide_source(
                content_score=content_score,
                collaborative_score=collaborative_score
            )

            # Nếu cả content và collaborative đều không có tín hiệu thì bỏ qua
            if source == "none":
                continue

            # --------------------------
            # Final score theo fallback layer
            # --------------------------
            if source == "hybrid":
                final_score = self.hybrid_engine.combine(
                    content_score=content_score,
                    collaborative_score=collaborative_score,
                    interaction_count=len(interactions)
                )

                reason = "Kết hợp nội dung phim và hành vi cộng đồng người dùng."

            elif source == "content_based":
                final_score = content_score

                reason = "Gợi ý dựa trên nội dung phim giống sở thích của bạn."

            else:
                final_score = collaborative_score

                reason = "Gợi ý dựa trên hành vi của những người dùng có sở thích tương tự."

            results.append({
                "movieId": movie_id,
                "score": float(final_score),
                "source": source,
                "reason": reason
            })

        # ==========================
        # 5. Sắp xếp kết quả
        # ==========================
        results = sorted(
            results,
            key=lambda item: item["score"],
            reverse=True
        )

        # ==========================
        # 6. Fallback cuối nếu không có kết quả
        # ==========================
        if not results:
            print("No hybrid/content/collaborative results. Using popular fallback.")

            return self.get_popular_or_latest_movies(
                limit=limit,
                excluded_movie_ids=booked_movie_ids
            )

        return results[:limit]