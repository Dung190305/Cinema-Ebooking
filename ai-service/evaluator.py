from data_loader import load_all_interactions


class RecommendationEvaluator:
    def __init__(self, recommender):
        self.recommender = recommender

    def evaluate_user(self, user_id: int, k: int = 10):
        """
        Đánh giá recommendation cho 1 user.

        Ý tưởng:
        - Lấy các phim user thật sự thích từ dữ liệu interaction.
        - Gọi AI gợi ý top K phim.
        - So sánh phim AI gợi ý với phim user thích thật.
        """

        all_interactions = load_all_interactions()

        user_interactions = all_interactions[
            all_interactions["user_id"] == user_id
        ]

        if user_interactions.empty:
            return {
                "user_id": user_id,
                "precision_at_k": 0,
                "recall_at_k": 0,
                "hit_count": 0,
                "relevant_count": 0,
                "recommended_count": 0,
                "message": "User chưa có dữ liệu tương tác để đánh giá."
            }

        # Phim user thật sự thích: weight > 0
        relevant_movie_ids = set(
            int(movie_id)
            for movie_id in user_interactions[
                user_interactions["weight"] > 0
            ]["movie_id"].dropna().tolist()
        )

        if not relevant_movie_ids:
            return {
                "user_id": user_id,
                "precision_at_k": 0,
                "recall_at_k": 0,
                "hit_count": 0,
                "relevant_count": 0,
                "recommended_count": 0,
                "message": "User chưa có phim được xem là yêu thích."
            }

        recommendations = self.recommender.recommend_for_user(user_id, k)

        recommended_movie_ids = set(
            int(item["movieId"])
            for item in recommendations
        )

        hit_movie_ids = recommended_movie_ids & relevant_movie_ids

        hit_count = len(hit_movie_ids)
        recommended_count = len(recommended_movie_ids)
        relevant_count = len(relevant_movie_ids)

        precision_at_k = hit_count / recommended_count if recommended_count > 0 else 0
        recall_at_k = hit_count / relevant_count if relevant_count > 0 else 0

        return {
            "user_id": user_id,
            "k": k,
            "precision_at_k": round(precision_at_k, 4),
            "recall_at_k": round(recall_at_k, 4),
            "hit_count": hit_count,
            "relevant_count": relevant_count,
            "recommended_count": recommended_count,
            "hit_movie_ids": list(hit_movie_ids),
            "recommended_movie_ids": list(recommended_movie_ids),
            "relevant_movie_ids": list(relevant_movie_ids)
        }

    def evaluate_all_users(self, k: int = 10):
        """
        Đánh giá trung bình trên tất cả user có interaction.
        """

        all_interactions = load_all_interactions()

        if all_interactions.empty:
            return {
                "k": k,
                "average_precision_at_k": 0,
                "average_recall_at_k": 0,
                "user_count": 0,
                "message": "Không có dữ liệu interaction."
            }

        user_ids = sorted(
            set(
                int(user_id)
                for user_id in all_interactions["user_id"].dropna().tolist()
            )
        )

        results = []

        for user_id in user_ids:
            result = self.evaluate_user(user_id, k)

            if result["relevant_count"] > 0:
                results.append(result)

        if not results:
            return {
                "k": k,
                "average_precision_at_k": 0,
                "average_recall_at_k": 0,
                "user_count": 0,
                "message": "Không có user đủ dữ liệu để đánh giá."
            }

        average_precision = sum(
            item["precision_at_k"]
            for item in results
        ) / len(results)

        average_recall = sum(
            item["recall_at_k"]
            for item in results
        ) / len(results)

        return {
            "k": k,
            "average_precision_at_k": round(average_precision, 4),
            "average_recall_at_k": round(average_recall, 4),
            "user_count": len(results),
            "details": results
        }