package io.zeebe;

import io.zeebe.exporter.api.context.Controller;
import org.slf4j.Logger;

import java.io.IOException;

class Sender {
  private Controller controller;
  private Logger log;
  private HttpStatelessSender http;
  int sendPeriod;

  Sender(Controller controller, Logger log) {
    this.controller = controller;
    this.log = log;
    http = new HttpStatelessSender();
  }

  void sendFrom(Batcher batcher, HandlerMapLoader handlerMap) {
    if (batcher.queue.isEmpty()) {
      return;
    }
    final BatchedEvents batchedEvents = batcher.queue.getFirst();
    final Batch eventBatch = batchedEvents.batch;
    final long positionOfLastEventInBatch = batchedEvents.lastExportedRecordPosition;
    for (BatchItem entry : eventBatch.getItems()) {
      String taskType = entry.taskType;
      String url = getUrl(taskType, handlerMap);
      try {
        if (url != null) {
          http.send(url, entry.events);
        } else {
          // @TODO push to backlog queue - and poll to see if we have new handlers for it
          log.warn("No handler defined for " + taskType);
        }
      } catch (IOException e) {
        log.error(e.getMessage());
      }
    }
    batcher.queue.pollFirst();
    controller.updateLastExportedRecordPosition(positionOfLastEventInBatch);
  }

  private String getUrl(String taskType, HandlerMapLoader handlerMap) {
    if (handlerMap.hasHandler("*")) {
      return handlerMap.getHandler("*");
    }
    if (handlerMap.hasHandler(taskType)) {
      return handlerMap.getHandler(taskType);
    }
    return null;
  }
}
