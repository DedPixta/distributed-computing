package dev.makos.discussion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestDiscussionApplication {

    @Bean
    @ServiceConnection
    CassandraContainer<?> cassandraContainer() {
        return new CassandraContainer<>(DockerImageName.parse("cassandra:latest"));
    }

    public static void main(String[] args) {
        SpringApplication.from(DiscussionApplication::main).with(TestDiscussionApplication.class).run(args);
    }

}
