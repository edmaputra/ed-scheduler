package io.github.edmaputra.scheduler.application.port.out;

import java.util.Map;

public interface MessageDispatchPort {

    void publish(String topic, String payload, Map<String, String> headers);
}
