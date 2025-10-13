package com.zencode.app.kafka.serdes;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import com.zencode.app.ws.handlers.beans.TrackMetadataBean;

import com.zencode.app.kafka.serdes.TrackMetadataBeanSer;
import com.zencode.app.kafka.serdes.TrackMetadataBeanDer;


public class TrackMetadataBeanSerde extends Serdes.WrapperSerde<TrackMetadataBean> {
    public TrackMetadataBeanSerde() {
        super(new TrackMetadataBeanSer(), new TrackMetadataBeanDer());
    }
}

