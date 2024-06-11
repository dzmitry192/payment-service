package com.innowise.sivachenko.kafka.producer;

import com.innowise.sivachenko.kafka.avro.AuditActionRequest;
import com.innowise.sivachenko.kafka.producer.api.SendMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
@RequiredArgsConstructor
public class AuditActionProducer implements SendMessageProducer<AuditActionRequest> {

    @Value("${kafka.topics.audit-topic.name}")
    private String auditTopicName;

    private final KafkaTemplate<String, AuditActionRequest> kafkaTemplate;

    @Override
    public void sendMessage(AuditActionRequest actionRequest) {
        log.info("Sending message at {}, message {}", LocalDateTime.now(), actionRequest);
        CompletableFuture<SendResult<String, AuditActionRequest>> futureResult = kafkaTemplate.send(auditTopicName, actionRequest);
        futureResult.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Audit message sent successfully to topic [{}] with offset [{}]", auditTopicName, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to sent audit message to topic [{}], reason [{}], message [{}]", auditTopicName, ex.getCause(), ex.getMessage());
            }
        });
    }
}
