# Retrofit Specification

## Overview

SnapMind uses Retrofit for three external APIs that count as remote enrichment features:

| API | Purpose | Scoring |
| --- | --- | --- |
| Gemini API | Memo sentence recommendation | API 1/3 |
| Google Cloud Vision API | Image label detection for auto-tagging | API 2/3 |
| YouTube Data API v3 | Video search by OCR-extracted title | API 3/3 |

## Base Rules

- Core on-device features (import, OCR, TFLite, local tags, search, memo editing) do not require network.
- Remote API features degrade gracefully offline and can be disabled independently.
- Retrofit calls run on the IO dispatcher.
- DTOs are mapped to domain models before reaching ViewModels.
- All errors are converted to `AppResult.Error`.
- API keys are stored outside version control, loaded from `local.properties`/`BuildConfig` for demo builds or private app preferences for runtime entry.
- Release logs never include API keys, image bytes, OCR text, memo body, source URI, or app-private paths.

## Client Configuration

| Setting | Value |
| --- | --- |
| Connect timeout | 10 seconds |
| Read timeout | 20 seconds |
| Write timeout | 20 seconds |
| Serialization | Kotlinx Serialization or Moshi |
| Logging | Debug builds only, headers/body redacted |

---

## 1. Gemini API — Memo Recommendation

### Endpoint

Default model is configurable. Use `gemini-2.5-flash` for the initial implementation unless the team explicitly updates the model.

```http
POST https://generativelanguage.googleapis.com/v1beta/models/{MODEL}:generateContent
x-goog-api-key: {API_KEY}
```

### Request

Send a downsampled, re-encoded image payload. Do not include OCR full text, memo body, source URI, or internal file path in the prompt.

```json
{
  "contents": [
    {
      "parts": [
        { "text": "이 이미지를 저장한 이유를 한 문장(50자 이내 한국어)으로 추천해 주세요." },
        { "inlineData": { "mimeType": "image/jpeg", "data": "{BASE64_DOWNSAMPLED_IMAGE}" } }
      ]
    }
  ]
}
```

### Response

```json
{
  "candidates": [
    {
      "content": {
        "parts": [
          { "text": "카페 메뉴 가격을 나중에 참고하기 위해 저장했습니다." }
        ]
      }
    }
  ]
}
```

### Usage

- Called after local processing when Gemini memo recommendation is enabled.
- The recommended sentence is stored as `MemoEntity.geminiSuggestion`.
- User can accept (tap chip), edit, or dismiss.
- `geminiMemoStatus` transitions: `SKIPPED` or `PENDING` → `RUNNING` → `SUGGESTED` → `ACCEPTED` / `DISMISSED`.
- On failure: hide suggestion UI, set status `FAILED`, user writes memo manually.

---

## 2. Google Cloud Vision API

### Endpoint

```http
POST https://vision.googleapis.com/v1/images:annotate?key={API_KEY}
```

### Request

Send a downsampled, re-encoded image payload. Request `LABEL_DETECTION` only; OCR remains local through ML Kit.

```json
{
  "requests": [
    {
      "image": { "content": "{BASE64_DOWNSAMPLED_IMAGE}" },
      "features": [
        { "type": "LABEL_DETECTION", "maxResults": 10 }
      ]
    }
  ]
}
```

### Response (label annotations)

```json
{
  "responses": [
    {
      "labelAnnotations": [
        { "description": "Food", "score": 0.97 },
        { "description": "Receipt", "score": 0.91 }
      ]
    }
  ]
}
```

### Usage

- Called after TFLite classification when remote visual tagging is enabled.
- Labels with `score >= 0.80` are persisted as `VisionLabelEntity` and converted to tags.
- Tags are merged with OCR-derived and TFLite-derived tags; duplicates are de-duplicated.
- On failure: log sanitized error, set `visionLabelStatus = FAILED`, continue without Vision tags.

---

## 3. YouTube Data API v3

### Endpoint

```http
GET https://www.googleapis.com/youtube/v3/search?part=snippet&q={TITLE}&type=video&maxResults=1&key={API_KEY}
```

### Response

```json
{
  "items": [
    {
      "id": { "videoId": "dQw4w9WgXcQ" },
      "snippet": {
        "title": "Video Title",
        "thumbnails": { "default": { "url": "..." } }
      }
    }
  ]
}
```

### Usage

- Triggered only when TFLite category is `youtube`, a candidate title exists, and YouTube deep-linking is enabled.
- Send only the selected OCR-derived candidate title as the `q` query parameter.
- First result's `videoId` generates the deep-link: `https://www.youtube.com/watch?v={videoId}`.
- Persist the result in `YoutubeLinkEntity`.
- Deep-link button "▶ 영상 바로 이동" is displayed in detail screen.
- On failure or no result: hide deep-link button; show optional retry.

---

## Error Response Format (internal)

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}
```

Common API errors:

| Error | Cause |
| --- | --- |
| `NetworkUnavailable` | No internet connection |
| `ApiQuotaExceeded` | Daily quota limit reached |
| `ApiUnauthorized` | Invalid or missing API key |
| `ApiTimeout` | Request exceeded timeout |
| `ApiParseError` | Unexpected response structure |
| `RemoteFeatureDisabled` | API setting is disabled or not configured |
