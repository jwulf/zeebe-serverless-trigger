package io.zeebe;

import org.json.JSONObject;
import java.util.LinkedList;

class Batcher {
  final LinkedList<BatchedEvents> queue = new LinkedList<>();
  private final int batchSize;
  final int batchPeriod;

  Batcher(ServerlessTriggerConfiguration configuration) {
    batchSize = configuration.batchSize;
    batchPeriod = configuration.batchTimeMilli;
  }

  void batchFrom(EventQueue eventQueue) {
    if (eventQueue.events.isEmpty()) {
      return;
    }
    Batch batch = new Batch();
    long positionOfLastRecordInBatch = -1L;
    final int countEventsToBatchNow = Math.max(batchSize, eventQueue.events.size());

    for (int i = 0; i < countEventsToBatchNow; i++) {
      final JSONObject event = eventQueue.events.pollFirst();
      if (event != null) {
        batch.addItem(event);
        System.out.println(event);
        positionOfLastRecordInBatch = (long) event.get("position");
      }
    }
    BatchedEvents batchedEvents = new BatchedEvents(positionOfLastRecordInBatch, batch);
    queue.add(batchedEvents);
  }
}
