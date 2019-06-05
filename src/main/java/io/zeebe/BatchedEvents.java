package io.zeebe;

class BatchedEvents {
  long lastExportedRecordPosition;
  Batch batch;

  BatchedEvents(long lastExportedRecordPosition, Batch batch) {
    this.lastExportedRecordPosition = lastExportedRecordPosition;
    this.batch = batch;
  }
}
