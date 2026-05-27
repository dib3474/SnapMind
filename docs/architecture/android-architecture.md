# Android Architecture

## Architecture Style

SnapMind uses MVVM with a repository-driven data layer and use cases for feature workflows.

## Package Hierarchy

```text
com.example.snapmind
├── app
│   ├── MainActivity.kt
│   ├── ShareActivity.kt
│   ├── DetailActivity.kt
│   └── SnapMindApplication.kt
├── core
│   ├── common
│   ├── coroutine
│   ├── worker
│   ├── result
│   └── ui
├── data
│   ├── local
│   │   ├── dao
│   │   ├── entity
│   │   └── converter
│   ├── remote
│   │   ├── gemini
│   │   │   ├── GeminiApiService.kt
│   │   │   └── dto
│   │   ├── vision
│   │   │   ├── VisionApiService.kt
│   │   │   └── dto
│   │   ├── youtube
│   │   │   ├── YouTubeApiService.kt
│   │   │   └── dto
│   │   └── common
│   ├── repository
│   └── file
├── worker
│   ├── LocalMemoryProcessingWorker.kt
│   ├── RemoteEnrichmentWorker.kt
│   ├── AutoTaggingWorker.kt
│   └── CleanupWorker.kt
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── ai
│   ├── ocr
│   ├── classifier
│   └── tagging
└── feature
    ├── importimage
    ├── home
    ├── favorites
    ├── tagbrowse
    ├── memorydetail
    ├── search
    ├── trash
    └── settings
```

## MVVM Rules

- Activities/fragments/composables render state only.
- ViewModels expose immutable UI state using `StateFlow`.
- UI events are handled through explicit methods such as `onImportClicked()`, `onSearchQueryChanged(query)`.
- Repositories return domain models or `Flow` streams, not Room entities.
- Use cases contain workflow logic and keep ViewModels small.

## State Management

Use a single state holder per screen:

```kotlin
data class MemoryListUiState(
    val isLoading: Boolean = false,
    val items: List<MemoryListItemUiModel> = emptyList(),
    val query: String = "",
    val activeFilters: MemoryFilters = MemoryFilters(),
    val errorMessage: String? = null
)
```

## Coroutine Strategy

| Layer | Scope | Dispatcher |
| --- | --- | --- |
| UI | `lifecycleScope` for one-off UI collection | Main |
| ViewModel | `viewModelScope` | Main for state, injected dispatchers for work |
| Repository | suspend functions and `Flow` | IO |
| Worker | WorkManager coroutine worker | Injected dispatchers |
| OCR | suspend processor | Default or dedicated CPU dispatcher |
| TFLite | suspend processor | Default or dedicated CPU dispatcher |
| Room | DAO suspend/Flow | IO handled by Room, call from IO where batching |

## Repository Pattern

Repositories coordinate:

- Room DAO operations.
- File persistence and URI copying.
- OCR processor execution (ML Kit).
- TFLite classifier execution.
- Optional Google Cloud Vision API calls (image labeling).
- Optional Gemini API calls (memo recommendation).
- Optional YouTube Data API v3 calls (video title search).
- Auto-tag rule execution (OCR + TFLite + Vision labels).

Repository interfaces live in `domain/repository`; implementations live in `data/repository`.

## Dependency Injection

Use Hilt when dependency setup begins.

Required modules:

- `DatabaseModule`
- `DaoModule`
- `NetworkModule` (Retrofit clients for Gemini, Vision, YouTube APIs)
- `ApiModule` (GeminiApiService, VisionApiService, YouTubeApiService)
- `RepositoryModule`
- `AiModule`
- `DispatcherModule`
- `WorkerModule`

## Error Model

Use a project-level result wrapper:

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}
```

Common errors:

- `PermissionDenied`
- `UnsupportedImageType`
- `FileNotFound`
- `OcrFailed`
- `ClassificationFailed`
- `VisionApiFailed`
- `GeminiApiFailed`
- `YouTubeApiFailed`
- `ApiQuotaExceeded`
- `DatabaseError`
- `NetworkUnavailable`

## Background Work

Use WorkManager for durable local processing, optional remote enrichment, tagging, and cleanup work after import. See `docs/architecture/background-processing.md`.

## Privacy Boundary

UI and logs must not expose OCR text, memo body, source URI, or absolute image paths unnecessarily. See `docs/architecture/privacy-security.md`.
