package com.zencode.app.kafka.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import com.zencode.app.ws.handlers.beans.TrackMetadataBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.common.errors.SerializationException;


public class TrackMetadataBeanSer implements Serializer<TrackMetadataBean> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public byte[] serialize(String topic, TrackMetadataBean data) {
         if (data == null) {
             return null;
         }
         try {
             return objectMapper.writeValueAsBytes(data);
         } catch (JsonProcessingException e) {
             throw new SerializationException(e);
         }
    }
}
