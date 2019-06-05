package io.zeebe;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

class Batch {
  private ItemCache itemCache = new ItemCache();

  Batch() {}

  void addItem(JSONObject event) {
    itemCache.addItem(event.getString("type"), event);
  }

  List<BatchItem> getItems() {
    LinkedList<BatchItem> items = new LinkedList<>();
    Set<String> taskTypes = itemCache.getTaskTypes();

    for (String taskType : taskTypes) {
      LinkedList<JSONObject> events = itemCache.getEventsForTaskType(taskType);
      JSONArray jsonArray = new JSONArray();
      while (!events.isEmpty()) {
        jsonArray.put(events.pollFirst());
      }
      items.add(new BatchItem(taskType, jsonArray));
    }
    return items;
  }

  private class ItemCache {
    private HashMap<String, LinkedList<JSONObject>> items = new HashMap<>();

    void addItem(String taskType, JSONObject event) {
      if (!items.containsKey(taskType)) {
        items.put(taskType, new LinkedList<>());
      }
      items.get(taskType).add(event);
    }

    Set<String> getTaskTypes() {
      return items.keySet();
    }

    LinkedList<JSONObject> getEventsForTaskType(String taskType) {
      return items.get(taskType);
    }
  }
}
