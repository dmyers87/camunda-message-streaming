package com.ultimatesoftware.workflow.messaging.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.Lifecycle;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootConfiguration
@EmbeddedKafka
@ExtendWith(SpringExtension.class)
public class KafkaTopicContainerManagerTest {

    KafkaTopicContainerManager kafkaTopicContainerManager;

    BlockingQueue<ConsumerRecord<String, String>> records;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    Producer<String, String> producer;

    @BeforeEach
    public void setup() {
        Map<String, Object> consumerConfigs = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(
            consumerConfigs,
            new StringDeserializer(),
            new StringDeserializer()
        );

        records = new LinkedBlockingQueue<>();

        Map<String, Object> producerConfigs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producer = new DefaultKafkaProducerFactory<>(producerConfigs, new StringSerializer(), new StringSerializer())
            .createProducer();

        kafkaTopicContainerManager = new KafkaTopicContainerManager(consumerFactory);
    }

    @AfterEach
    public void teardown() {
        kafkaTopicContainerManager.stopConsumer(TestConstants.GENERIC_TOPIC_NAME);
    }

    @Test
    public void whenCreateOrStartConsumerCalled_shouldCreateNewConsumer() throws InterruptedException {
        kafkaTopicContainerManager.createOrStartConsumer(TestConstants.GENERIC_TOPIC_NAME, (MessageListener<String, String>) records::add);

        Lifecycle container = kafkaTopicContainerManager.getConsumer(TestConstants.GENERIC_TOPIC_NAME);

        assertThat(container).isNotNull();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        producer.send(new ProducerRecord<>(TestConstants.GENERIC_TOPIC_NAME, "aggregateId", "testValue"));
        producer.flush();

        ConsumerRecord<String, String> singleRecord = records.poll(100, TimeUnit.MILLISECONDS);
        assertThat(singleRecord).isNotNull();
        assertThat(singleRecord.key()).isEqualTo("aggregateId");
        assertThat(singleRecord.value()).isEqualTo("testValue");
    }

    @Test
    public void whenCreateOrStartConsumerCalledWithExistingConsumer_shouldStartConsumer() {
        kafkaTopicContainerManager.createOrStartConsumer(TestConstants.GENERIC_TOPIC_NAME, (MessageListener<String, String>) records::add);

        Lifecycle container = kafkaTopicContainerManager.getConsumer(TestConstants.GENERIC_TOPIC_NAME);

        assertThat(container).isNotNull();
        container.stop();
        assertThat(container.isRunning()).isFalse();

        kafkaTopicContainerManager.createOrStartConsumer(TestConstants.GENERIC_TOPIC_NAME, (MessageListener<String, String>) records::add);

        Lifecycle container2 = kafkaTopicContainerManager.getConsumer(TestConstants.GENERIC_TOPIC_NAME);

        assertThat(container).isEqualTo(container2);
        assertThat(container.isRunning()).isTrue();
    }

    @Test
    public void whenStopConsumerCalled_shouldStopConsumer() {
        kafkaTopicContainerManager.createOrStartConsumer(TestConstants.GENERIC_TOPIC_NAME, (MessageListener<String, String>) records::add);

        Lifecycle container = kafkaTopicContainerManager.getConsumer(TestConstants.GENERIC_TOPIC_NAME);

        assertThat(container).isNotNull();
        assertThat(container.isRunning()).isTrue();

        kafkaTopicContainerManager.stopConsumer(TestConstants.GENERIC_TOPIC_NAME);
        assertThat(container.isRunning()).isFalse();
    }

    @Test
    public void whenStopConsumerCalled_AndConsumerDoesNotExist_shouldDoNothing() {
        Lifecycle container = kafkaTopicContainerManager.getConsumer(TestConstants.GENERIC_TOPIC_NAME);

        assertThat(container).isNull();
        kafkaTopicContainerManager.stopConsumer(TestConstants.GENERIC_TOPIC_NAME);
    }
}
