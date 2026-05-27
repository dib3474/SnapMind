# Database Design

## Goal

Define Room database entities, relationships, indexes, FTS configuration, and query patterns for local-first memory management.

## Database Name

`snapmind.db`

## Entities

| Entity | Purpose |
| --- | --- |
| `MemoryItemEntity` | Main image memory record |
| `OcrTextEntity` | ML Kit OCR full text and block-level extraction metadata |
| `TagEntity` | Unique normalized tag definitions |
| `MemoryTagCrossRef` | N:M relationship between memories and tags, including assignment/source metadata |
| `ClassificationEntity` | TFLite CNN category prediction results |
| `VisionLabelEntity` | Google Cloud Vision API label results per memory |
| `MemoEntity` | User-authored and Gemini-recommended memo content |
| `YoutubeLinkEntity` | YouTube Data API result used by the detail deep-link button |
| `MemorySearchFts` | FTS4 virtual table over OCR text, memo text, tags, categories, and YouTube title metadata |

## Relationships

- One `MemoryItemEntity` has zero or one `OcrTextEntity`.
- One `MemoryItemEntity` has zero or many `ClassificationEntity` rows.
- One `MemoryItemEntity` has zero or many `VisionLabelEntity` rows.
- One `MemoryItemEntity` has zero or one `MemoEntity`.
- One `MemoryItemEntity` has zero or one `YoutubeLinkEntity`.
- One `MemoryItemEntity` has many `TagEntity` rows through `MemoryTagCrossRef` (N:M).

## Key Fields

### MemoryItemEntity

| Field | Type | Notes |
| --- | --- | --- |
| `id` | Long PK | Auto-generated |
| `imageUri` | String UNIQUE | App-private URI |
| `sourceUri` | String? | Original shared URI for debug/source metadata; do not expose in release logs |
| `mimeType` | String? | Validated imported MIME type |
| `contentHash` | String? UNIQUE | Optional SHA-256 hash for duplicate detection |
| `createdAt` | Long | Epoch millis |
| `updatedAt` | Long | Epoch millis |
| `ocrStatus` | Enum | PENDING / RUNNING / SUCCESS / FAILED |
| `classificationStatus` | Enum | PENDING / RUNNING / SUCCESS / FAILED |
| `visionLabelStatus` | Enum | PENDING / RUNNING / SUCCESS / FAILED / SKIPPED |
| `geminiMemoStatus` | Enum | PENDING / RUNNING / SUGGESTED / ACCEPTED / DISMISSED / FAILED / SKIPPED |
| `youtubeLinkStatus` | Enum | PENDING / RUNNING / SUCCESS / FAILED / SKIPPED |
| `taggingStatus` | Enum | PENDING / RUNNING / SUCCESS / FAILED |
| `isFavorite` | Boolean | Favorites tab filter |
| `deletedAt` | Long? | Soft delete (trash); null = active |

### TagEntity

| Field | Type | Notes |
| --- | --- | --- |
| `id` | Long PK | Auto-generated |
| `name` | String UNIQUE | Normalized lowercase tag key |
| `displayName` | String | UI label |
| `isUserManaged` | Boolean | True for user-created/editable tags |
| `isArchived` | Boolean | Hide from default drawer lists when true |
| `createdAt` | Long | Epoch millis |
| `updatedAt` | Long | Epoch millis |

### MemoryTagCrossRef

| Field | Type | Notes |
| --- | --- | --- |
| `memoryId` | Long FK | Composite PK |
| `tagId` | Long FK | Composite PK |
| `assignedBy` | Enum | AUTO / USER |
| `sourceTypes` | String | Comma-separated enum set: OCR / TFLITE / VISION / USER / SYSTEM |
| `removedAt` | Long? | Null means active; non-null preserves "user removed generated tag" history |
| `createdAt` | Long | Epoch millis |

### VisionLabelEntity

| Field | Type | Notes |
| --- | --- | --- |
| `id` | Long PK | |
| `memoryId` | Long FK | |
| `label` | String | e.g. "Food", "Receipt" |
| `score` | Float | Vision API confidence score |
| `createdAt` | Long | Epoch millis |

### MemoEntity

| Field | Type | Notes |
| --- | --- | --- |
| `memoryId` | Long PK/FK | |
| `body` | String | Final user-saved memo text |
| `geminiSuggestion` | String? | Gemini API recommendation; null if not available |
| `updatedAt` | Long | |

### YoutubeLinkEntity

| Field | Type | Notes |
| --- | --- | --- |
| `memoryId` | Long PK/FK | |
| `videoId` | String | YouTube video ID |
| `title` | String? | API result title |
| `url` | String | `https://www.youtube.com/watch?v={videoId}` |
| `createdAt` | Long | Epoch millis |

## FTS Configuration

Room FTS virtual table for fast full-text search. Use a denormalized table so tag/category searches do not require fragile FTS joins:

```kotlin
@Fts4(notIndexed = ["memoryId"])
@Entity(tableName = "memory_search_fts")
data class MemorySearchFts(
    val memoryId: Long,
    val ocrText: String,
    val memoBody: String,
    val tagText: String,
    val categoryText: String,
    val youtubeTitle: String
)
```

FTS supports searches across:

- OCR full text
- Memo body
- Active tag display names
- Top classification/category labels
- YouTube title metadata

The repository refreshes the FTS row in the same transaction that changes OCR text, memo body, tag assignments, classification rows, or YouTube link metadata. Exact tag/category filters still use indexed joins to avoid ambiguous text matches.

## Index Strategy

| Table | Index | Reason |
| --- | --- | --- |
| `memory_items` | `createdAt` | Date sorting |
| `memory_items` | `imageUri` UNIQUE | Prevent duplicate imports |
| `memory_items` | `contentHash` UNIQUE nullable | Detect duplicate imports after local copy |
| `memory_items` | `deletedAt` | Trash / active filter |
| `memory_items` | `isFavorite` | Favorites tab |
| `ocr_texts` | `memoryId` UNIQUE | Fast join from memory |
| `tags` | `name` UNIQUE | Tag lookup and de-duplication |
| `tags` | `isUserManaged`, `isArchived` | Tag management and popular tag lists |
| `memory_tag_cross_refs` | `memoryId`, `tagId` | N:M joins |
| `memory_tag_cross_refs` | `tagId`, `removedAt` | Tag usage counts for top-3 shortcuts |
| `classifications` | `memoryId`, `label` | Category filtering |
| `classifications` | `label` | Drawer category counts |
| `vision_labels` | `memoryId` | Join from memory |
| `vision_labels` | `label`, `score` | High-confidence label lookup |
| `memos` | `memoryId` UNIQUE | Detail screen memo loading |
| `youtube_links` | `memoryId` UNIQUE | Detail deep-link lookup |

## Search Queries

| Query Type | Strategy |
| --- | --- |
| Full-text (OCR + memo + tags + category + YouTube title) | Room FTS `MATCH` query on `memory_search_fts` |
| Tag filter | Join `memory_tag_cross_refs` + `tags` |
| Category filter | Join `classifications` by `label` |
| Date range | `WHERE createdAt BETWEEN ? AND ?` |
| Favorites | `WHERE isFavorite = 1` |
| Trash | `WHERE deletedAt IS NOT NULL` |
| Top-3 tags | Active cross refs only: `removedAt IS NULL`, `GROUP BY tagId ORDER BY COUNT(*) DESC LIMIT 3` |

## Drawer Utility Queries

- **인기 태그 TOP 3**: group `memory_tag_cross_refs` by `tagId`, order by count DESC, join `tags`, limit 3.
- **휴지통**: select `memory_items WHERE deletedAt IS NOT NULL`.
- **태그별 사진**: join `memory_items` with `memory_tag_cross_refs` filtered by selected `tagId`.

## Migration Policy

- Never use destructive migrations outside local debug builds.
- Every schema change must include a Room migration class.
- Update `specs/room-schema.md` with entity and version changes.
- Add migration tests before production releases.
