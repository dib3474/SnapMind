# AI TODO

AI means OCR, TFLite classification, preprocessing, model assets, and tag rule logic.

## OCR

- [ ] Add ML Kit text recognition dependency.
- [ ] Create OCR processor interface.
- [ ] Implement ML Kit OCR processor.
- [ ] Normalize OCR text output.
- [ ] Handle empty OCR result.
- [ ] Add retry-safe OCR status updates.
- [ ] Integrate OCR processor with background worker.

## Classification

- [ ] Choose initial TFLite model.
- [ ] Add model to assets.
- [ ] Add label file to assets.
- [ ] Implement bitmap preprocessing.
- [ ] Implement TFLite interpreter wrapper.
- [ ] Map output probabilities to labels.
- [ ] Define confidence threshold.
- [ ] Persist top-N predictions.
- [ ] Integrate classifier with background worker.

## Auto-Tagging

- [ ] Define tag taxonomy.
- [ ] Define keyword rule format.
- [ ] Implement OCR keyword extraction.
- [ ] Implement category-to-tag mapping.
- [ ] Add max generated tag count.
- [ ] Add tests for tag rules.
- [ ] Integrate auto-tagging with post-processing queue.

## Quality

- [ ] Add sample images for OCR/classification testing.
- [ ] Measure inference time on at least one physical device.
- [ ] Document model version and label ordering.
- [ ] Verify privacy-safe logging for AI/OCR failures.

## Status

Current Status: Not Started

Progress: 0%
