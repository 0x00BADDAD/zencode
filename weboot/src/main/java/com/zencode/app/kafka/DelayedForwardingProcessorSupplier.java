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



public class DelayedForwardingProcessorSupplier implements ProcessorSupplier<String, String, String, String>{

    private final StoreBuilder<?> storeBuilder;

    public DelayedForwardingProcessorSupplier(final StoreBuilder<?> storeBuilder) {
         this.storeBuilder = storeBuilder;
    }

    @Override
    public Processor<String, String, String, String> get() {
        return new DelayedForwardingProcessor();
    }

    public record TimedValue<V>(V value, long timestamp) {}
    private class DelayedForwardingProcessor extends ContextualProcessor<String, String, String, String> {
        private TimestampedKeyValueStore<String, String> store;


        @Override
        public void init(ProcessorContext<String, String> context) {
            super.init(context);
            store = context.getStateStore(storeBuilder.name());
            context.schedule(Duration.ofSeconds(2),
                               PunctuationType.WALL_CLOCK_TIME,
                               this::checkAndForward);
        }


        @Override
        public void process(Record<String, String> record) {
            // simply store it in the state store. We will use the default event-time timestamp semantic
            store.put(record.key(), ValueAndTimestamp.make("Placed-in-store-" + record.value(), record.timestamp()));
        }

        public void checkAndForward(long timestamp){
           try (KeyValueIterator<String, ValueAndTimestamp<String>> iter = store.all()) {
               while (iter.hasNext()) {
                   KeyValue<String, ValueAndTimestamp<String>> entry = iter.next();
                   if (timestamp >= entry.value.timestamp() + Duration.ofSeconds(2).toMillis() ) {
                       // Forward to downstream topic (next node in topology)
                       context().forward(new Record<>(entry.key, "forwarded-" + entry.value.value(), entry.value.timestamp()));
                       store.delete(entry.key);
                   }
               }
           }
        }

    }

}

