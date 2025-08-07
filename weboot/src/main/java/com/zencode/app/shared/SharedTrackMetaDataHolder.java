package com.zencode.app.shared;

import org.springframework.stereotype.Component; // For the @Component annotation
import java.util.concurrent.atomic.AtomicReference; // For AtomicReference
import com.zencode.app.ws.handlers.beans.TrackMetadataBean;

@Component
public class SharedTrackMetaDataHolder {

    private final AtomicReference<TrackMetadataBean> data = new AtomicReference<>();

    public TrackMetadataBean getData() {
        return data.get();
    }

    public void setData(TrackMetadataBean newData) {
        data.set(newData);
    }
}
