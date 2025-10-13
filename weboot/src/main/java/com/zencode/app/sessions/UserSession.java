package com.zencode.app.sessions;


import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class UserSession {
    private String  sessionId;
    private boolean keepInSync;
    private boolean isRemoteDevice;
    private String remoteDeviceId;
    private boolean syncing;

    public UserSession(String sessionId, boolean keepInSync, boolean isRemoteDevice, String remoteId, boolean isSyncing){
        this.sessionId = sessionId;
        this.keepInSync = keepInSync;
        this.isRemoteDevice = isRemoteDevice;
        this.remoteDeviceId = remoteId;
        this.syncing = isSyncing;
    }
}
