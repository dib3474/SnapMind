# Room Schema

## Database

- Name: `snapmind.db`
- Version: `1`
- Export schema: `true`

## Shared Enum Values

Processing status values are stored as strings for readable migrations and debugging.

| Status Group | Values |
| --- | --- |
| Standard processing | `PENDING`, `RUNNING`, `SUCCESS`, `FAILED` |
| Optional remote processing | `PENDING`, `RUNNING`, `SUCCESS`, `FAILED`, `SKIPPED` |
| Gemini memo | `PENDING`, `RUNNING`, `SUGGESTED`, `ACCEPTED`, `DISMISSED`, `FAILED`, `SKIPPED` |
| Tag assignment source | `OCR`, `TFLITE`, `VISION`, `USER`, `SYSTEM` |

## `memory_items`

| Column | Type | Nullable | Notes |
| --- | --- | --- | --- |
| `id` | Long | No | Primary key, auto-generated |
| `imageUri` | String | No | App-private URI/path |
| `sourceUri` | String | Yes | Original shared URI for debug/source metadata |
| `mimeType` | String | Yes | Validated imported MIME type |
| `contentHash` | String | Yes | SHA-256 hash for duplicate detection when available |
| `createdAt` | Long | No | Epoch millis |
| `updatedAt` | Long | No | Epoch millis |
| `ocrStatus` | String | No | Standard processing status |
| `classificationStatus` | String | No | Standard processing status |
| `visionLabelStatus` | String | No | Optional remote processing status |
| `geminiMemoStatus` | String | No | Gemini memo status |
| `youtubeLinkStatus` | String | No | Optional remote processing status |
| `taggingStatus` | String | No | Standard processing status |
| `isFavorite` | Boolean | No | Default false |
| `deletedAt` | Long | Yes | Null when active; non-null means trash |

Default statuses for a newly imported item:

- `ocrStatus = PENDING`
- `classificationStatus = PENDING`
- `visionLabelStatus = SKIPPED` until remote Vision enrichment is enabled
- `geminiMemoStatus = SKIPPED` until Gemini recommendation is enabled
- `youtubeLinkStatus = SKIPPED` until category is `youtube` and YouTube search is enabled
- `taggingStatus = PENDING`

## `ocr_texts`

| Column | Type | Nullable | Notes |
| --- | --- | --- | --- |
| `memoryId` | Long | No | Primary key, foreign key |
| `fullText` | String | No | Normalized OCR text |
| `rawText` | String | Yes | Optional raw recognizer text |
| `createdAt` | Long | No | Epoch millis |

## `classifications`

| Column | Type | Nullable | Notes |
| --- | --- | --- | --- |
| `id` | Long | No | Primary key, auto-generated |
| `memoryId` | Long | No | Foreign key |
| `label` | String | No | Model label |
| `confidence` | Float | No | 0.0 to 1.0 |
| `modelVersion` | String | No | Model asset version |
| `rank` | Int | No | 1 for top prediction |
| `createdAt` | Long | No | Epoch millis |

## `vision_labels`

| Column | Type | Nullable | Notes |
| --- | --- | --- | --- |
| `id` | Long | No | Primary key, auto-generated |
| `memoryId` | Long | No | Foreign key |
| `label` | String | No | Vision label description |
| `score` | Float | No | 0.0 to 1.0 |
| `createdAt` | Long | No | Epoch millis |

## `tags`

| Column | Type | Nullable | Notes |
| --- | --- | --- | --- |
| `id` | Long | No | Primary key, auto-generated |
| `name` | String | No | Unique normalized lowercase tag key |
| `displayName` | String | No | UI label |
| `isUserManaged` | Boolean | No | True for user-created/editable tags |
| `createdAt` | Long | No | Epoch millis |
| `updatedAt` | Long | No | Epoch millis |
| `isArchived` | Boolean | No | Hide from default drawer lists when true |

## `memory_tag_cross_refs`

| Column | Type | Nullable | Notes |
| --- | --- | --- | --- |
| `memoryId` | Long | No | Foreign key, composite primary key |
| `tagId` | Long | No | Foreign key, composite primary key |
| `assignedBy` | String | No | `AUTO` or `USER` |
| `sourceTypes` | String | No | Comma-separated source enum set, e.g. `OCR,TFLITE` |
| `createdAt` | Long | No | Epoch millis |
| `removedAt` | Long | Yes | Null when active; preserves user removal history |

## `memos`

| Column | Type | Nullable | Notes |
| --- | --- | --- | --- |
| `memoryId` | Long | No | Primary key, foreign key |
| `body` | String | No | User memo text |
| `geminiSuggestion` | String | Yes | Last Gemini suggestion, if available |
| `createdAt` | Long | No | Epoch millis |
| `updatedAt` | Long | No | Epoch millis |

## `youtube_links`

| Column | Type | Nullable | Notes |
| --- | --- | --- | --- |
| `memoryId` | Long | No | Primary key, foreign key |
| `videoId` | String | No | YouTube video ID |
| `title` | String | Yes | API result title |
| `url` | String | No | Watch URL used for `Intent.ACTION_VIEW` |
| `createdAt` | Long | No | Epoch millis |

## `memory_search_fts`

Denormalized Room FTS table used for text search. Refresh this row transactionally after OCR, memo, tag, classification, or YouTube metadata changes.

| Column | Type | Nullable | Notes |
| --- | --- | --- | --- |
| `memoryId` | Long | No | Unindexed reference to `memory_items.id` |
| `ocrText` | String | No | Normalized OCR text |
| `memoBody` | String | No | User memo text |
| `tagText` | String | No | Active tag display names joined by spaces |
| `categoryText` | String | No | Classification labels joined by spaces |
| `youtubeTitle` | String | No | YouTube result title when available |

## Indexes

- `memory_items(imageUri)` unique.
- `memory_items(contentHash)` unique, nullable.
- `memory_items(createdAt)`.
- `memory_items(deletedAt)`.
- `memory_items(isFavorite)`.
- `ocr_texts(memoryId)` unique.
- `tags(name)` unique.
- `tags(isUserManaged)`.
- `tags(isArchived)`.
- `memory_tag_cross_refs(memoryId, tagId)` primary composite.
- `memory_tag_cross_refs(tagId, removedAt)`.
- `classifications(memoryId, label)`.
- `classifications(label)`.
- `vision_labels(memoryId)`.
- `vision_labels(label, score)`.
- `memos(memoryId)` unique.
- `youtube_links(memoryId)` unique.

## Drawer Query Requirements

### Category Counts

Return visible active memory count grouped by top classification label.

```sql
SELECT classifications.label, COUNT(DISTINCT classifications.memoryId) AS count
FROM classifications
JOIN memory_items ON memory_items.id = classifications.memoryId
WHERE classifications.rank = 1
  AND memory_items.deletedAt IS NULL
GROUP BY classifications.label
ORDER BY count DESC, classifications.label ASC
```

### Popular Tag Counts

Return most-used active tags for the drawer.

```sql
SELECT tags.id, tags.name, tags.displayName, COUNT(memory_tag_cross_refs.memoryId) AS count
FROM tags
JOIN memory_tag_cross_refs ON tags.id = memory_tag_cross_refs.tagId
JOIN memory_items ON memory_items.id = memory_tag_cross_refs.memoryId
WHERE tags.isArchived = 0
  AND memory_tag_cross_refs.removedAt IS NULL
  AND memory_items.deletedAt IS NULL
GROUP BY tags.id
ORDER BY count DESC, tags.displayName ASC
LIMIT :limit
```
