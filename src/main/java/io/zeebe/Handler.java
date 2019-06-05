package io.zeebe;

class Handler {
  public String url;
  public String taskType;

  public Handler(String taskType, String url) {
    this.taskType = taskType;
    this.url = url;
  }

  public Handler() {}

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTaskType() {
    return taskType;
  }

  public void setTaskType(String taskType) {
    this.taskType = taskType;
  }
}
