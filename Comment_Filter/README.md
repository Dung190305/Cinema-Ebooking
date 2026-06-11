# Comment_Filter

AI-based comment filter system for Cinema E-booking platform.

## Cấu trúc

```
Comment_Filter/
├── layer1_filter.py    # Lớp 1: Chuẩn hóa & lọc cứng (profanity, spam, ...)
├── train.py            # Train model phân loại POSITIVE / NEGATIVE / SPOILER
├── dataset.csv         # Dataset 1800 câu (500+/nhãn)
├── api.py              # FastAPI server phục vụ BE Spring Boot
├── model.pkl           # Model đã train (sinh ra sau khi chạy train.py)
└── requirements.txt    # Dependencies
```

## Luồng hoạt động

```
Comment → Layer1 (chuẩn hóa + lọc cứng)
         ↓
    Layer2 (ML model: POSITIVE / NEGATIVE / SPOILER)
         ↓
    Final Output → API response → Spring Boot BE
```

## Cách chạy

```bash
# 1. Cài đặt dependencies
pip install -r requirements.txt

# 2. Train model
python train.py

# 3. Chạy API server
python api.py
```

## API Endpoint

```
POST http://localhost:8081/api/v1/ai/analyze
Content-Type: application/json

Body: { "text": "Bộ phim hay quá, xem 10 lần vẫn thích!" }

Response:
{
  "valid": true,
  "label": "POSITIVE",
  "cleanedText": "Bộ phim hay quá, xem 10 lần vẫn thích!",
  "finalOutput": "Bộ phim hay quá, xem 10 lần vẫn thích!",
  "finalDecision": "APPROVED",
  "spoiler": false,
  "spoilerConf": 0.03,
  "censoredWords": [],
  "profanityCount": 0,
  "profanityRatio": 0.0,
  "processTime": 12.5
}
```

## Cài đặt AI trên Spring Boot

```yaml
# application.yml
ai:
  service:
    url: http://localhost:8081/api/v1/ai/analyze
    enabled: true
```
