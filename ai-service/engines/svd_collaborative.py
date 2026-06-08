import numpy as np


class SvdCollaborativeEngine:
    def __init__(self, latent_dim: int = 8):
        self.latent_dim = latent_dim

        self.user_id_to_index = {}
        self.movie_id_to_index = {}
        self.index_to_movie_id = {}

        self.movie_latent_vectors = {}
        self.global_mean = 0.0
        self.movie_means = {}

    def train(self, all_interactions):
        """
        Huấn luyện Collaborative Filtering bằng SVD thủ công thông qua numpy.

        Input:
        all_interactions gồm:
        user_id, movie_id, weight

        Output:
        movie_latent_vectors:
        movie_id -> vector ẩn K chiều
        """

        if all_interactions.empty:
            print("No data for SVD Collaborative Engine.")
            return

        user_ids = sorted(
            set(int(user_id) for user_id in all_interactions["user_id"].dropna().tolist())
        )

        movie_ids = sorted(
            set(int(movie_id) for movie_id in all_interactions["movie_id"].dropna().tolist())
        )

        self.user_id_to_index = {
            user_id: index
            for index, user_id in enumerate(user_ids)
        }

        self.movie_id_to_index = {
            movie_id: index
            for index, movie_id in enumerate(movie_ids)
        }

        self.index_to_movie_id = {
            index: movie_id
            for movie_id, index in self.movie_id_to_index.items()
        }

        matrix = np.zeros((len(user_ids), len(movie_ids)))

        for _, row in all_interactions.iterrows():
            user_id = int(row["user_id"])
            movie_id = int(row["movie_id"])
            weight = float(row["weight"])

            user_index = self.user_id_to_index[user_id]
            movie_index = self.movie_id_to_index[movie_id]

            matrix[user_index, movie_index] = weight

        non_zero_values = matrix[matrix != 0]

        if len(non_zero_values) == 0:
            print("SVD matrix has no non-zero values.")
            return

        self.global_mean = float(non_zero_values.mean())

        centered_matrix = matrix.copy()

        for movie_index in range(centered_matrix.shape[1]):
            movie_values = centered_matrix[:, movie_index]
            non_zero_movie_values = movie_values[movie_values != 0]

            if len(non_zero_movie_values) > 0:
                movie_mean = float(non_zero_movie_values.mean())
            else:
                movie_mean = self.global_mean

            movie_id = self.index_to_movie_id[movie_index]
            self.movie_means[movie_id] = movie_mean

            for user_index in range(centered_matrix.shape[0]):
                if centered_matrix[user_index, movie_index] != 0:
                    centered_matrix[user_index, movie_index] -= movie_mean

        try:
            user_matrix, singular_values, movie_matrix_t = np.linalg.svd(
                centered_matrix,
                full_matrices=False
            )
        except Exception as e:
            print("SVD training failed:")
            print(e)
            return

        k = min(self.latent_dim, len(singular_values))

        singular_values_k = singular_values[:k]
        movie_matrix_k = movie_matrix_t[:k, :].T

        self.movie_latent_vectors = {}

        for movie_index, vector in enumerate(movie_matrix_k):
            movie_id = self.index_to_movie_id[movie_index]

            # Nhân thêm căn bậc hai singular value để giữ độ quan trọng latent dimension
            latent_vector = vector * np.sqrt(singular_values_k)

            self.movie_latent_vectors[movie_id] = latent_vector

        print(f"SVD Collaborative Engine trained.")
        print(f"Users: {len(user_ids)}")
        print(f"Movies: {len(movie_ids)}")
        print(f"Latent dim: {k}")

    def cosine_similarity(self, vec1, vec2):
        if vec1 is None or vec2 is None:
            return 0.0

        norm1 = np.linalg.norm(vec1)
        norm2 = np.linalg.norm(vec2)

        if norm1 == 0 or norm2 == 0:
            return 0.0

        return float(np.dot(vec1, vec2) / (norm1 * norm2))

    def item_similarity(self, movie_id_1: int, movie_id_2: int):
        vec1 = self.movie_latent_vectors.get(movie_id_1)
        vec2 = self.movie_latent_vectors.get(movie_id_2)

        return self.cosine_similarity(vec1, vec2)

    def score(self, candidate_movie_id: int, user_interactions):
        """
        Tính điểm SVD Collaborative cho phim ứng viên.
        """

        if user_interactions.empty or not self.movie_latent_vectors:
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

        # weight nằm khoảng -3 đến +8
        normalized_score = (raw_score + 3) / 11

        if normalized_score < 0:
            return 0.0

        if normalized_score > 1:
            return 1.0

        return normalized_score