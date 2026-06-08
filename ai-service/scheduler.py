from apscheduler.schedulers.background import BackgroundScheduler


def start_training_scheduler(recommender):
    """
    Batch Training Scheduler.

    Mỗi ngày lúc 02:00 sáng, hệ thống sẽ tự động train lại model:
    - Load lại dữ liệu phim
    - Load lại booking/review
    - Train lại TF-IDF
    - Train lại Collaborative Filtering
    - Train lại SVD
    - Đồng bộ vector lên Qdrant
    """

    scheduler = BackgroundScheduler(timezone="Asia/Ho_Chi_Minh")

    scheduler.add_job(
        recommender.train,
        trigger="cron",
        hour=2,
        minute=0,
        id="daily_ai_training",
        replace_existing=True
    )

    scheduler.start()

    print("Batch training scheduler started.")
    print("AI will retrain every day at 02:00 Asia/Ho_Chi_Minh.")

    return scheduler