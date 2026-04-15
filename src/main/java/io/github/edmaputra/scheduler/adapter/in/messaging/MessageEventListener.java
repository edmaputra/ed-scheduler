package io.github.edmaputra.scheduler.adapter.in.messaging;

/**
 * Interface for message event listeners that can trigger job scheduling
 * 
 * Implementations of this interface should handle incoming messages from
 * various
 * message providers (e.g., Kafka, RabbitMQ, AWS SQS, etc.) and convert them
 * into job scheduling requests.
 * 
 * Example implementations:
 * - KafkaJobEventListener: Listens to Kafka topics for job scheduling events
 * - RabbitMQJobEventListener: Listens to RabbitMQ queues for job scheduling
 * events
 * - SQSJobEventListener: Polls AWS SQS queues for job scheduling events
 */
public interface MessageEventListener {

    /**
     * Handle incoming job schedule event from message provider
     * 
     * @param event The job schedule event received from the message provider
     */
    void onJobScheduleEvent(JobScheduleEvent event);

    /**
     * Start listening for messages
     * This method should be called to start consuming messages from the provider
     */
    void startListening();

    /**
     * Stop listening for messages
     * This method should be called to gracefully stop consuming messages
     */
    void stopListening();

    /**
     * Check if the listener is currently active
     * 
     * @return true if the listener is actively consuming messages, false otherwise
     */
    boolean isListening();
}
