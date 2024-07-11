package dev.makos.publisher.kafka.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.makos.publisher.exception.KafkaException;
import dev.makos.publisher.model.dto.CommentCassandraDTO;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RequiredArgsConstructor
public class CommentCassandraSerDe implements Serializer<CommentCassandraDTO>, Deserializer<CommentCassandraDTO> {
    private final ObjectMapper objectMapper;

    public CommentCassandraSerDe() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Configuration if needed
    }

    @Override
    public byte[] serialize(String topic, CommentCassandraDTO comment) {
        try {
            return objectMapper.writeValueAsBytes(comment);
        } catch (Exception e) {
            throw new KafkaException(HttpStatus.BAD_REQUEST , "Failed to serialize comment");
        }
    }

    @Override
    public CommentCassandraDTO deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, CommentCassandraDTO.class);
        } catch (Exception e) {
            throw new KafkaException(HttpStatus.BAD_REQUEST , "Failed to deserialize comment");
        }
    }

    @Override
    public void close() {
        // Cleanup if needed
    }
}

