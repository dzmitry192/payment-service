package com.innowise.sivachenko.kafka.producer.api;

public interface SendMessageProducer<T> {
    void sendMessage(T message);
}