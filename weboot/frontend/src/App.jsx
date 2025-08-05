import './style.css';
import VertBar from './VertBar';
import AutoTyping from './AutoTyping';
import SpotifyLogin from './SpotifyLogin.jsx';
import SpotifyTrack from './SpotifyTrack.jsx';
import SpotifyPlayer from './SpotifyPlayer.jsx';

function App() {

    return (
    <>
        <SpotifyTrack/>
        <SpotifyLogin/>
        {/*userGrantedPermission &&
        <SpotifyPlayer/>*/}
    </>
    )
}

export default App;
