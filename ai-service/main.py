from fastapi import FastAPI
from recommender import MovieRecommender
from evaluator import RecommendationEvaluator
from scheduler import start_training_scheduler

app = FastAPI(
    title="Cinema Ebooking Recommendation AI",
    description="AI service for movie recommendation",
    version="1.0.0"
)

recommender = MovieRecommender()
evaluator = RecommendationEvaluator(recommender)
scheduler = None


@app.on_event("startup")
def startup_event():
    """
    Khi AI Service khởi động:
    1. Train model lần đầu.
    2. Khởi động batch training scheduler.
    """

    global scheduler

    recommender.train()

    if scheduler is None:
        scheduler = start_training_scheduler(recommender)


@app.get("/")
def root():
    return {
        "message": "Cinema Ebooking AI Recommendation Service is running",
        "model": "Hybrid Recommendation System",
        "components": [
            "Content-Based Filtering",
            "Item-Item Collaborative Filtering",
            "Truncated SVD",
            "Hybrid Model",
            "Qdrant Vector DB",
            "Fallback Strategy",
            "Batch Training Scheduler"
        ],
        "training_schedule": "Every day at 02:00 Asia/Ho_Chi_Minh"
    }


@app.get("/recommendations/{user_id}")
def recommend_movies(user_id: int, limit: int = 10):
    """
    API gợi ý phim cho user.

    Spring Boot sẽ gọi API này:
    GET /recommendations/{user_id}?limit=10
    """

    items = recommender.recommend_for_user(user_id, limit)

    recommendations = []

    for item in items:
        recommendations.append({
            "movie_id": item["movieId"],
            "score": item["score"],
            "source": item.get("source", "hybrid")
        })

    return {
        "user_id": user_id,
        "source": "hybrid",
        "cached": False,
        "recommendations": recommendations
    }


@app.get("/evaluate/{user_id}")
def evaluate_user(user_id: int, k: int = 10):
    """
    Đánh giá recommendation cho 1 user.
    """

    return evaluator.evaluate_user(user_id, k)


@app.get("/evaluate")
def evaluate_all(k: int = 10):
    """
    Đánh giá recommendation trên toàn bộ user có dữ liệu.
    """

    return evaluator.evaluate_all_users(k)


@app.post("/train")
def train_model():
    """
    API train thủ công.

    Khi thêm dữ liệu booking/review mới, có thể gọi API này
    để train lại model ngay mà không cần restart FastAPI.
    """

    recommender.train()

    return {
        "message": "AI model trained successfully",
        "source": "hybrid"
    }