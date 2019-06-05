package io.zeebe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class HandlerMapLoader {

  private HandlerMap handlermap;
  private HashMap<String, String> handlers;

  HandlerMapLoader() {}

  void loadHandlers(File file) throws FileNotFoundException {
    Yaml yaml = new Yaml(new Constructor(HandlerMap.class));
    InputStream targetStream = new FileInputStream(file);
    handlermap = yaml.load(targetStream);
    handlers = new HashMap<>();
    for (Handler handler : handlermap.handlers) {
      handlers.put(handler.taskType, handler.url);
    }
  }

  HashMap<String, String> getHandlers() {
    return handlers;
  }

  HandlerMap getHandlerMap() {
    return handlermap;
  }

  String getHandler(String taskType) {
    return handlers.getOrDefault(taskType, null);
  }

  boolean hasHandler(String taskType) {
    return handlers.containsKey(taskType);
  }
}
