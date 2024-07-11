package dev.makos.discussion.kafka.producer;

import dev.makos.discussion.model.dto.CommentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KafkaCommentProducer {

    private final  KafkaTemplate<Long, CommentDTO> kafkaTemplate;

    @Value("${spring.kafka.reply-topic}")
    private String replyTopic;

    public void sendComment(CommentDTO comment, byte[] correlationId) {
        Message<CommentDTO> message = MessageBuilder.withPayload(comment)
                .setHeader(KafkaHeaders.TOPIC, replyTopic)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();
        kafkaTemplate.send(message);
    }
}
