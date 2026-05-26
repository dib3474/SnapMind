# Privacy and Security

## Goal

Define privacy and security rules for handling screenshots/photos, OCR text, memo content, tags, and optional remote API interactions.

SnapMind handles sensitive user-created visual data. The default policy is local-first and privacy-preserving.

## Core Principles

- Process OCR and image classification on device by default.
- Do not upload image bytes in the initial release.
- Do not upload OCR text, memo text, or tags without explicit future user consent.
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
| Model config | Low | Preferences/Room metadata |

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

- No image upload.
- No OCR text upload.
- No memo upload.
- Retrofit may only fetch optional model/config metadata or send non-sensitive aggregate metadata if explicitly enabled.

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
| Accidental remote upload | Keep API layer optional and deny image/text upload by default |
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
- [ ] Add no-upload rule to API review checklist.
- [ ] Add privacy review before any remote metadata feature.
- [ ] Add orphan file cleanup task if permanent delete can fail.

