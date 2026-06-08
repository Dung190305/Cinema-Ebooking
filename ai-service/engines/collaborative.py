import math


class CollaborativeEngine:
    def __init__(self):
        self.item_user_vectors = {}

    def train(self, all_interactions):
        """
        Tạo vector cộng đồng cho từng phim.

        item_user_vectors:
        movie_id -> {user_id: weight}
        """

        item_user_vectors = {}

        if all_interactions.empty:
            self.item_user_vectors = {}
            print("No collaborative data found.")
            return

        for _, row in all_interactions.iterrows():
            movie_id = int(row["movie_id"])
            user_id = int(row["user_id"])
            weight = float(row["weight"])

            if movie_id not in item_user_vectors:
                item_user_vectors[movie_id] = {}

            item_user_vectors[movie_id][user_id] = weight

        self.item_user_vectors = item_user_vectors

        print(f"Collaborative Engine trained for {len(self.item_user_vectors)} movies.")

    def item_similarity(self, movie_id_1: int, movie_id_2: int):
        """
        Tính độ giống nhau giữa 2 phim dựa trên hành vi cộng đồng.
        """

        vec1 = self.item_user_vectors.get(movie_id_1, {})
        vec2 = self.item_user_vectors.get(movie_id_2, {})

        if not vec1 or not vec2:
            return 0.0

        common_users = set(vec1.keys()) & set(vec2.keys())

        dot_product = sum(
            vec1[user_id] * vec2[user_id]
            for user_id in common_users
        )

        norm1 = math.sqrt(sum(value * value for value in vec1.values()))
        norm2 = math.sqrt(sum(value * value for value in vec2.values()))

        if norm1 == 0 or norm2 == 0:
            return 0.0

        return dot_product / (norm1 * norm2)

    def score(self, candidate_movie_id: int, user_interactions):
        """
        Tính điểm Collaborative cho một phim ứng viên.

        Nếu phim ứng viên giống các phim user từng tương tác tích cực,
        điểm sẽ cao hơn.
        """

        if user_interactions.empty or not self.item_user_vectors:
            return 0.0

        numerator = 0.0
        denominator = 0.0

        for _, row in user_interactions.iterrows():
            interacted_movie_id = int(row["movie_id"])
            user_weight = float(row["weight"])

            similarity = self.item_similarity(
                candidate_movie_id,
                interacted_movie_id
            )

            if similarity == 0:
                continue

            numerator += similarity * user_weight
            denominator += abs(similarity)

        if denominator == 0:
            return 0.0

        raw_score = numerator / denominator

        # Vì weight nằm khoảng -3 đến +8
        # Chuẩn hóa về 0 → 1
        normalized_score = (raw_score + 3) / 11

        if normalized_score < 0:
            return 0.0

        if normalized_score > 1:
            return 1.0

        return normalized_score