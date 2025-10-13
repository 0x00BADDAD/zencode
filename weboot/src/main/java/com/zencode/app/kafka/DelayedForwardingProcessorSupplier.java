package com.zencode.app.kafka;

import org.apache.kafka.streams.processor.*;
import org.apache.kafka.streams.state.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.KeyValue;
import java.time.Duration;
import org.apache.kafka.streams.state.TimestampedKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

import com.zencode.app.ws.handlers.beans.TrackMetadataBean;


public class DelayedForwardingProcessorSupplier implements ProcessorSupplier<String, TrackMetadataBean, String, TrackMetadataBean>{

    private final StoreBuilder<?> storeBuilder;

    public DelayedForwardingProcessorSupplier(final StoreBuilder<?> storeBuilder) {
         this.storeBuilder = storeBuilder;
    }

    @Override
    public Processor<String, TrackMetadataBean, String, TrackMetadataBean> get() {
        return new DelayedForwardingProcessor();
    }

    public record TimedValue<V>(V value, long timestamp) {}
    private class DelayedForwardingProcessor extends ContextualProcessor<String, TrackMetadataBean, String, TrackMetadataBean> {
        private TimestampedKeyValueStore<String, TrackMetadataBean> store;


        @Override
        public void init(ProcessorContext<String, TrackMetadataBean> context) {
            super.init(context);
            store = context.getStateStore(storeBuilder.name());
            context.schedule(Duration.ofSeconds(2),
                               PunctuationType.WALL_CLOCK_TIME,
                               this::checkAndForward);
        }


        @Override
        public void process(Record<String, TrackMetadataBean> record) {
            // simply store it in the state store. We will use the default event-time timestamp semantic
            store.put(record.key(), ValueAndTimestamp.make(record.value(), record.timestamp()));
        }

        public void checkAndForward(long timestamp){
           try (KeyValueIterator<String, ValueAndTimestamp<TrackMetadataBean>> iter = store.all()) {
               while (iter.hasNext()) {
                   KeyValue<String, ValueAndTimestamp<TrackMetadataBean>> entry = iter.next();
                   if (timestamp >= entry.value.timestamp() + Duration.ofSeconds(2).toMillis() ) {
                       // Forward to downstream topic (next node in topology)
                       context().forward(new Record<>(entry.key, entry.value.value(), entry.value.timestamp()));
                       store.delete(entry.key);
                   }
               }
           }
        }

    }

}

