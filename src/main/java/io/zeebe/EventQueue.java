package io.zeebe;

import io.zeebe.exporter.api.record.Record;
import org.json.JSONObject;
import java.util.LinkedList;

class EventQueue {
  LinkedList<JSONObject> events = new LinkedList<>();

  void addEvent(Record record) {
    final JSONObject json = new JSONObject(record.getValue().toJson());
    final JSONObject metadata = new JSONObject(record.getMetadata().toJson());
    json.put("position", record.getPosition());
    json.put("metadata", metadata);
    events.add(json);
  }
}
