package com.innowise.sivachenko.kafka.producer;


import com.innowise.sivachenko.kafka.avro.PaymentServiceNotification;
import com.innowise.sivachenko.kafka.producer.api.SendMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
@RequiredArgsConstructor
public class NotificationProducer implements SendMessageProducer<PaymentServiceNotification> {

    @Value("${kafka.topics.notification-topic.name}")
    private String notificationTopicName;

    private final KafkaTemplate<String, PaymentServiceNotification> notificationKafkaTemplate;

    @Override
    public void sendMessage(PaymentServiceNotification message) {
        log.info("Sending notification... message {}", message);
        CompletableFuture<SendResult<String, PaymentServiceNotification>> futureResult = notificationKafkaTemplate.send(notificationTopicName, message);
        futureResult.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Notification sent successfully to topic [{}] with offset [{}]", notificationTopicName, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to sent notification to topic [{}], reason [{}], message [{}]", notificationTopicName, ex.getCause(), ex.getMessage());
            }
        });
    }
}
