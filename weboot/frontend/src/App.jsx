import './style.css';
import VertBar from './VertBar';
import AutoTyping from './AutoTyping';
import SpotifyLogin from './SpotifyLogin.jsx';
import SpotifyTrack from './SpotifyTrack.jsx';
import SpotifyPlayer from './SpotifyPlayer.jsx';
import TrackMetaDataProvider from './Providers/TrackMetaDataProvider.jsx';

function App() {

    return (
    <>
        <TrackMetaDataProvider initialTrackMetaData={initialTrackMetaData}>
            <SpotifyTrack/>
            <SpotifyLogin/>
            {userGrantedPermission &&
            <SpotifyPlayer/>}
        </TrackMetaDataProvider>
    </>
    )
}

export default App;
