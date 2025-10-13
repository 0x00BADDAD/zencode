package com.zencode.app.kafka;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;

import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import org.springframework.boot.ApplicationRunner;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.state.TimestampedKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

import com.zencode.app.kafka.DelayedForwardingProcessorSupplier;
import com.zencode.app.ws.handlers.beans.TrackMetadataBean;
import com.zencode.app.kafka.serdes.TrackMetadataBeanSerde;
import java.util.List;


@Configuration
public class KafkaStreamsConfig {

    private static final Logger logger = LogManager.getLogger(KafkaStreamsConfig.class);

    @Bean
    public KStream<String, TrackMetadataBean> kafkaTopology(StreamsBuilder builder) {

        TrackMetadataBeanSerde trackMetadataBeanSerde = new TrackMetadataBeanSerde();

        StoreBuilder<TimestampedKeyValueStore<String, TrackMetadataBean>> storeBuilder =
                Stores.timestampedKeyValueStoreBuilder(
                   Stores.persistentTimestampedKeyValueStore("delay-spotify-track-store"),
                   Serdes.String(),
                   trackMetadataBeanSerde
                );
        builder.addStateStore(storeBuilder);

        KStream<String, TrackMetadataBean> stream = builder.stream("spotify-track-topic",
                       Consumed.with(Serdes.String(), trackMetadataBeanSerde));

                       stream.process(new DelayedForwardingProcessorSupplier(storeBuilder), storeBuilder.name())
                       .to("delay-spotify-track-topic",
                       Produced.with(Serdes.String(), trackMetadataBeanSerde));

        return stream;

    }


    @Bean
    public NewTopic trackTopic() {
        return TopicBuilder.name("spotify-track-topic")
                .partitions(10)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic delayTopic() {
        return TopicBuilder.name("delay-spotify-track-topic")
                .partitions(10)
                .replicas(1)
                .build();
    }

   // @Bean
   // public ApplicationRunner runner(KafkaTemplate<String, TrackMetadataBean> template) {
   //             TrackMetadataBean emptyBean = new TrackMetadataBean("No music playing right now!", List.of(), "No-track", "No-resource", 0, 0, false, 0, false, "No-device-active", "No-img-url", false, false);
   //     return args -> {
   //         template.send("spotify-track-topic", "some-track-id", emptyBean);
   //     };
   // }


    @KafkaListener(id = "my-group", topics = "delay-spotify-track-topic")
    public void listen(TrackMetadataBean in) {
        logger.debug("We got the delayed bean from kafka topic --> {}", in);
    }
}

