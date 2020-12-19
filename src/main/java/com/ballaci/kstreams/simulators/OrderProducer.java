package com.ballaci.kstreams.simulators;

import com.ballaci.kstreams.model.OrderThin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Sample producer application using Reactive API for Kafka.
 * To run sample producer
 * <ol>
 *   <li> Start Zookeeper and Kafka server
 *   <li> Update {@link #BOOTSTRAP_SERVERS} and {@link #TOPIC} if required
 *   <li> Create Kafka topic {@link #TOPIC}
 *   <li> Run {@link OrderProducer} as Java application with all dependent jars in the CLASSPATH (eg. from IDE).
 *   <li> Shutdown Kafka server and Zookeeper when no longer required
 * </ol>
 */
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class.getName());

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "orders";

    private final KafkaSender<String, String> sender;
    private final SimpleDateFormat dateFormat;
    private final Faker faker = new Faker();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderProducer(String bootstrapServers) {


        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "sample-producer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        SenderOptions<String, String> senderOptions = SenderOptions.create(props);

        sender = KafkaSender.create(senderOptions);
        dateFormat = new SimpleDateFormat("HH:mm:ss:SSS z dd MMM yyyy");
    }

    public void sendMessages(String topic, int count, CountDownLatch latch) throws InterruptedException, JsonProcessingException {
        sender.<Integer>send(Flux.range(1, count)
                .map(i -> {
                    String order = "";
                    try {
                        order = this.objectMapper.writeValueAsString(new OrderThin(i.toString(), i.toString(), String.valueOf(faker.random().nextInt(42000)), faker.random().nextInt(100)));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    log.info(order);
                    return SenderRecord.create(new ProducerRecord<>(topic, i.toString(), order), i);
                }))
                .doOnError(e -> log.error("Send failed", e))
                .subscribe(r -> {
                    RecordMetadata metadata = r.recordMetadata();
                    System.out.printf("Message %d sent successfully, topic-partition=%s-%d offset=%d timestamp=%s\n",
                            r.correlationMetadata(),
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset(),
                            dateFormat.format(new Date(metadata.timestamp())));
                    latch.countDown();
                });
    }

    public void close() {
        sender.close();
    }

    public static void main(String[] args) throws Exception {
        int count = 20;
        CountDownLatch latch = new CountDownLatch(count);
        OrderProducer producer = new OrderProducer(BOOTSTRAP_SERVERS);
        producer.sendMessages(TOPIC, count, latch);
        latch.await(10, TimeUnit.SECONDS);
        producer.close();
    }
}
