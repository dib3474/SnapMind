# Privacy and Security

## Goal

Define privacy and security rules for handling screenshots/photos, OCR text, memo content, tags, and optional remote API interactions.

SnapMind handles sensitive user-created visual data. The default policy is local-first and privacy-preserving.

## Core Principles

- Process OCR and image classification on device by default.
- Keep the app usable without any remote API.
- Upload downsampled image bytes only for user-enabled Vision/Gemini remote enrichment.
- Upload OCR-derived YouTube search text only when the YouTube deep-link feature is enabled.
- Do not upload memo text or user-created tags in the initial release.
- Store imported images in app-private storage.
- Keep Room data local to the device.
- Avoid logging sensitive content.

## Sensitive Data Types

| Data | Sensitivity | Storage |
| --- | --- | --- |
| Screenshot/photo file | High | App-private file storage |
| OCR full text | High | Room |
| Memo body | High | Room |
| Tags | Medium | Room |
| Classification label | Low/Medium | Room |
| Source URI | Medium | Room/debug metadata |
| API keys / remote settings | High | BuildConfig for demo keys or private preferences for runtime keys |

## Logging Rules

Release builds must not log:

- OCR full text
- memo body
- full image file path
- source URI
- user-created tag names if they may contain private data
- API response bodies containing user metadata

Debug builds may log IDs, counts, statuses, and non-sensitive error codes.

## Network Rules

Initial release:

- Local OCR and TFLite classification never require network.
- Vision API may upload a bounded, downsampled, re-encoded image only after remote enrichment is enabled.
- Gemini API may upload a bounded, downsampled, re-encoded image only after memo recommendation is enabled.
- YouTube Data API may upload the selected OCR-derived candidate title only after YouTube deep-linking is enabled.
- Do not upload memo body, user-created tags, source URI, or app-private file paths.
- Do not include Vision `TEXT_DETECTION`; ML Kit handles OCR locally.

Future remote features must add:

- user-facing opt-in
- privacy policy update
- data minimization review
- delete/export behavior

## Local Storage Rules

- Store image files under app-private directory.
- Store metadata in Room.
- Do not write OCR text to external/shared storage.
- Do not expose internal file paths through intents.
- Clean files on permanent delete.

## Deletion Rules

When user permanently deletes a memory:

- delete app-private image file
- delete OCR text
- delete classification rows
- delete memo
- delete tag cross references
- keep shared tag definitions unless no longer needed or explicitly deleted

Deletion failures must be visible enough for debugging without exposing sensitive data.

## Backup Policy

Initial release recommendation:

- Disable automatic backup for image/OCR data until a deliberate backup strategy exists.

Reason:

- Screenshots and OCR text may include credentials, private conversations, identity documents, or financial data.

If backup is enabled later:

- document included/excluded files
- encrypt sensitive data where appropriate
- provide user setting if feasible

## Threats and Mitigations

| Threat | Mitigation |
| --- | --- |
| Source URI permission revoked | Copy image into app-private storage |
| Sensitive OCR text leaked through logs | Redact logs and disable sensitive logging in release |
| Accidental remote upload | Keep API layer optional, gate it behind settings, and test disabled-path defaults |
| File path exposed to another app | Use FileProvider only for explicit share/export features |
| Deleted memory leaves file behind | Add cleanup path and periodic orphan file check |
| Untrusted shared file content | MIME allowlist and safe decode handling |

## Access Control

Initial release does not include app lock or authentication.

Future options:

- biometric app lock
- hidden/private memories
- encrypted Room database
- encrypted image files

These are out of scope until baseline memory management is complete.

## TODO Checklist

- [ ] Confirm backup policy in manifest.
- [ ] Add release logging rules.
- [ ] Add sensitive data redaction helper.
- [ ] Add file cleanup verification.
- [ ] Add remote-enrichment opt-in and payload minimization checks to API review checklist.
- [ ] Add privacy review before any remote metadata feature.
- [ ] Add orphan file cleanup task if permanent delete can fail.
