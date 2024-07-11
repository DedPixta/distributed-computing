package dev.makos.publisher.kafka.producer;

import dev.makos.publisher.exception.CustomException;
import dev.makos.publisher.model.dto.CommentCassandraDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyMessageFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaCommentProducer {

    private final KafkaTemplate<Long, CommentCassandraDTO> kafkaTemplate;
    private final ReplyingKafkaTemplate<Long, CommentCassandraDTO, CommentCassandraDTO> replyingKafkaTemplate;

    @Value("${spring.kafka.request-topic}")
    private String requestTopic;
    @Value("${spring.kafka.reply-topic}")
    private String replyTopic;

    public CommentCassandraDTO sendAndReceive(CommentCassandraDTO comment)  {
        Long correlationId = comment.getId();

        Message<CommentCassandraDTO> message = MessageBuilder.withPayload(comment)
                .setHeader(KafkaHeaders.TOPIC, requestTopic)
                .setHeader(KafkaHeaders.REPLY_TOPIC, replyTopic)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();

        RequestReplyMessageFuture<Long, CommentCassandraDTO> future = replyingKafkaTemplate.sendAndReceive(message);

        SendResult<Long, CommentCassandraDTO> sendResult;
        try {
            sendResult = future.getSendFuture().get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for reply");
            throw CustomException.builder()
                    .message("Interrupted while waiting for reply")
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (ExecutionException | TimeoutException e) {
            return comment;
        }
        CommentCassandraDTO result = sendResult.getProducerRecord().value();
        log.info("Sent request: {}", result);

        return result;
    }
}
