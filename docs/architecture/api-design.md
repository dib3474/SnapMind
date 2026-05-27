# API Design

## Goal

Define Retrofit conventions for SnapMind's three optional remote enrichment APIs: Google Cloud Vision, Gemini, and YouTube Data API v3.

The app is local-first. OCR, TFLite classification, import, search, tags, and memo editing must work without network or API keys.

## Retrofit Services

```kotlin
interface VisionApi {
    @POST("v1/images:annotate")
    suspend fun annotate(
        @Query("key") apiKey: String,
        @Body request: VisionAnnotateRequestDto
    ): VisionAnnotateResponseDto
}

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Path("model") model: String,
        @Body request: GeminiGenerateContentRequestDto
    ): GeminiGenerateContentResponseDto
}

interface YoutubeApi {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 1,
        @Query("key") apiKey: String
    ): YoutubeSearchResponseDto
}
```

## Remote Feature Gates

- Vision API runs only when remote visual tagging is enabled and a key is configured.
- Gemini API runs only when memo recommendation is enabled and a key is configured.
- YouTube API runs only when the top TFLite category is `youtube`, a candidate title exists, deep-linking is enabled, and a key is configured.
- Disabled or not-applicable remote work sets the corresponding status to `SKIPPED`, not `FAILED`.

## DTO Rules

- DTOs live in `data/remote/dto`.
- DTOs must not be exposed to ViewModels.
- Mapping functions convert DTOs to domain models.
- Nullable API fields must be handled explicitly during mapping.
- Remote request DTOs must contain only the minimized payload described in `docs/architecture/privacy-security.md`.

## Result Wrapper

Repository methods should convert network responses to `AppResult`.

```kotlin
sealed interface NetworkError {
    data object Offline : NetworkError
    data object Timeout : NetworkError
    data object QuotaExceeded : NetworkError
    data object Unauthorized : NetworkError
    data class Http(val code: Int, val body: String?) : NetworkError
    data class Serialization(val message: String) : NetworkError
}
```

## Error Handling

| Case | Handling |
| --- | --- |
| No network | Continue local processing and mark remote step retryable or skipped |
| Missing/disabled API setting | Mark remote step `SKIPPED` |
| HTTP 401/403 | Disable remote-only feature for the current run and surface auth/config issue in Settings |
| HTTP 429/quota | Mark step `FAILED`; allow manual retry later |
| HTTP 5xx | Retry with exponential backoff where safe |
| Serialization error | Mark the remote step `FAILED` and keep local outputs |
| Timeout | Cancel request and keep local flow responsive |

## API Constraints

- Do not require login for core app usage.
- All remote calls must be cancellable.
- Retrofit clients must use configured timeouts.
- Release logging must not include image bytes, OCR text, memo body, API keys, source URI, or app-private file paths.
