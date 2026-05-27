# Permission and Storage Specification

## Goal

Define how SnapMind receives, validates, stores, reads, and deletes screenshot/photo files while respecting Android permission and scoped storage rules.

## Scope

This spec covers:

- share sheet image import
- optional in-app photo picker import
- `ContentResolver` URI handling
- app-private image storage
- MIME/type validation
- file cleanup on delete
- permission denial and revoked URI handling

## Storage Policy

SnapMind should copy imported images into app-private storage during import.

Preferred location:

```text
context.filesDir/snapmind/images/
```

Rationale:

- imported memories remain readable after the source app revokes URI access
- no broad media permission is required for copied app-private files
- delete cleanup is controlled by the app
- offline search/detail views remain stable

## Import Sources

| Source | Support | Notes |
| --- | --- | --- |
| Android share sheet `ACTION_SEND` | Required | Single image import |
| Android share sheet `ACTION_SEND_MULTIPLE` | Required after single import | Batch image import |
| Android Photo Picker | Recommended | In-app import without broad storage permission |
| Legacy gallery picker | Optional | Use only if Photo Picker unavailable |
| Direct camera capture | Out of scope | Not required for initial release |

## MIME Validation

Accepted MIME types:

- `image/jpeg`
- `image/png`
- `image/webp`
- `image/heic`
- `image/heif`

Validation rules:

- Reject non-image MIME types.
- If MIME type is missing, inspect `ContentResolver.getType(uri)` and file extension as fallback.
- Do not trust file extension alone.
- Store original MIME type when available.

## URI Handling

### Share Intent

Required handling:

- Read `Intent.EXTRA_STREAM`.
- Read `ClipData` for multi-share.
- Validate every URI before inserting DB rows.
- Open input stream through `ContentResolver`.
- Copy bytes into app-private storage.
- Close streams with `use`.

### Persistable Permissions

Persistable URI permissions are optional. The app should not rely on them for core memory access.

Use persistable permissions only when:

- URI came from a picker that grants persistable access.
- The app has a specific reason to avoid copying.

Initial release should still copy into app-private storage.

## File Naming

Use generated filenames, not source filenames.

Format:

```text
memory_{createdAt}_{randomSuffix}.{extension}
```

Rules:

- Use safe lowercase extension from MIME type.
- Avoid user-provided path segments.
- Store original display name only as metadata if needed.
- Never expose internal file paths in UI.

## Database Fields

`MemoryItemEntity` should store:

- `imageUri`: app-private file URI/path used by app
- `sourceUri`: original shared URI when useful for debugging/source metadata
- `mimeType`: detected MIME type
- `createdAt`
- `updatedAt`

## Delete Policy

Permanent delete must attempt to remove:

- app-private image file
- `MemoryItemEntity`
- `OcrTextEntity`
- `ClassificationEntity`
- `MemoEntity`
- `MemoryTagCrossRef`

Deletion should be transactional for database rows. File deletion failure should be logged and surfaced as a cleanup warning, but DB consistency should remain clear.

## Permission Requirements

| Android Version | Import Method | Runtime Permission |
| --- | --- | --- |
| Android 13+ | Photo Picker | None for selected media |
| Android 13+ | Share intent | None for provided URI access |
| Android 12 and below | Share intent | None for provided URI access |
| Any | App-private copied file | None |

Do not request broad storage permissions for initial release.

## Error Handling

| Case | Handling |
| --- | --- |
| URI cannot be opened | Show import failed state and do not create memory row |
| MIME unsupported | Show unsupported file message |
| Copy fails due to storage | Show retry/cancel and log storage error |
| Batch partially fails | Import valid images and show failed count |
| File missing after import | Show missing file error and keep metadata visible if possible |
| Delete file fails | Delete DB rows only if product decision allows; log cleanup issue |

## Security Rules

- Never execute or parse non-image files as images.
- Never store external URI credentials or tokens.
- Never log full OCR text or absolute file paths in release builds.
- Do not upload image bytes except through user-enabled Vision/Gemini remote enrichment; always upload a bounded, re-encoded copy rather than the original app-private file.

## TODO Checklist

- [ ] Implement MIME allowlist.
- [ ] Implement app-private image copy helper.
- [ ] Add file extension mapping from MIME type.
- [ ] Add batch import validation.
- [ ] Add missing file error state.
- [ ] Add delete cleanup behavior.
- [ ] Add tests for unsupported MIME type.
- [ ] Add tests for copy failure.
- [ ] Add tests for file deletion path.
