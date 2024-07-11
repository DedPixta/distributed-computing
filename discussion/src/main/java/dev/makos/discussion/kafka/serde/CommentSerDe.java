package dev.makos.discussion.kafka.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.makos.discussion.exception.KafkaException;
import dev.makos.discussion.model.dto.CommentDTO;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RequiredArgsConstructor
public class CommentSerDe implements Serializer<CommentDTO>, Deserializer<CommentDTO> {
    private final ObjectMapper objectMapper;

    public CommentSerDe() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Configuration if needed
    }

    @Override
    public byte[] serialize(String topic, CommentDTO comment) {
        try {
            return objectMapper.writeValueAsBytes(comment);
        } catch (Exception e) {
            throw new KafkaException(HttpStatus.BAD_REQUEST , "Failed to serialize comment");
        }
    }

    @Override
    public CommentDTO deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, CommentDTO.class);
        } catch (Exception e) {
            throw new KafkaException(HttpStatus.BAD_REQUEST , "Failed to deserialize comment");
        }
    }

    @Override
    public void close() {
        // Cleanup if needed
    }
}

