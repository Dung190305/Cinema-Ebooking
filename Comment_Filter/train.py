"""
Train Vietnamese Comment Sentiment Model
- POSITIVE (0) / NEGATIVE (1) / SPOILER (2)
- Uses TF-IDF + Logistic Regression (lightweight, fast, no GPU needed)
- Falls back to dummy all-zero if sklearn unavailable
"""

import os
import sys
import time
import pickle
import json
import io

# Force UTF-8 stdout so Vietnamese characters print correctly on Windows
if sys.stdout.encoding != "utf-8":
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")
if sys.stderr.encoding != "utf-8":
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding="utf-8", errors="replace")

# ─── CHECK DEPENDENCIES ──────────────────────────────────────────────────────
try:
    import pandas as pd
    import numpy as np
    from sklearn.model_selection import train_test_split
    from sklearn.feature_extraction.text import TfidfVectorizer
    from sklearn.linear_model import LogisticRegression
    from sklearn.pipeline import Pipeline
    from sklearn.metrics import (
        classification_report,
        confusion_matrix,
        accuracy_score,
    )
    DEPENDENCIES_OK = True
except ImportError as e:
    DEPENDENCIES_OK = False
    MISSING_MODULE = str(e).split("'")[1] if "'" in str(e) else str(e)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "model.pkl")
DATASET_PATH = os.path.join(BASE_DIR, "dataset.csv")


# ─── VIETNAMESE TOKENIZER ─────────────────────────────────────────────────────
import re

def tokenize_vi(text: str) -> str:
    """
    Lightweight tokenizer for Vietnamese:
    - Lowercase
    - Split on spaces + punctuation
    - Keep only [a-zA-Z0-9ăâáắấàảãạằầấẩẫậđẹêéếềểễệiíìỉĩịoôốộờớởỡợuúùủũụưứừửữựyýỳỷỹỵ]
    """
    text = text.lower()
    # Keep letters, digits, and Vietnamese diacritics
    tokens = re.findall(
        r"[\wăâáắấàảãạằầấẩẫậđẹêéếềểễệiíìỉĩịoôốộờớởỡợuúùủũụưứừửữựyýỳỷỹỵ]+",
        text,
    )
    return " ".join(tokens)


# ─── TRAIN ─────────────────────────────────────────────────────────────────────
def train_model(dataset_path: str = DATASET_PATH, model_path: str = MODEL_PATH):
    if not DEPENDENCIES_OK:
        print(f"[WARN] Missing '{MISSING_MODULE}', generating dummy model.pkl")
        _create_dummy_model(model_path)
        return

    print("=" * 60)
    print("Vietnamese Comment Sentiment Classifier — Training")
    print("=" * 60)

    # 1. Load dataset
    print(f"\n[1] Loading dataset from: {dataset_path}")
    df = pd.read_csv(dataset_path)
    print(f"    Total samples : {len(df)}")
    print(f"    Columns       : {list(df.columns)}")
    print(f"    Label counts  :")
    for label, count in df["label"].value_counts().sort_index().items():
        name = {0: "POSITIVE", 1: "NEGATIVE", 2: "SPOILER"}.get(label, str(label))
        print(f"      {label} ({name}): {count}")

    # 2. Preprocess
    print("\n[2] Tokenizing text...")
    df["cleaned"] = df["text"].astype(str).apply(tokenize_vi)
    df = df[df["cleaned"].str.len() > 0].reset_index(drop=True)
    print(f"    Samples after cleaning: {len(df)}")

    # 3. Split — ensure 1-D arrays to avoid sklearn dimension mismatch
    X = df["cleaned"].values.tolist()
    y = df["label"].values.ravel()
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    print(f"    Train size: {len(X_train)} | Test size: {len(X_test)}")

    # 4. Build pipeline
    print("\n[3] Building TF-IDF + Logistic Regression pipeline...")
    pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(
            max_features=15_000,
            ngram_range=(1, 3),
            min_df=2,
            max_df=0.95,
            sublinear_tf=True,
            analyzer="word",
        )),
        ("clf", LogisticRegression(
            C=1.0,
            max_iter=1000,
            solver="lbfgs",
            class_weight="balanced",
            random_state=42,
        )),
    ])

    # 5. Train
    print("\n[4] Training model...")
    t0 = time.time()
    pipeline.fit(X_train, y_train)
    train_time = time.time() - t0
    print(f"    Training done in {train_time:.2f}s")

    # 6. Evaluate
    print("\n[5] Evaluation on test set:")
    y_pred = pipeline.predict(X_test)
    acc = accuracy_score(y_test, y_pred)
    print(f"\n    Accuracy: {acc:.4f}")

    label_names = ["POSITIVE (0)", "NEGATIVE (1)", "SPOILER (2)"]
    print("\n" + classification_report(y_test, y_pred, target_names=label_names))

    print("Confusion Matrix:")
    cm = confusion_matrix(y_test, y_pred)
    print(f"               Predicted")
    print(f"             P   N   S")
    for i, row in enumerate(cm):
        name = ["P", "N", "S"][i]
        print(f"  True {name}  {row[0]:3d} {row[1]:3d} {row[2]:3d}")

    # 7. Save model
    print(f"\n[6] Saving model to: {model_path}")
    os.makedirs(os.path.dirname(model_path) or ".", exist_ok=True)
    with open(model_path, "wb") as f:
        pickle.dump(pipeline, f)

    # 8. Save label mapping for inference
    label_map = {0: "POSITIVE", 1: "NEGATIVE", 2: "SPOILER"}
    meta_path = model_path.replace(".pkl", "_meta.json")
    with open(meta_path, "w", encoding="utf-8") as f:
        json.dump({
            "label_map": label_map,
            "accuracy": float(acc),
            "train_samples": len(X_train),
            "test_samples": len(X_test),
            "train_time_s": round(train_time, 2),
        }, f, ensure_ascii=False, indent=2)

    print(f"\n{'='*60}")
    print(f"Training complete! Model saved to: {model_path}")
    print(f"{'='*60}")


def _create_dummy_model(model_path: str):
    """Create a minimal placeholder model when sklearn is unavailable."""
    dummy_pipeline = None
    os.makedirs(os.path.dirname(model_path) or ".", exist_ok=True)
    with open(model_path, "wb") as f:
        pickle.dump(dummy_pipeline, f)

    meta_path = model_path.replace(".pkl", "_meta.json")
    with open(meta_path, "w", encoding="utf-8") as f:
        json.dump({
            "label_map": {0: "POSITIVE", 1: "NEGATIVE", 2: "SPOILER"},
            "accuracy": None,
            "note": "Dummy model — install sklearn to train real model",
        }, f, ensure_ascii=False, indent=2)
    print(f"[INFO] Dummy model created at: {model_path}")


def load_model(model_path: str = MODEL_PATH):
    """Load trained model from disk."""
    if not os.path.exists(model_path):
        return None
    with open(model_path, "rb") as f:
        return pickle.load(f)


def predict(text: str, model_path: str = MODEL_PATH) -> dict:
    """
    Run prediction on a single comment string.
    Returns dict with: label, confidence, probabilities.
    """
    model = load_model(model_path)
    if model is None:
        return {
            "label": "POSITIVE",
            "label_code": 0,
            "confidence": 0.33,
            "probabilities": {"POSITIVE": 0.33, "NEGATIVE": 0.33, "SPOILER": 0.33},
            "error": "Model not found. Run train.py first.",
        }

    cleaned = tokenize_vi(text)
    label_code = int(model.predict([cleaned])[0])
    probs = model.predict_proba([cleaned])[0]
    confidence = float(max(probs))

    label_map = {0: "POSITIVE", 1: "NEGATIVE", 2: "SPOILER"}
    label_name = label_map[label_code]

    return {
        "label": label_name,
        "label_code": label_code,
        "confidence": round(confidence, 4),
        "probabilities": {
            label_map[i]: round(float(p), 4) for i, p in enumerate(probs)
        },
        "cleaned_text": cleaned,
    }


# ─── CLI ──────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    # Check if --quick flag passed
    quick = "--quick" in sys.argv

    if DEPENDENCIES_OK:
        train_model()
        if not quick:
            # Quick demo
            print("\n--- Prediction Demo ---")
            test_comments = [
                "Phim hay quá, diễn viên diễn xuất tuyệt vời!",
                "Dở ẹt, phí tiền vé, không nên xem",
                "Twist ở cuối khiến tôi sốc, nhân vật chính thực ra là kẻ sát nhân",
            ]
            for comment in test_comments:
                result = predict(comment)
                print(f"\nText: {comment}")
                print(f"  → Label: {result['label']} (conf: {result['confidence']})")
                print(f"     Probs: {result['probabilities']}")
    else:
        print(f"[ERROR] Cannot train: missing dependency '{MISSING_MODULE}'")
        print("Install it with: pip install scikit-learn pandas numpy")
        print("Creating dummy model...")
        _create_dummy_model(MODEL_PATH)
