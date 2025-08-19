import './style.css';
import {useContext} from 'react';
import VertBar from './VertBar';
import AutoTyping from './AutoTyping';
import SpotifyLogin from './SpotifyLogin.jsx';
import SpotifyTrack from './SpotifyTrack.jsx';
import SpotifyPlayer from './SpotifyPlayer.jsx';
import TrackMetaDataProvider from './Providers/TrackMetaDataProvider.jsx';
import WsRefProvider from './Providers/WsRefProvider.jsx';
import {DisableWebSocketContext} from './Contexts/DisableWebSocketContext.jsx';




function App() {
    const disableWebSocket = useContext(DisableWebSocketContext);

    return (
    <>
        <WsRefProvider>
            <TrackMetaDataProvider initialTrackMetaData={initialTrackMetaData}>
                <SpotifyTrack/>
                <SpotifyLogin/>
                {userGrantedPermission &&
                <SpotifyPlayer/>}
                 {disableWebSocket && (
                        <>
                          {/* Overlay with partial blur */}
                          <div className="overlay" />

                          {/* Modal content */}
                          <div className="modal">
                            <h2>Attention</h2>
                            <p>This playback has timedout and no longer updated. kindly close this tab/window.</p>
                          </div>
                        </>
                      )}
            </TrackMetaDataProvider>
        </WsRefProvider>
    </>
    )
}

export default App;
