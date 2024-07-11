package dev.makos.publisher.kafka.config;

import dev.makos.publisher.model.dto.CommentCassandraDTO;
import dev.makos.publisher.kafka.serde.CommentCassandraSerDe;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.request-topic}")
    private String requestTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public NewTopic topic() {
        return new NewTopic(requestTopic, 5, (short) 1);
    }

    @Bean
    public Map<String, Object> producerCommentConfigs() {
        final Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CommentCassandraSerDe.class);
        return props;
    }

    @Bean
    public ProducerFactory<Long, CommentCassandraDTO> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerCommentConfigs());
    }

    @Bean
    public KafkaTemplate<Long, CommentCassandraDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ReplyingKafkaTemplate<Long, CommentCassandraDTO, CommentCassandraDTO> replyingKafkaTemplate(
            ProducerFactory<Long, CommentCassandraDTO> pf,
            ConcurrentMessageListenerContainer<Long, CommentCassandraDTO> repliesContainer) {
        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
    }
}
