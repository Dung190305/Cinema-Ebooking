class HybridEngine:
    def get_weights(self, interaction_count: int):
        """
        Trọng số động:
        - User mới: ưu tiên content-based
        - User vừa đủ dữ liệu: cân bằng
        - User lâu năm: tăng collaborative
        """

        if interaction_count < 3:
            return {
                "content_weight": 0.8,
                "collaborative_weight": 0.2
            }

        if interaction_count < 10:
            return {
                "content_weight": 0.6,
                "collaborative_weight": 0.4
            }

        return {
            "content_weight": 0.4,
            "collaborative_weight": 0.6
        }

    def combine(self, content_score: float, collaborative_score: float, interaction_count: int):
        """
        Kết hợp điểm theo Weighted Linear Combination.
        """

        weights = self.get_weights(interaction_count)

        final_score = (
            weights["content_weight"] * content_score +
            weights["collaborative_weight"] * collaborative_score
        )

        return final_score

    def decide_source(self, content_score: float, collaborative_score: float):
        """
        Xác định recommendation đến từ tầng nào.
        """

        has_content = content_score > 0
        has_collaborative = collaborative_score > 0

        if has_content and has_collaborative:
            return "hybrid"

        if has_content:
            return "content_based"

        if has_collaborative:
            return "collaborative"

        return "none"