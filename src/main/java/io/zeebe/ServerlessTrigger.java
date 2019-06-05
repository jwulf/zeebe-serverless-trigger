package io.zeebe;

import io.zeebe.exporter.api.context.Context;
import io.zeebe.exporter.api.context.Controller;
import io.zeebe.exporter.api.record.Record;
import io.zeebe.exporter.api.spi.Exporter;
import io.zeebe.protocol.intent.Intent;
import io.zeebe.protocol.intent.JobIntent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONArray;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class ServerlessTrigger implements Exporter {
  private static final String ENV_PREFIX = "SERVERLESS_TRIGGER_";
  private static final String ENV_URL = ENV_PREFIX + "URL";
  private static final String ENV_BATCH_SIZE = ENV_PREFIX + "BATCH_SIZE";
  private static final String ENV_BATCH_TIME_MILLI = ENV_PREFIX + "BATCH_TIME_MILLI";
  private static final String CONFIG_FILE = "conf/serverless-trigger.yaml";
  private static final long CONFIG_REFRESH_SECONDS = 30L;

  private Logger log;
  private ServerlessTriggerConfiguration configuration;

  private EventQueue eventQueue;
  private Batcher batcher;
  private Sender sender;
  private Controller controller;
  private HandlerMapLoader handlerMapLoader;

  public void configure(final Context context) {
    log = context.getLogger();
    configuration = context.getConfiguration().instantiate(ServerlessTriggerConfiguration.class);
    applyEnvironmentVariables(configuration);

    configureRecordFilter(context);

    handlerMapLoader = new HandlerMapLoader();
    File file = new File(CONFIG_FILE);
    try {
      handlerMapLoader.loadHandlers(file);
    } catch (FileNotFoundException e) {
      //      throw new RuntimeException(e);
    }

    log.debug("Serverless Trigger Exporter configured with {}", configuration);
    log.info(handlerMapLoader.getHandlers().toString());

    if (!testConnectionToLambdaProvider(handlerMapLoader.getHandlerMap())) {
      //      throw new RuntimeException("Lambda configuration not working");
    }
  }

  private void configureRecordFilter(Context context) {
    RecordFilter filter = new RecordFilter();
    context.setFilter(filter);
  }

  private boolean testConnectionToLambdaProvider(HandlerMap handlerMap) {
    boolean working = true;
    for (Handler handler : handlerMap.getHandlers()) {
      try {
        HttpStatelessSender connectionTest = new HttpStatelessSender();
        connectionTest.send(handler.url, new JSONArray());
      } catch (IOException e) {
        log.error(e.toString());
        log.error(
            "Could not contact handler: " + handler.url + " for task type: " + handler.taskType);
        working = false;
      }
    }
    return working;
  }

  private void loadHandlerMap() {
    File file = new File(CONFIG_FILE);
    try {
      handlerMapLoader.loadHandlers(file);
    } catch (FileNotFoundException e) {
      log.warn("Could not find the config file " + file.getAbsolutePath());
    }
  }

  public void open(final Controller controller) {
    eventQueue = new EventQueue();
    batcher = new Batcher(configuration);
    sender = new Sender(controller, log);
    this.controller = controller;
    controller.scheduleTask(Duration.ofMillis(batcher.batchPeriod), this::batchEvents);
    controller.scheduleTask(Duration.ofMillis(sender.sendPeriod), this::sendBatch);
    controller.scheduleTask(Duration.ofSeconds(CONFIG_REFRESH_SECONDS), this::loadHandlerMap);
    log.debug("Event Store exporter started.");
  }

  public void close() {
    log.debug("Closing Event Store Exporter");
  }

  public void export(Record record) {
    Intent intent = record.getMetadata().getIntent();
    if (intent == JobIntent.CREATED
        || intent == JobIntent.TIMED_OUT
        || intent == JobIntent.FAILED) {
      eventQueue.addEvent(record);
    }
  }

  private void batchEvents() {
    batcher.batchFrom(eventQueue);
    controller.scheduleTask(Duration.ofMillis(batcher.batchPeriod), this::batchEvents);
  }

  private void sendBatch() {
    sender.sendFrom(batcher, handlerMapLoader);
    controller.scheduleTask(Duration.ofMillis(sender.sendPeriod), this::sendBatch);
  }

  private void applyEnvironmentVariables(final ServerlessTriggerConfiguration configuration) {
    final Map<String, String> environment = System.getenv();

    Optional.ofNullable(environment.get(ENV_URL)).ifPresent(url -> configuration.url = url);
    Optional.ofNullable(environment.get(ENV_BATCH_SIZE))
        .ifPresent(batchSize -> configuration.batchSize = Integer.parseInt(batchSize));
    Optional.ofNullable(environment.get(ENV_BATCH_TIME_MILLI))
        .ifPresent(
            batchTimeMilli -> configuration.batchTimeMilli = Integer.parseInt(batchTimeMilli));
  }
}
