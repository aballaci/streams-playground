package com.ballaci.kstreams;

import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableBinding(KStreamKTableBinding.class)
@SpringBootApplication
public class KstreamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(KstreamsApplication.class, args);
    }

}

interface KStreamKTableBinding {

    @Input("inputStream")
    KStream<?, ?> inputStream();

    @Input("inputTable")
    GlobalKTable<?, ?> inputTable();

    @Output("outputStream")
    KStream<?, ?> outputStream();
}