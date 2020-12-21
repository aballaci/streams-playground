package com.ballaci.kstreams;


import com.ballaci.kstreams.model.Document;
import com.ballaci.kstreams.model.UserTag;
import com.ballaci.kstreams.model.UserTags;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(ports = {9092})
@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RoutingIT {

    private static final String USER_TAGS_TOPIC_NAME = "user_tags";
    private static final String DOCUMENTS_TOPIC_NAME = "documents";
    private static final String NOT_RELEVANT_TOPIC_NAME = "not_relevant";
    private static final String DOCUMENT_APPROVAL_TOPIC_NAME = "document_approval";


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    // Autowire the kafka broker registered via @EmbeddedKafka
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Before
    void setUp() {
    }

    @After
    void tearDown() {
    }

    @Test
    public void shouldRouteOneDocumentToNotRelevantTopic() throws InterruptedException, ExecutionException {
        Map<String, Object> produserConfig = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafka));
        Producer<String, UserTags> userTagsProducer = new DefaultKafkaProducerFactory<>(produserConfig, new StringSerializer(), new JsonSerializer<UserTags>()).createProducer();
        Producer<String, Document> documentsProducer = new DefaultKafkaProducerFactory<>(produserConfig, new StringSerializer(), new JsonSerializer<Document>()).createProducer();


        userTagsProducer.send(new ProducerRecord<String, UserTags>(USER_TAGS_TOPIC_NAME, "1", createUserTags("1", true))).get();
        userTagsProducer.send(new ProducerRecord<String, UserTags>(USER_TAGS_TOPIC_NAME, "2", createUserTags("2", false))).get();

        documentsProducer.send(new ProducerRecord<String, Document>(DOCUMENTS_TOPIC_NAME, "doc1", new Document("doc1", "1", "RechnungEingang", false))).get();
        documentsProducer.send(new ProducerRecord<String, Document>(DOCUMENTS_TOPIC_NAME, "doc2", new Document("doc2", "1", "RechnungEingang", false))).get();

        documentsProducer.send(new ProducerRecord<String, Document>(DOCUMENTS_TOPIC_NAME, "doc3", new Document("doc3", "2", "RechnungEingang", false))).get();

        Map<String, Object> config = new HashMap<>(KafkaTestUtils.consumerProps("not-relevant-consumer", "true", embeddedKafka));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Consumer<String, Document> consumer = new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(Document.class)).createConsumer();
        consumer.subscribe(Collections.singleton(NOT_RELEVANT_TOPIC_NAME));

        List<Document> documents = new ArrayList<>();
        Awaitility.await().atMost(5L, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, Document> messages = consumer.poll(Duration.ofMillis(500));
            messages.forEach(m -> {
                log.info(m.value().toString());
                documents.add(m.value());
            });
            assertThat(documents).isNotEmpty();
            assertThat(documents.size()).isEqualTo(1);
            assertThat(documents.get(0).isApprovalRelevant()).isFalse();
            assertThat(documents.get(0).getId()).isEqualTo("doc3");

        });
    }

    @Test
    public void shouldRouteTwoDocumentsToApprovalRelevantTopic() throws InterruptedException, ExecutionException {
        Map<String, Object> produserConfig = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafka));
        Producer<String, UserTags> userTagsProducer = new DefaultKafkaProducerFactory<>(produserConfig, new StringSerializer(), new JsonSerializer<UserTags>()).createProducer();
        Producer<String, Document> documentsProducer = new DefaultKafkaProducerFactory<>(produserConfig, new StringSerializer(), new JsonSerializer<Document>()).createProducer();


        userTagsProducer.send(new ProducerRecord<String, UserTags>(USER_TAGS_TOPIC_NAME, "1", createUserTags("1", true))).get();
        userTagsProducer.send(new ProducerRecord<String, UserTags>(USER_TAGS_TOPIC_NAME, "2", createUserTags("2", false))).get();

        documentsProducer.send(new ProducerRecord<String, Document>(DOCUMENTS_TOPIC_NAME, "doc1", new Document("doc1", "1", "RechnungEingang", false))).get();
        documentsProducer.send(new ProducerRecord<String, Document>(DOCUMENTS_TOPIC_NAME, "doc2", new Document("doc2", "1", "RechnungEingang", false))).get();

        documentsProducer.send(new ProducerRecord<String, Document>(DOCUMENTS_TOPIC_NAME, "doc3", new Document("doc3", "2", "RechnungEingang", false))).get();

        Map<String, Object> config = new HashMap<>(KafkaTestUtils.consumerProps("not-relevant-consumer", "true", embeddedKafka));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        Consumer<String, Document> consumer = new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(Document.class)).createConsumer();
        consumer.subscribe(Collections.singleton(DOCUMENT_APPROVAL_TOPIC_NAME));

        List<Document> documents = new ArrayList<>();
        Awaitility.await().atMost(5L, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, Document> messages = consumer.poll(Duration.ofMillis(500));
            messages.forEach(m -> {
                log.info(m.value().toString());
                documents.add(m.value());
            });
            assertThat(documents).isNotEmpty();
            assertThat(documents.size()).isEqualTo(2);
            assertThat(documents.get(0).isApprovalRelevant()).isTrue();
            assertThat(documents.get(1).isApprovalRelevant()).isTrue();
        });
    }

    private static UserTags createUserTags(String userId, boolean isRrelevant) {
        UserTags ut = new UserTags();
        ut.setUserId(userId);
        List<UserTag> tags = new ArrayList<UserTag>();
        tags.add(new UserTag("RechnungEingang", isRrelevant));
        tags.add(new UserTag("RechnungAusgang", isRrelevant));
        ut.setTags(tags);
        return ut;
    }
}