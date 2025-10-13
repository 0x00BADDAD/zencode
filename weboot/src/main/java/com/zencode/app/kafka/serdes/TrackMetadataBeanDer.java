package com.zencode.app.kafka.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import com.zencode.app.ws.handlers.beans.TrackMetadataBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.common.errors.SerializationException;
import java.io.IOException;

public class TrackMetadataBeanDer implements Deserializer<TrackMetadataBean> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public TrackMetadataBean deserialize(String topic, byte[] data){
         if (data == null) {
             return null;
         }
         TrackMetadataBean beanDer = null;
         try {
             beanDer = objectMapper.readValue(data, TrackMetadataBean.class);
         } catch (IOException e) {
             // do nothing
         }
         return beanDer;
    }
}

