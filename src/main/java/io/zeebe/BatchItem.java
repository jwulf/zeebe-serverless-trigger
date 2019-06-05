package io.zeebe;

import org.json.JSONArray;

public class BatchItem {
  String taskType;
  JSONArray events;

  public BatchItem(String taskType, JSONArray events) {
    this.taskType = taskType;
    this.events = events;
  }
}
