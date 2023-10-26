package ru.sergdm.ws.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import ru.sergdm.ws.model.Notification;

@Service
public class NotificationSender {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationSender.class);

    @Autowired
    private KafkaTemplate<String, Notification> kafkaTemplate;

    @Value("${app.topic.example}")
    private String topic;

    public void send(Notification data){
        LOG.info("sending data='{}' to topic='{}'", data, topic);

        Message<Notification> message = MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();

        kafkaTemplate.send(message);
    }
}
