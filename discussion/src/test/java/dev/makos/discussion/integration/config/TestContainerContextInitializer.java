package dev.makos.discussion.integration.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.CassandraContainer;

public class TestContainerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra:4.0");

    static {
        cassandraContainer.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.data.cassandra.contact-points=" + cassandraContainer.getHost() + ":" + cassandraContainer.getFirstMappedPort(),
                "spring.data.cassandra.keyspace-name=testkeyspace",
                "spring.data.cassandra.local-datacenter=datacenter1"
        ).applyTo(applicationContext.getEnvironment());
    }
}
