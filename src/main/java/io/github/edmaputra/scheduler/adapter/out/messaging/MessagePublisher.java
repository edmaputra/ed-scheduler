package io.github.edmaputra.scheduler.adapter.out.messaging;

/**
 * Interface for publishing messages to a message provider when jobs execute
 * 
 * Implementations can use various message providers:
 * - Kafka: KafkaTemplate
 * - RabbitMQ: RabbitTemplate
 * - AWS SQS: SqsTemplate
 * - Google Pub/Sub: PubSubTemplate
 * - Azure Service Bus: ServiceBusTemplate
 */
public interface MessagePublisher {

    /**
     * Publish a message to a specific topic/queue
     * 
     * @param topic   The topic/queue name to publish to
     * @param payload The message payload (typically JSON string)
     */
    void publish(String topic, String payload);

    /**
     * Publish a message with headers/metadata
     * 
     * @param topic   The topic/queue name to publish to
     * @param payload The message payload
     * @param headers Additional headers/metadata for the message
     */
    void publish(String topic, String payload, java.util.Map<String, String> headers);
}
