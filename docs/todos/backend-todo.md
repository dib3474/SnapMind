# Backend/Data TODO

Backend in this project means local data, repositories, Room, file storage, and optional Retrofit APIs.

## Room

- [ ] Define `MemoryItemEntity`.
- [ ] Define `OcrTextEntity`.
- [ ] Define `ClassificationEntity`.
- [ ] Define `TagEntity`.
- [ ] Define `MemoryTagCrossRef`.
- [ ] Define `MemoEntity`.
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
- [ ] Implement tag upsert and linking.
- [ ] Implement memo upsert/delete.
- [ ] Map Room entities to domain models.

## File Storage

- [ ] Define app-private image directory.
- [ ] Implement content URI copy.
- [ ] Implement MIME allowlist validation.
- [ ] Implement safe filename generation.
- [ ] Handle missing app-private image files.
- [ ] Handle image delete cleanup.
- [ ] Add duplicate detection placeholder.

## Background Work

- [ ] Add WorkManager dependency.
- [ ] Implement `MemoryProcessingWorker`.
- [ ] Persist processing status transitions.
- [ ] Add retry use case.
- [ ] Add cancellation on permanent delete.
- [ ] Add cleanup worker for orphan files if needed.

## Retrofit

- [ ] Create Retrofit client module.
- [ ] Create `ModelConfigApi`.
- [ ] Create DTO mapping conventions.
- [ ] Add network error mapping.
- [ ] Keep API features optional for initial release.

## Privacy/Security

- [ ] Confirm backup policy.
- [ ] Add privacy-safe logging conventions.
- [ ] Ensure no image/OCR/memo upload in initial release.
- [ ] Add cleanup verification for permanent delete.

## Status

Current Status: Not Started

Progress: 0%
