# SnapMind Project Plan

## Project Summary

SnapMind is an Android app that stores screenshots/photos as searchable memory items. It uses on-device OCR, image classification, auto-tagging, memo editing, category/tag filtering, and local-first persistence.

## Problem Statement

Users save screenshots for later reference but often forget:

- why the screenshot was saved
- what text or context was inside it
- which category it belongs to
- how to find it later

SnapMind solves this by extracting text, classifying images, generating tags, and letting users attach notes.

## Product Goals

- Import screenshots/photos from Android share sheet.
- Store image memories locally with reliable file/URI handling.
- Extract OCR text with ML Kit.
- Classify image category with TensorFlow Lite.
- Generate and manage tags.
- Search by OCR text, memo text, tag, category, and date.
- Provide fast navigation with `ViewPager2`, `BottomNavigationView`, and `DrawerLayout`.
- Keep core behavior offline and local-first.

## Initial Release Scope

| Area | Included |
| --- | --- |
| Import | `ACTION_SEND`, app-private image copy, import confirmation |
| Navigation | `ViewPager2`, bottom navigation, drawer quick filters |
| OCR | ML Kit text recognition, normalized text persistence |
| Classification | Bundled TFLite model, category confidence storage |
| Tags | Auto-generated tags, user tags, tag create/rename/delete |
| Search | Room-backed query by OCR/memo/tag/category/date |
| Memo | Edit memo, pin, archive, delete memory |
| Storage | Local Room DB and app-private image files |

## Out of Scope for Initial Release

- User accounts.
- Cloud sync.
- Server-side image processing.
- Required OpenAI/YouTube/Notion integration.
- In-app model training.
- Multi-device backup.

## Core Feature Plan

### 1. Screenshot/Photo Import

Users share an image into SnapMind. The app validates the input, copies the image into app-private storage, creates a `MemoryItem`, and queues processing.

Implementation docs:

- `docs/features/screenshot-sharing.md`
- `docs/specs/permission-storage-spec.md`
- `docs/architecture/background-processing.md`

### 2. Main Navigation

The app shell uses `ViewPager2` for swipe navigation and `BottomNavigationView` for direct access.

Main pages:

| Index | Page | Purpose |
| ---: | --- | --- |
| 0 | Memory | Saved screenshots/photos |
| 1 | Search | Query and structured filters |
| 2 | Tags | Category/tag browsing |
| 3 | Memo | Memo-focused entry point |
| 4 | Settings | App/model/debug settings |

Implementation docs:

- `docs/features/swipe-navigation.md`
- `docs/specs/navigation-flow.md`
- `docs/specs/screen-specs.md`

### 3. Drawer Category/Tag Filtering

The Memory page uses `DrawerLayout` as a fast filtering menu. A left-edge swipe opens categories and popular tags. Selecting `Code`, `Shopping`, `Map`, or a tag filters the main list immediately.

Implementation docs:

- `docs/features/drawer-filtering.md`
- `docs/features/search-filter.md`

### 4. OCR Processing

ML Kit extracts text from imported screenshots/photos. OCR text is normalized, stored, and used for search and tag generation.

Implementation docs:

- `docs/features/ocr-processing.md`
- `docs/architecture/ai-pipeline.md`

### 5. Image Classification

A bundled TFLite model classifies memories into categories such as `chat`, `receipt`, `document`, `product`, `travel`, `food`, `event`, `code`, and `unknown`.

Implementation docs:

- `docs/features/ai-image-classification.md`
- `docs/specs/ml-training-spec.md`

### 6. Auto-Tagging and Tag Management

Tags are generated from OCR text, classification labels, and deterministic rules. Users can also create, rename, delete, assign, and remove tags.

Implementation docs:

- `docs/features/auto-tagging.md`
- `docs/features/drawer-filtering.md`

### 7. Search and Filter

Search supports OCR text, memo body, tag name, category label, and date range. Search results come from Room `Flow`.

Implementation docs:

- `docs/features/search-filter.md`
- `docs/architecture/database-design.md`

### 8. Memo Management

Users can record why a screenshot was saved, edit notes, pin memories, archive items, or permanently delete them.

Implementation docs:

- `docs/features/memo-management.md`

## Architecture Plan

| Layer | Responsibility |
| --- | --- |
| UI | Activity/Fragment or View system screens, rendering state and forwarding events |
| ViewModel | `StateFlow` UI state, event handling, coroutine orchestration |
| Domain | Use cases and domain models |
| Repository | Coordinates Room, file storage, OCR, classifier, tagging, and optional API |
| Local Data | Room entities, DAOs, migrations |
| AI | ML Kit OCR, TFLite classifier, tag rule engine |

Primary architecture docs:

- `docs/architecture/system-overview.md`
- `docs/architecture/android-architecture.md`
- `docs/architecture/database-design.md`
- `docs/architecture/ai-pipeline.md`
- `docs/architecture/background-processing.md`
- `docs/architecture/privacy-security.md`

## Technology Stack

| Area | Technology |
| --- | --- |
| Language | Kotlin |
| Architecture | MVVM, Repository pattern, Use cases |
| Async | Coroutine, Flow |
| UI | Android View system, Fragment, RecyclerView, ViewPager2 |
| Navigation | BottomNavigationView, DrawerLayout, Navigation routes |
| Database | Room |
| File Storage | App-private storage, ContentResolver |
| OCR | ML Kit Text Recognition |
| ML | TensorFlow Lite |
| Network | Retrofit for optional config/metadata APIs |
| DI | Hilt |
| Background Work | Coroutine for foreground work, WorkManager for durable processing |
| Testing | JUnit, Room tests, ViewModel tests, instrumented UI tests |

## Data Lifecycle

```text
Share Intent / Picker URI
  -> permission and MIME validation
  -> app-private image copy
  -> MemoryItem insert
  -> background processing request
  -> OCR extraction
  -> TFLite classification
  -> auto-tagging
  -> Room updates
  -> searchable/filterable UI
```

## Implementation Order

1. Project package structure and dependency setup.
2. Room schema v1 and DAOs.
3. App-private image storage and import flow.
4. Main app shell with `ViewPager2`, bottom navigation, and drawer filter shell.
5. OCR processing pipeline.
6. TFLite classification pipeline.
7. Auto-tagging and tag management.
8. Search/filter queries.
9. Memo edit, archive, pin, delete.
10. Testing, performance, privacy, and release hardening.

## Risk Register

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Large images cause memory pressure | Crash or slow import | Decode with bounds, copy original separately, process downsampled bitmap |
| URI permission expires | Missing image after import | Copy into app-private storage during import |
| OCR/classification takes too long | Poor UX | Use background queue and visible processing status |
| Gesture conflicts between drawer and pager | Navigation feels broken | Reserve left-edge swipe for drawer, content swipe for pager |
| Tag noise | Search/filter quality drops | Cap generated tags and allow user removal |
| Sensitive OCR text exposure | Privacy issue | Keep processing local, avoid remote upload by default |

## Documentation Maintenance

- Feature scope lives in `docs/features/`.
- Implementation status lives in `docs/todos/` and `docs/progress/`.
- Technical contracts live in `docs/specs/`.
- Architecture decisions live in `docs/architecture/`.
- Update this plan only when project direction changes.

