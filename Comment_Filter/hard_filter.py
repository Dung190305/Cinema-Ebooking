import re
import time
import unicodedata
from dataclasses import dataclass, field
from typing import List


@dataclass
class Layer1Result:
    valid: bool
    cleaned_text: str
    censored_words: List[str]
    profanity_count: int
    profanity_ratio: float
    process_time_ms: float
    reject_reason: str = ""


# ─── BẢNG TỪ TỤC CƠ SỞ ──────────────────────────────────────────────────────
_PROFANITY_TABLE: List[dict] = [
    {
        "base": "địt",
        "variants": ["địt", "đjt", "đj", "đjt m", "đjt mẹ", "địt mẹ", "đm", "dm", "đmm", "đụ", "đụ m", "đụ má", "đmẹ"],
        "level": 3,
    },
    {
        "base": "lồn",
        "variants": ["lồn", "lon", "l0n", "lòn", "ln", "lồn c", "lồn m", "lồn má", "lồn mẹ", "lôn"],
        "level": 3,
    },
    {
        "base": "cặc",
        "variants": ["cặc", "cac", "cacc", "cặk"],
        "level": 3,
    },
    {
        "base": "buồi",
        "variants": ["buồi", "buoi", "bu0i"],
        "level": 3,
    },
    {
        "base": "dkm",
        "variants": ["dkm", "đkm", "dk", "đk", "đcm", "dcm", "đc", "dc", "đụ má", "đmẹ"],
        "level": 3,
    },
    {
        "base": "đĩ",
        "variants": ["đĩ", "đĩ má"],
        "level": 2,
    },
    {
        "base": "súc vật",
        "variants": ["súc vật", "suc vat", "súc vat", "thú vật"],
        "level": 2,
    },
    {
        "base": "vl",
        "variants": ["vl", "vkl", "vcl", "vk"],
        "level": 3,
    },
]

# Tạo dict lookup nhanh
_VARIANT_TO_BASE: dict = {}
_BASE_TO_LEVEL: dict = {}
for entry in _PROFANITY_TABLE:
    base = entry["base"]
    level = entry["level"]
    for variant in entry["variants"]:
        _VARIANT_TO_BASE[variant.lower()] = base
        _BASE_TO_LEVEL[base] = level

_VARIANT_LENGTHS: dict = {v: len(v) for v in _VARIANT_TO_BASE.keys()}


# ─── LEVENSHTEIN DISTANCE ──────────────────────────────────────────────────────
def _levenshtein(s1: str, s2: str) -> int:
    if len(s1) < len(s2):
        return _levenshtein(s2, s1)
    if len(s2) == 0:
        return len(s1)
    prev = list(range(len(s2) + 1))
    curr = [0] * (len(s2) + 1)
    for i, c1 in enumerate(s1):
        curr[0] = i + 1
        for j, c2 in enumerate(s2):
            ins = prev[j + 1] + 1
            dele = curr[j] + 1
            sub = prev[j] + (c1 != c2)
            curr[j + 1] = min(ins, dele, sub)
        prev, curr = curr, prev
    return prev[len(s2)]


_THRESHOLD_RATIO = 0.15  # cho phép sai 15% ký tự


def _find_profanity_in_word(word: str) -> List[tuple]:
    """Tìm từ tục trong 1 token (dùng Levenshtein + fuzzy).

    Quy tắc:
    - word_len ≤ 4 HOẶC variant_len ≤ 4: bỏ qua fuzzy hoàn toàn.
      Từ ngắn có quá nhiều từ bình thường trong tiếng Việt chỉ khác 1 ký tự
      (buồn/buồi, bạn/bán, ...) → chỉ dùng exact match (đã xử lý ở bước 10a).
    - word_len ≥ 5 VÀ variant_len ≥ 5: dùng threshold 15% độ dài variant.
    """
    w = word.lower()
    word_len = len(w)
    results = []
    for variant, length in _VARIANT_LENGTHS.items():
        # Không fuzzy cho từ ngắn — quá nhiều false positive tiếng Việt
        if word_len <= 4 or length <= 4:
            continue

        d = _levenshtein(w, variant)

        # Từ đủ dài: dùng threshold % độ dài
        threshold = max(1, int(length * _THRESHOLD_RATIO))
        if d > threshold:
            continue

        base = _VARIANT_TO_BASE[variant]
        level = _BASE_TO_LEVEL[base]
        results.append((base, level, d))

    if results:
        results.sort(key=lambda x: (x[1], x[2]))
        return [results[0]]
    return []


# ─── TEENCODE MAP ─────────────────────────────────────────────────────────────
_TEENCODE_MAP: dict = {
    # Không
    "ko": "không", "k": "không", "kg": "không", "hk": "không", "k0": "không",
    "dc": "được", "đc": "được", "dk": "được", "đk": "được",
    # Mình / Tôi / Em
    "mk": "mình", "mn": "mọi người", "mik": "mình", "m": "mình", "t": "tôi", "e": "em",
    # Bạn / Người / Nói / Biết
    "bn": "bạn", "ngta": "người ta", "ns": "nói", "nt": "như thế",
    "nc": "nói chung", "bít": "biết", "bit": "biết", "bic": "biết",
    # Cũng / Với / Như thế nào / Nhưng mà
    "cx": "cũng", "cg": "cũng", "vs": "với", "v": "vậy",
    "ntn": "như thế nào", "nma": "nhưng mà", "nhma": "nhưng mà", "nm": "như mà",
    # Thời gian / Hôm nay
    "bh": "bây giờ", "bg": "bây giờ", "hn": "hôm nay",
    "hnay": "hôm nay", "hnaj": "hôm nay",
    # Làm / Quá / Nghe / Rồi / Gì / Vậy
    "lm": "làm", "qá": "quá", "wá": "quá", "qa": "quá", "q": "quá",
    "nge": "nghe", "r": "rồi", "j": "gì", "jv": "gì vậy", "g": "gì",
    "z": "vậy", "v": "vậy", "zậy": "vậy",
    # Phim / Xem / Đẹp
    "p": "phim", "x": "xem", "dep": "đẹp", "phj": "phim", "pjm": "phim",
    # Khác
    "bt": "bình thường", "bth": "bình thường",
    "nv": "nhân vật", "kq": "kết quả", "nvc": "nhân vật chính",
    "tr": "trời", "ak" : "á"
}


def _replace_teencode(text: str) -> str:
    """Thay teencode bằng tiếng Việt chuẩn, xử lý cả khi teencode đứng trước dấu câu."""
    tokens = re.findall(r"[.,!?;:ạ]+|" + r"[^\s.,!?;:ạ]+" + r"|" + r"\s+", text)
    out = []
    for token in tokens:
        if re.fullmatch(r"[.,!?;:ạ]+", token) or re.search(r"\s", token):
            out.append(token)
        else:
            replaced = _TEENCODE_MAP.get(token.lower(), token)
            out.append(replaced)
    return "".join(out).strip()


# ─── SPAM & LINK PATTERNS ──────────────────────────────────────────────────────
_SPAM_PATTERNS = [
    r"(.)\1{5,}",               # ký tự lặp 6+ lần: "haaaaahaha"
    r"[A-Z]{8,}",               # viết hoa dài liên tiếp
    r"https?://\S+",            # URL
    r"www\.\S+",
    r"\b(free|win|click|link|bấm|nhấn|tải|tặng)\b.*\b(link|url|code|now)\b",
    r"( casino | gambling | betting )\b",
    r"\b(spam|quảng cáo)\b",
    r"http[s]?://[^\s]{30,}",   # URL rất dài
    r"[!@#$%^&*]{5,}",          # ký tự đặc biệt lặp
    r"^\s*[\W_]{5,}\s*$",       # toàn ký tự đặc biệt
    r"\b(buy|sell|order|giảm giá|khuyến mãi)\b.*\b(link|code|now|click)\b",
    r"\b(🔗|💰|🎁|🎉|👉|👉👉|bit\.ly|tinyurl)\b",
]

_SPAM_REGEX = re.compile("|".join(_SPAM_PATTERNS), re.IGNORECASE)

# Pattern cắt từ tục bằng dấu cách
_PROFANITY_SPACE_PATTERNS: List[re.Pattern] = []
for entry in _PROFANITY_TABLE:
    for v in entry["variants"]:
        escaped = re.escape(v)
        _PROFANITY_SPACE_PATTERNS.append(
            re.compile(r"(?<![aăâáắấàảãạằầấẩẫậđẹêéếềểễệiíìỉĩịoôốộờớởỡợuúùủũụưứừửữựyýỳỷỹỵA-Z0-9])" + escaped +
                       r"(?![aăâáắấàảãạằầấẩẫậđẹêéếềểễệiíìỉĩịoôốộờớởỡợuúùủũụưứừửữựyýỳỷỹỵA-Z0-9])", re.IGNORECASE)
        )


# ─── XỬ LÝ DẤU CÂU TIẾNG VIỆT ───────────────────────────────────────────────
_VOWELS_UPPER = "ÁÀẢÃẠÉÈẺẼẸÍÌỈĨỊÓÒỎÕỌÚÙỦŨỤÝỲỶỸỴ"
_VOWELS_LOWER = "áàảãạéèẻẽẹíìỉĩịóòỏõọúùủũụýỳỷỹỵ"
_DIACRITIC_TO_STRIP = str.maketrans(
    _VOWELS_UPPER + _VOWELS_LOWER,
    (
        "AAAAA"  # ÁÀẢÃẠ
        "EEEEE"  # ÉÈẺẼẸ
        "IIIII"  # ÍÌỈĨỊ
        "OOOOO"  # ÓÒỎÕỌ
        "UUUUU"  # ÚÙỦŨỤ
        "YYYYY"  # ÝỲỶỸỴ
        "aaaaa"
        "eeeee"
        "iiiii"
        "ooooo"
        "uuuuu"
        "yyyyy"
    ),
)


def _strip_diacritics(text: str) -> str:
    return text.translate(_DIACRITIC_TO_STRIP)


# ─── THAY THẾ TỪ TỤC BẰNG DẤU * ──────────────────────────────────────────────
_REPLACE_MAP: dict = {v: entry["base"] for entry in _PROFANITY_TABLE for v in entry["variants"]}

_REPLACE_SPACE_PATTERNS: List[tuple] = [
    (re.compile(r"(?<![a-zA-Z0-9ăâáắấàảãạằầấẩẫậđẹêéếềểễệiíìỉĩịoôốộờớởỡợuúùủũụưứừửữựyýỳỷỹỵ])" + re.escape(v) +
                r"(?![a-zA-Z0-9ăâáắấàảãạằầấẩẫậđẹêéếềểễệiíìỉĩịoôốộờớởỡợuúùủũụưứừửữựyýỳỷỹỵ])", re.IGNORECASE), entry["base"])
    for v, entry in [(v, next(e for e in _PROFANITY_TABLE if v in e["variants"])) for v in _VARIANT_TO_BASE]
]


def _censor_word(word: str, base_word: str) -> str:
    """Thay thế từ tục bằng dấu *, giữ nguyên độ dài."""
    return "".join(c if c.isspace() else "*" for c in word)


# ─── SENTENCE NORMALIZATION ───────────────────────────────────────────────────
def _normalize_sentences(text: str) -> str:
    """
    Chuẩn hóa văn bản theo quy tắc:
    1. Xóa khoảng trắng thừa trước dấu câu (., !, ?, ...)
    2. Đảm bảo có đúng 1 khoảng trắng sau dấu câu kết thúc câu (., !, ?)
    3. Viết hoa chữ cái đầu mỗi câu (kể cả câu đầu tiên)

    Ví dụ:
      "phim hay lắm .xem ngay đi!tôi thích"
      → "Phim hay lắm. Xem ngay đi! Tôi thích"
    """
    # Bước 1: Xóa khoảng trắng thừa trước dấu câu
    # Vd: "hay lắm . Tốt" → "hay lắm. Tốt"
    text = re.sub(r"\s+([.!?,;:])", r"\1", text)

    # Bước 2: Đảm bảo đúng 1 khoảng trắng sau dấu câu kết thúc câu (., !, ?)
    # Chỉ áp dụng khi sau dấu câu là chữ cái (không phải số, dấu câu khác, cuối chuỗi)
    # Vd: "hay.Tốt" → "hay. Tốt" | "hay.  Tốt" → "hay. Tốt"
    text = re.sub(r"([.!?])(\s*)(?=[^\s\d.!?])", lambda m: m.group(1) + " ", text)

    # Bước 3: Viết hoa chữ cái đầu mỗi câu
    # Tách theo ranh giới câu: sau ., !, ? (đã có space ở bước 2)
    # Dùng re.sub để thay thế ký tự đầu câu → upper
    def _capitalize_first_alpha(s: str) -> str:
        """Viết hoa ký tự alpha đầu tiên trong chuỗi s."""
        for i, ch in enumerate(s):
            if ch.isalpha():
                return s[:i] + ch.upper() + s[i + 1:]
        return s

    # Tách thành các đoạn: [nội_dung_câu, dấu_câu_+_space, nội_dung_câu, ...]
    # Pattern: tách tại vị trí SAU "[.!?] " (lookbehind)
    parts = re.split(r"(?<=[.!?] )", text)
    normalized_parts = [_capitalize_first_alpha(p) for p in parts]
    text = "".join(normalized_parts)

    # Bước 4: Viết hoa ký tự đầu tiên của toàn bộ chuỗi (câu đầu)
    text = _capitalize_first_alpha(text)

    return text


# ─── MAIN FILTER ──────────────────────────────────────────────────────────────
_TOKEN_REGEX = re.compile(
    r"[\wăâáắấàảãạằầấẩẫậđẹêéếềểễệiíìỉĩịoôốộờớởỡợuúùủũụưứừửữựyýỳỷỹỵ]+"
    r"|[^\s\wăâáắấàảãạằầấẩẫậđẹêéếềểễệiíìỉĩịoôốộờớởỡợuúùủũụưứừửữựyýỳỷỹỵ]+"
    r"|\s+"
)


def normalize_layer1(text: str) -> Layer1Result:
    """
    Layer 1 filter:
    1. Strip null bytes & basic length checks
    2. Unicode normalization (NFC)
    3. URL strip, emoji/special-char strip, repeated-char collapse
    4. Spam / link detection
    5. Teencode → standard Vietnamese
    6. Profanity detection (exact + fuzzy via Levenshtein)
    7. Censor profanity & threshold check
    8. Sentence normalization (spacing + capitalization)
    9. Return stats
    """
    start = time.perf_counter()

    # 1. Strip null bytes
    text = text.replace("\x00", "").strip()
    if not text:
        return _reject(start, text, "EMPTY_INPUT")

    # 2. Length checks
    if len(text) < 3:
        return _reject(start, text, "TOO_SHORT")
    if len(text) > 2000:
        return _reject(start, text, "TOO_LONG")

    # 3. NFC normalize (combining chars)
    text = unicodedata.normalize("NFC", text)

    # 4. URL strip
    text = re.sub(r"https?://\S+|www\.\S+", " ", text)

    # 5. Emoji & special-char strip (keep basic punctuation)
    text = re.sub(r"[^\w\s,.!?;:()\-ăâáắấàảãạằầấẩẫậđẹêéếềểễệiíìỉĩịoôốộờớởỡợuúùủũụưứừửữựyýỳỷỹỵ]", " ", text)

    # 6. Collapse repeated characters (keep 1): "vlll" → "vl", "haaaaa" → "ha"
    text = re.sub(r"(.)\1{2,}", r"\1", text)

    # 7. Normalize whitespace
    text = " ".join(text.split())
    if not text:
        return _reject(start, text, "EMPTY_AFTER_CLEAN")

    # 8. Spam & link detection
    spam_match = _SPAM_REGEX.search(text)
    if spam_match:
        return _reject(start, "", "SPAM")

    # 9. Teencode → standard Vietnamese
    text = _replace_teencode(text)

    # 10. Word-level profanity detection
    tokens = _TOKEN_REGEX.findall(text)
    cleaned_tokens = []
    censored_words = []
    profanity_count = 0
    word_count = 0

    for token in tokens:
        if token.isspace():
            cleaned_tokens.append(token)
            continue
        word_count += 1
        lower = token.lower()

        # 10a. Exact match
        if lower in _VARIANT_TO_BASE:
            base = _VARIANT_TO_BASE[lower]
            profanity_count += 1
            censored_words.append(base)
            cleaned_tokens.append(_censor_word(token, base))
            continue

        # 10b. Fuzzy match (Levenshtein) – từ đủ dài >= 3
        if len(token) >= 3:
            fuzzy = _find_profanity_in_word(token)
            if fuzzy:
                base, level, dist = fuzzy[0]
                profanity_count += 1
                censored_words.append(f"{base} (~{token})")
                cleaned_tokens.append(_censor_word(token, base))
                continue

        cleaned_tokens.append(token)

    cleaned_text = "".join(cleaned_tokens)
    profanity_ratio = profanity_count / max(word_count, 1)

    # 11. Threshold check
    if profanity_ratio >= 0.5 and profanity_count >= 3:
        return _reject(
            start, cleaned_text, "HIGH_PROFANITY",
            censored_words=censored_words,
            profanity_count=profanity_count,
            profanity_ratio=profanity_ratio,
        )

    # 12. Sentence normalization: chuẩn khoảng cách sau dấu câu + viết hoa đầu câu
    cleaned_text = _normalize_sentences(cleaned_text)

    elapsed = (time.perf_counter() - start) * 1000
    return Layer1Result(
        valid=True,
        cleaned_text=cleaned_text,
        censored_words=censored_words,
        profanity_count=profanity_count,
        profanity_ratio=profanity_ratio,
        process_time_ms=elapsed,
        reject_reason="",
    )


def _reject(
    start: float,
    cleaned_text: str,
    reason: str,
    censored_words: List[str] = None,
    profanity_count: int = 0,
    profanity_ratio: float = 0.0,
) -> Layer1Result:
    elapsed = (time.perf_counter() - start) * 1000
    return Layer1Result(
        valid=False,
        cleaned_text=cleaned_text or "",
        censored_words=censored_words or [],
        profanity_count=profanity_count,
        profanity_ratio=profanity_ratio,
        process_time_ms=elapsed,
        reject_reason=reason,
    )


# ─── THÔNG TIN LOG ĐỂ DEBUG ───────────────────────────────────────────────────
def explain_filter(text: str) -> dict:
    """
    Trả về dict chi tiết để debug/investigate.
    Dùng cho development & testing.
    """
    result = normalize_layer1(text)

    # Token breakdown
    tokens = _TOKEN_REGEX.findall(text)
    token_report = []
    for t in tokens:
        if t.isspace():
            continue
        lower = t.lower()
        if lower in _VARIANT_TO_BASE:
            base = _VARIANT_TO_BASE[lower]
            token_report.append({"token": t, "status": "PROFANITY", "base": base})
        else:
            fuzzy = _find_profanity_in_word(t)
            if fuzzy:
                base, level, dist = fuzzy[0]
                token_report.append({"token": t, "status": "FUZZY", "base": base, "distance": dist})
            else:
                token_report.append({"token": t, "status": "CLEAN"})

    spam_match = _SPAM_REGEX.search(text)

    return {
        "original": text,
        "valid": result.valid,
        "cleaned": result.cleaned_text,
        "reject_reason": result.reject_reason,
        "profanity_count": result.profanity_count,
        "profanity_ratio": round(result.profanity_ratio, 4),
        "censored_words": result.censored_words,
        "process_time_ms": round(result.process_time_ms, 3),
        "is_spam": spam_match is not None,
        "spam_match": spam_match.group() if spam_match else None,
        "token_report": token_report,
    }


if __name__ == "__main__":
    pass