# Background Processing

## Goal

Define how SnapMind schedules OCR, TensorFlow Lite classification, optional remote enrichment, and auto-tagging without blocking UI or losing work when the app process is interrupted.

## Processing Principles

- Import persistence must complete before AI processing starts.
- Room is the source of truth for processing status.
- Every processing step must be retryable.
- UI must remain usable while processing runs.
- Durable background work should use WorkManager.

## Work Types

| Work | Trigger | Execution | Durability |
| --- | --- | --- | --- |
| Image copy | User confirms import | Coroutine on IO dispatcher | Must complete before DB insert |
| OCR | Memory inserted or retry requested | WorkManager or foreground coroutine for immediate small work | Durable |
| Classification | Memory inserted or retry requested | WorkManager or foreground coroutine for immediate small work | Durable |
| Local AI processing | Memory inserted or retry requested | WorkManager | Durable |
| Remote enrichment | Local OCR/classification success and remote feature enabled | WorkManager with network constraint | Durable, optional |
| Auto-tagging | Local processing success, remote enrichment success/failure/skip, or retry requested | WorkManager or repository coroutine | Durable preferred |
| Cleanup | Memory delete | Repository transaction plus cleanup worker if needed | Durable |

## Recommended Initial Design

Use a small processing coordinator with separate local and remote work:

```text
ImportMemoryUseCase
  -> insert MemoryItem with PENDING statuses
  -> enqueue LocalMemoryProcessingWork
  -> LocalMemoryProcessingWork enqueues RemoteEnrichmentWork if enabled/needed
  -> enqueue/continue AutoTaggingWork after available outputs
```

`LocalMemoryProcessingWork` should:

1. Mark OCR/classification statuses as `RUNNING`.
2. Run OCR and persist result.
3. Run classification and persist result.
4. Mark local final statuses as `SUCCESS` or `FAILED`.

`RemoteEnrichmentWork` should:

1. Check user-enabled API settings and configured keys.
2. Mark disabled remote steps as `SKIPPED`.
3. Run Vision/Gemini/YouTube steps only when enabled and applicable.
4. Persist partial remote results and statuses independently.

`AutoTaggingWork` should:

1. Read all available OCR, classification, and Vision outputs.
2. Generate deterministic tags from available sources.
3. Mark `taggingStatus` as `SUCCESS` or `FAILED`.

If OCR fails but classification succeeds, classification and tagging should still persist partial results.

## Status Model

Use status values:

- `PENDING`
- `RUNNING`
- `SUCCESS`
- `FAILED`
- `SKIPPED` for optional remote steps that are disabled or not applicable

Store separate statuses:

- `ocrStatus`
- `classificationStatus`
- `visionLabelStatus`
- `geminiMemoStatus`
- `youtubeLinkStatus`
- `taggingStatus`

## WorkManager Requirements

Use WorkManager when:

- processing may continue after app backgrounding
- batch import creates multiple processing jobs
- retry needs durable scheduling
- cleanup needs eventual completion

Worker input:

- `memoryId`

Worker output:

- success/failure status through Room, not only WorkManager result

Constraints:

- Network is not required for local OCR/classification/tagging.
- Network is required for remote Vision/Gemini/YouTube work.
- Battery-not-low constraint is optional for large batch processing.
- Expedited work may be used only for a small local import if UX requires quick processing.

## Coroutine Dispatcher Rules

| Task | Dispatcher |
| --- | --- |
| File copy | IO |
| Room writes | IO |
| Bitmap decode | Default or dedicated image dispatcher |
| ML Kit OCR await | Default/IO wrapper depending implementation |
| TFLite inference | Default or dedicated CPU dispatcher |
| Tag rule evaluation | Default |

Inject dispatchers through `DispatcherProvider`.

## Retry Policy

| Failure | Retry |
| --- | --- |
| Temporary file read error | Retry with backoff |
| ML Kit transient failure | Retry with backoff |
| TFLite model load failure | Do not auto-retry until app restart/model reload |
| Remote API offline/quota/auth failure | Mark remote step `FAILED` or `SKIPPED` according to configuration; keep local item usable |
| Database write failure | Retry once, then mark failed |
| Missing file | Do not retry automatically |

Manual retry should be available from memory detail.

## Batch Import Behavior

- Create one memory row per valid image.
- Enqueue one worker per memory or a bounded batch worker.
- Limit concurrent CPU-heavy work.
- Show aggregate batch progress when possible.
- Allow partial success.

## Cancellation

When memory is deleted:

- cancel pending processing work for that memory
- ignore worker result if memory no longer exists
- clean temporary files

## UI State Integration

ViewModels observe Room `Flow`.

UI displays:

- processing badge for `RUNNING`
- retry action for `FAILED`
- "remote enrichment skipped" only in detail/settings, not as a blocking error
- partial metadata when only one processor succeeded
- non-blocking progress for batch import

## Error Handling

| Case | Handling |
| --- | --- |
| Worker starts but memory missing | Return success and do no work |
| OCR failed | Persist failure status and continue classification |
| Classification failed | Persist failure status and continue OCR/tagging where possible |
| Remote enrichment disabled | Persist `SKIPPED` and continue local flow |
| Remote enrichment failed | Persist failure status and continue local flow |
| Tagging failed | Keep OCR/classification results and show retry |
| App killed during processing | WorkManager resumes or status remains retryable |

## TODO Checklist

- [ ] Add WorkManager dependency.
- [ ] Define `LocalMemoryProcessingWorker`.
- [ ] Define optional `RemoteEnrichmentWorker`.
- [ ] Define `AutoTaggingWorker` or repository-level tagging continuation.
- [ ] Define processing coordinator/use case.
- [ ] Add worker factory with DI.
- [ ] Persist per-step status transitions.
- [ ] Add manual retry use case.
- [ ] Add cancellation on delete.
- [ ] Add batch import concurrency policy.
- [ ] Add worker unit tests.
- [ ] Add integration test for partial OCR/classification failure.
