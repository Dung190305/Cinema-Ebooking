from qdrant_client import QdrantClient
from qdrant_client.models import Distance, VectorParams, PointStruct


class QdrantVectorService:
    def __init__(self):
        self.client = QdrantClient(
            url="http://localhost:6333",
            check_compatibility=False
        )

        self.content_collection = "movie_content_vectors"
        self.svd_collection = "movie_svd_vectors"

    def recreate_collection(self, collection_name: str, vector_size: int):
        collections = self.client.get_collections().collections
        existing_names = [collection.name for collection in collections]

        if collection_name in existing_names:
            self.client.delete_collection(collection_name=collection_name)

        self.client.create_collection(
            collection_name=collection_name,
            vectors_config=VectorParams(
                size=vector_size,
                distance=Distance.COSINE
            )
        )

        print(f"Qdrant collection recreated: {collection_name}, size={vector_size}")

    def upsert_vectors(self, collection_name: str, vector_dict: dict):
        points = []

        for movie_id, vector in vector_dict.items():
            points.append(
                PointStruct(
                    id=int(movie_id),
                    vector=[float(value) for value in vector],
                    payload={
                        "movie_id": int(movie_id)
                    }
                )
            )

        if not points:
            print(f"No vectors to upsert into {collection_name}.")
            return

        self.client.upsert(
            collection_name=collection_name,
            points=points
        )

        print(f"Upserted {len(points)} vectors into {collection_name}.")

    def search(self, collection_name: str, query_vector, limit: int = 10):
        return self.client.search(
            collection_name=collection_name,
            query_vector=[float(value) for value in query_vector],
            limit=limit
        )