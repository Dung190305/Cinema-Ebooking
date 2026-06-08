import math
from collections import Counter, defaultdict


class ContentBasedEngine:
    def __init__(self):
        self.idf_scores = {}
        self.movie_vectors = {}

        # Dùng cho Qdrant: word -> index
        self.vocabulary = {}
        self.vector_size = 0

    def tokenize(self, text: str):
        text = str(text).lower()

        for ch in ",.!?;:/\\()[]{}\"'“”‘’-_":
            text = text.replace(ch, " ")

        return [
            word
            for word in text.split()
            if len(word) >= 2
        ]

    def build_tf_vector(self, text: str):
        words = self.tokenize(text)
        total_words = len(words)

        if total_words == 0:
            return {}

        counter = Counter(words)

        return {
            word: count / total_words
            for word, count in counter.items()
        }

    def build_idf_scores(self, movie_contents):
        total_movies = len(movie_contents)
        document_frequency = defaultdict(int)

        for content in movie_contents:
            unique_words = set(self.tokenize(content))

            for word in unique_words:
                document_frequency[word] += 1

        idf_scores = {}

        for word, df in document_frequency.items():
            idf_scores[word] = math.log((total_movies + 1) / (df + 1)) + 1

        return idf_scores

    def build_vocabulary(self):
        """
        Tạo vocabulary cố định để convert TF-IDF dict thành dense vector list.
        Qdrant yêu cầu mọi vector phải cùng số chiều.
        """

        words = sorted(self.idf_scores.keys())

        self.vocabulary = {
            word: index
            for index, word in enumerate(words)
        }

        self.vector_size = len(self.vocabulary)

    def build_tfidf_vector(self, text: str):
        tf_vector = self.build_tf_vector(text)
        tfidf_vector = {}

        for word, tf_value in tf_vector.items():
            idf_value = self.idf_scores.get(word, 0)
            tfidf_vector[word] = tf_value * idf_value

        return tfidf_vector

    def to_dense_vector(self, sparse_vector: dict):
        """
        Convert:
        {"action": 0.2, "hero": 0.1}

        thành:
        [0.0, 0.2, 0.0, 0.1, ...]
        """

        dense_vector = [0.0] * self.vector_size

        for word, value in sparse_vector.items():
            index = self.vocabulary.get(word)

            if index is not None:
                dense_vector[index] = float(value)

        return dense_vector

    def get_dense_movie_vectors(self):
        """
        Trả về:
        movie_id -> dense vector list
        để upsert vào Qdrant.
        """

        dense_vectors = {}

        for movie_id, sparse_vector in self.movie_vectors.items():
            dense_vectors[movie_id] = self.to_dense_vector(sparse_vector)

        return dense_vectors

    def train(self, movies_df):
        movie_contents = movies_df["content"].tolist()

        self.idf_scores = self.build_idf_scores(movie_contents)

        # Phải build vocabulary trước khi convert dense vector
        self.build_vocabulary()

        self.movie_vectors = {}

        for _, row in movies_df.iterrows():
            movie_id = int(row["movie_id"])
            content = row["content"]

            self.movie_vectors[movie_id] = self.build_tfidf_vector(content)

        print("Content-Based Engine trained.")
        print(f"Total vocabulary: {len(self.idf_scores)}")
        print(f"Content vector size: {self.vector_size}")

    def cosine_similarity(self, vec1: dict, vec2: dict):
        common_words = set(vec1.keys()) & set(vec2.keys())

        dot_product = sum(vec1[word] * vec2[word] for word in common_words)

        norm1 = math.sqrt(sum(value * value for value in vec1.values()))
        norm2 = math.sqrt(sum(value * value for value in vec2.values()))

        if norm1 == 0 or norm2 == 0:
            return 0.0

        return dot_product / (norm1 * norm2)

    def build_user_vector(self, interactions):
        """
        Tạo vector sở thích user từ trọng số hành vi.

        weight > 0: cộng vector phim
        weight < 0: trừ vector phim
        """

        user_vector = {}

        if interactions.empty:
            return user_vector

        total_abs_weight = 0

        for _, row in interactions.iterrows():
            movie_id = int(row["movie_id"])
            weight = float(row["weight"])

            if movie_id not in self.movie_vectors:
                continue

            movie_vector = self.movie_vectors[movie_id]

            for word, value in movie_vector.items():
                user_vector[word] = user_vector.get(word, 0) + value * weight

            total_abs_weight += abs(weight)

        if total_abs_weight == 0:
            return {}

        for word in user_vector:
            user_vector[word] = user_vector[word] / total_abs_weight

        return user_vector

    def build_dense_user_vector(self, interactions):
        sparse_user_vector = self.build_user_vector(interactions)
        return self.to_dense_vector(sparse_user_vector)

    def score(self, user_vector: dict, movie_id: int):
        movie_vector = self.movie_vectors.get(movie_id, {})

        if not user_vector or not movie_vector:
            return 0.0

        return self.cosine_similarity(user_vector, movie_vector)