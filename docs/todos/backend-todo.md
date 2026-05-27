# Backend/Data TODO

Backend in this project means local data, repositories, Room, file storage, and optional Retrofit APIs.

## Room

- [ ] Define `MemoryItemEntity`.
- [ ] Define `OcrTextEntity`.
- [ ] Define `ClassificationEntity`.
- [ ] Define `VisionLabelEntity`.
- [ ] Define `TagEntity`.
- [ ] Define `MemoryTagCrossRef`.
- [ ] Define `MemoEntity`.
- [ ] Define `YoutubeLinkEntity`.
- [ ] Define `MemorySearchFts`.
- [ ] Add type converters for status enums and timestamps.
- [ ] Create `SnapMindDatabase`.
- [ ] Add DAO interfaces.
- [ ] Add database migration policy.
- [ ] Add DAO tests.

## Repository

- [ ] Create domain repository interfaces.
- [ ] Implement memory repository.
- [ ] Implement OCR result persistence.
- [ ] Implement classification result persistence.
- [ ] Implement optional Vision label persistence.
- [ ] Implement optional YouTube link persistence.
- [ ] Implement tag upsert and linking.
- [ ] Implement memo upsert/delete.
- [ ] Refresh `memory_search_fts` after searchable metadata changes.
- [ ] Map Room entities to domain models.

## File Storage

- [ ] Define app-private image directory.
- [ ] Implement content URI copy.
- [ ] Implement MIME allowlist validation.
- [ ] Implement safe filename generation.
- [ ] Handle missing app-private image files.
- [ ] Handle image delete cleanup.
- [ ] Compute/store optional `contentHash` for duplicate detection.

## Background Work

- [ ] Add WorkManager dependency.
- [ ] Implement `LocalMemoryProcessingWorker`.
- [ ] Implement optional `RemoteEnrichmentWorker`.
- [ ] Implement `AutoTaggingWorker` or equivalent repository continuation.
- [ ] Persist processing status transitions.
- [ ] Add retry use case.
- [ ] Add cancellation on permanent delete.
- [ ] Add cleanup worker for orphan files if needed.

## Retrofit

- [ ] Create Retrofit client module.
- [ ] Create Vision/Gemini/YouTube API services.
- [ ] Gate remote calls behind API settings and configured keys.
- [ ] Create DTO mapping conventions.
- [ ] Add network error mapping.
- [ ] Keep API features optional for initial release.

## Privacy/Security

- [ ] Confirm backup policy.
- [ ] Add privacy-safe logging conventions.
- [ ] Ensure remote uploads are user-enabled, minimized, and redacted from logs.
- [ ] Add cleanup verification for permanent delete.

## Status

Current Status: Not Started

Progress: 0%
