package dev.makos.publisher.kafka.config;

import dev.makos.publisher.model.dto.CommentCassandraDTO;
import dev.makos.publisher.kafka.serde.CommentCassandraSerDe;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.reply-topic}")
    private String replyTopic;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "publisher_group_id");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CommentCassandraSerDe.class);
        return props;
    }

    @Bean
    public ConsumerFactory<Long, CommentCassandraDTO> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, CommentCassandraDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, CommentCassandraDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentMessageListenerContainer<Long, CommentCassandraDTO> replyContainer() {
        ConcurrentMessageListenerContainer<Long, CommentCassandraDTO> container = kafkaListenerContainerFactory().createContainer(replyTopic);
        container.getContainerProperties().setGroupId("publisher_reply_group_id");
        container.getContainerProperties().setMissingTopicsFatal(false);
        return container;
    }
}
