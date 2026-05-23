# Review + AI Spoiler — FE Implementation Guide

## Backend Response Structure

API `/api/v1/reviews/movies/{movieId}` trả về `ReviewResponse`:

```json
{
  "reviewId": 1,
  "userId": 5,
  "userName": "Nguyễn Văn A",
  "movieId": 101,
  "bookingId": 200,
  "rating": 8,
  "comment": "Phim hay nhưng cuối phim nhân vật chính chết mới shock!",
  "finalText": "Phim hay nhưng cuối phim nhân vật chính chết mới shock!",
  "sentiment": "POSITIVE",
  "decision": "SPOILER_WARNING",
  "status": "ACTIVE",
  "isSpoiler": true,
  "spoilerConf": 0.87,
  "createdAt": "2026-05-22T10:00:00",
  "editedAt": null,
  "edited": false
}
```

## Decision → FE Display

| Decision | Status | isSpoiler | FE hiển thị |
|---|---|---|---|
| `APPROVED` | `ACTIVE` | `false` | Comment bình thường |
| `SPOILER_WARNING` | `ACTIVE` | `true` | Comment + blur + cảnh báo spoiler |
| `REJECTED` | `HIDDEN` | — | **Không bao giờ gửi cho FE** |

---

## 1. Comment thường (APPROVED)

```tsx
<div className="review-card">
  <div className="review-header">
    <Avatar name={review.userName} />
    <div>
      <div className="review-user">{review.userName}</div>
      <div className="review-rating">{"★".repeat(review.rating)}</div>
    </div>
    {review.edited && <span className="edited-badge">Đã chỉnh sửa</span>}
  </div>

  <p className="review-text">{review.finalText || review.comment}</p>
</div>
```

---

## 2. Comment có Spoiler (SPOILER_WARNING)

**Giao diện:**
```
┌─────────────────────────────────────────────┐
│  [★] Nguyễn Văn A                          │
│      ★★★★★★★★☆☆                             │
│                                             │
│  ⚠️  CẢNH BÁO SPOILER                      │
│  ─────────────────────────────────────────  │
│  ┌─────────────────────────────────────┐   │
│  │ ████████████████████████████████    │ ← Blur overlay
│  │     Nội dung bị ẩn                 │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  [ Hiện nội dung ]          [ Báo cáo ]   │
└─────────────────────────────────────────────┘
```

**Mã React/TypeScript:**

```tsx
interface ReviewProps {
  review: ReviewResponse;
}

function ReviewCard({ review }: ReviewProps) {
  const [revealed, setRevealed] = useState(false);

  // ── APPROVED: hiển thị bình thường ──────────────────────────
  if (review.decision === "APPROVED") {
    return (
      <div className="review-card">
        <div className="review-header">
          <Avatar name={review.userName} />
          <div>
            <span className="review-user">{review.userName}</span>
            <RatingStars rating={review.rating} />
          </div>
          {review.edited && <span className="badge">Đã chỉnh sửa</span>}
        </div>
        <p className="review-text">{review.finalText || review.comment}</p>
        <SentimentBadge sentiment={review.sentiment} />
      </div>
    );
  }

  // ── SPOILER_WARNING: blur + cảnh báo ───────────────────────
  if (review.decision === "SPOILER_WARNING") {
    return (
      <div className="review-card spoiler-card">
        <div className="review-header">
          <Avatar name={review.userName} />
          <div>
            <span className="review-user">{review.userName}</span>
            <RatingStars rating={review.rating} />
          </div>
        </div>

        {/* Cảnh báo spoiler */}
        <div className="spoiler-warning">
          <span className="warning-icon">⚠️</span>
          <span className="warning-text">CẢNH BÁO SPOILER</span>
        </div>

        {/* Nội dung bị blur */}
        <div className={`spoiler-content ${revealed ? "revealed" : ""}`}>
          <div className="blur-overlay" onClick={() => setRevealed(true)}>
            <p className="blurred-text">
              {review.finalText || review.comment}
            </p>
          </div>
          {!revealed && (
            <div className="spoiler-actions">
              <button
                className="btn-reveal"
                onClick={() => setRevealed(true)}
              >
                👁️ Hiện nội dung
              </button>
              <button className="btn-report">🚩 Báo cáo</button>
            </div>
          )}
        </div>
      </div>
    );
  }

  return null; // REJECTED không hiển thị
}
```

**CSS cho spoiler:**

```css
/* Card spoiler */
.spoiler-card {
  border: 2px solid #ffc107;
  background: #fffdf0;
}

.spoiler-warning {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 6px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #856404;
}

.warning-icon { font-size: 18px; }
.warning-text { font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; }

/* Blur overlay */
.spoiler-content { position: relative; }

.blur-overlay {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  cursor: pointer;
}

.blurred-text {
  filter: blur(6px);
  user-select: none;
  pointer-events: none;
  transition: filter 0.3s ease;
}

/* Khi đã click hiện nội dung → bỏ blur */
.spoiler-content.revealed .blur-overlay {
  cursor: default;
}

.spoiler-content.revealed .blurred-text {
  filter: none;
  user-select: text;
  pointer-events: auto;
}

/* Ẩn nút khi đã hiện */
.spoiler-content.revealed .spoiler-actions {
  display: none;
}

/* Actions */
.spoiler-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  justify-content: center;
}

.btn-reveal {
  background: #ffc107;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  color: #333;
}

.btn-reveal:hover { background: #e0a800; }

.btn-report {
  background: transparent;
  border: 1px solid #ccc;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  color: #666;
}

.btn-report:hover { background: #f5f5f5; }
```

---

## 3. Dữ liệu mới trong ReviewResponse

| Field | Mục đích |
|---|---|
| `decision` | `"APPROVED"` / `"SPOILER_WARNING"` — quyết định cách hiển thị |
| `isSpoiler` | `true` khi comment có nội dung spoiler |
| `spoilerConf` | Độ tin cậy (0.0 - 1.0), hiển thị tuỳ chọn |
| `finalText` | Câu đã censor + chuẩn hoá, ưu tiên hiển thị thay `comment` |

---

## 4. Luồng xử lý tạo review (FE)

```tsx
// Khi user submit review
async function handleCreateReview(data: CreateReviewRequest) {
  try {
    const response = await api.post("/api/v1/reviews", data);

    // AI trả về decision
    if (response.decision === "REJECTED") {
      showToast("Bình luận vi phạm quy định. Vui lòng chỉnh sửa.");
      return;
    }

    if (response.decision === "SPOILER_WARNING") {
      showToast("Bình luận đã được đăng. Cảnh báo spoiler đã được áp dụng.");
    } else {
      showToast("Bình luận đã được đăng thành công!");
    }

    // Thêm vào danh sách review
    addReviewToList(response);

  } catch (error) {
    handleError(error);
  }
}
```

---

## 5. Điều kiện hiển thị (Pseudocode)

```
IF review.status != "ACTIVE" THEN
  -- REJECTED (HIDDEN): không hiển thị
  RETURN ""

IF review.decision == "APPROVED" THEN
  -- Comment bình thường
  RETURN normalCard(review)

IF review.decision == "SPOILER_WARNING" THEN
  -- Comment có spoiler
  IF userClickedReveal THEN
    RETURN spoilerCard(review, revealed=true)
  ELSE
    RETURN spoilerCard(review, revealed=false)
```
