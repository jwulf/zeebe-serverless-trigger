package io.zeebe;

import java.util.List;

class HandlerMap {
  public List<Handler> handlers;

  public HandlerMap() {}

  public void setHandlers(final List<Handler> handlers) {
    this.handlers = handlers;
  }

  public List<Handler> getHandlers() {
    return this.handlers;
  }
}
