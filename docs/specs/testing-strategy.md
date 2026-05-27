# Testing Strategy

## Goal

Define the minimum test coverage required for SnapMind features before they are considered implementation complete.

## Test Layers

| Layer | Purpose | Tools |
| --- | --- | --- |
| Unit tests | Pure Kotlin logic, mappers, validators, tag rules | JUnit |
| ViewModel tests | UI state and event behavior | JUnit, coroutine test |
| DAO tests | Room queries and relationships | AndroidX Room test, in-memory DB |
| Repository tests | Data orchestration and error mapping | Fake DAOs/processors |
| Worker tests | Background processing status transitions | WorkManager test utilities |
| Instrumented UI tests | Import, navigation, drawer, search, memo flows | AndroidX Test, Espresso |
| Manual tests | Device-specific permissions, share sheet, performance | Physical Android device |

## Feature Completion Test Requirements

| Feature | Required Tests |
| --- | --- |
| Screenshot Sharing | MIME validation, URI copy success/failure, duplicate handling, share intent UI smoke |
| Swipe Navigation | Pager swipe selects bottom nav, bottom nav tap changes page, state restore |
| Drawer Filtering | Drawer opens from edge/button, category filter applies, tag filter applies, active filter clears |
| OCR Processing | OCR success mapping, empty result, failure status, retry |
| AI Classification | Preprocessing dimensions, label mapping, confidence threshold, model failure |
| Auto-Tagging | keyword rules, duplicate tags, user tag preservation, removed generated tag behavior |
| Search and Filter | query by OCR/memo/tag/category/date, combined filters, empty result |
| Memo Management | save, clear, favorite, trash, restore, permanent delete transaction |

## Test Data Policy

- Use sanitized screenshots only.
- Do not commit personal screenshots.
- Do not commit OCR text containing real credentials, phone numbers, IDs, or private messages.
- Store small sample assets under a clearly named test asset directory.
- Keep large model/test data outside the app source unless required for tests.

## Coroutine Testing Rules

- Use test dispatchers for ViewModel/use case tests.
- Do not use real `Dispatchers.IO` directly in tests.
- Inject `DispatcherProvider`.
- Advance virtual time for debounce and retry tests.

## Room Testing Rules

- Use in-memory database for DAO tests.
- Test migrations before release builds.
- Verify cascade/delete behavior explicitly.
- Verify drawer count queries for category and popular tag lists.
- Verify search query ordering.

## Worker Testing Rules

- Test status transitions:
  - `PENDING` to `RUNNING`
  - `RUNNING` to `SUCCESS`
  - `RUNNING` to `FAILED`
- Test missing memory behavior.
- Test partial OCR/classification failure.
- Test retry scheduling.
- Test cancellation when memory is deleted.

## UI Testing Rules

Prioritize stable user flows:

- import preview save/cancel
- bottom navigation tap
- pager swipe
- drawer category filter
- drawer tag filter
- memory detail open/back
- memo save/delete

Avoid assertions tied to fragile animation timing. Wait for observable UI state.

## Manual QA Checklist

- [ ] Share a single screenshot from another app.
- [ ] Share multiple images.
- [ ] Deny/cancel import.
- [ ] Open drawer with toolbar button and left-edge swipe.
- [ ] Select `Code` category and verify filtered list.
- [ ] Create and assign a user tag.
- [ ] Search by OCR text.
- [ ] Edit memo and rotate device.
- [ ] Delete memory and verify file cleanup.
- [ ] Run on Android 13+ device or emulator.

## Definition of Done

A feature is done when:

- [ ] Unit tests cover core logic.
- [ ] DAO/repository tests cover persistence if the feature touches Room.
- [ ] ViewModel tests cover loading, success, and error states.
- [ ] UI test exists for critical user flow or manual test reason is documented.
- [ ] Bug tracker has no open S0/S1 bugs for the feature.
- [ ] Related feature spec TODO checklist is updated.
