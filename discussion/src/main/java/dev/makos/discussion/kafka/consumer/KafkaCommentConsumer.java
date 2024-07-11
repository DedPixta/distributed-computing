package dev.makos.discussion.kafka.consumer;

import dev.makos.discussion.kafka.producer.KafkaCommentProducer;
import dev.makos.discussion.model.dto.CommentDTO;
import dev.makos.discussion.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaCommentConsumer {

    private final CommentService commentService;
    private final KafkaCommentProducer kafkaCommentProducer;

    @KafkaListener(topics = "InTopic", groupId = "discussion-group")
    @SendTo("OutTopic")
    public void listen(ConsumerRecord<Long, CommentDTO> message) {
        CommentDTO comment = message.value();

        log.info("Received comment: {}", comment);
        HttpMethod method = comment.getMethod();

        if (HttpMethod.POST == method) {
            comment = commentService.saveOne(comment);
        } else if (HttpMethod.GET == method) {
            comment = commentService.getOne(comment.getId());
        } else if (HttpMethod.DELETE == method) {
            commentService.deleteOne(comment.getId());
        }

        kafkaCommentProducer.sendComment(comment, message.headers().lastHeader(KafkaHeaders.CORRELATION_ID).value());
    }
}
