import './style.css';
import {useContext, useEffect, useState} from 'react';
import VertBar from './VertBar';
import AutoTyping from './AutoTyping';
import SpotifyLogin from './SpotifyLogin.jsx';
import SpotifyTrack from './SpotifyTrack.jsx';
import SpotifyPlayer from './SpotifyPlayer.jsx';
import OfflineBanner from './OfflineBanner.jsx';
import Overlay from './Overlay.jsx';
import TrackMetaDataProvider from './Providers/TrackMetaDataProvider.jsx';
import WsRefProvider from './Providers/WsRefProvider.jsx';
import {DisableWebSocketContext} from './Contexts/DisableWebSocketContext.jsx';
import spotify_icon from './static/images/spotify-icon.png';




function App() {
    const disableWebSocket = useContext(DisableWebSocketContext);
    const [loginBtnDown, setLoginBtnDown] =  useState(false);
    const [isOffline, setIsOffline] = useState(false);

    const loginBtnMouseDownHn = (e) => {
        e.preventDefault();
        setLoginBtnDown(prev=>true);
        document.addEventListener("mouseup", loginBtnMouseUpHn);
    }

    const loginBtnMouseUpHn = () => {
        setLoginBtnDown(prev=>false);
        document.removeEventListener("mouseup", loginBtnMouseUpHn);
    }

    useEffect(()=>{
        window.addEventListener('offline', () => {
            setIsOffline(prev=>true);
        });

        window.addEventListener('online', () => {
            setIsOffline(prev=>false);
        });
        //console.log(`value of the currEmailGlobal is ${currEmailGlobal}`);
    }, []);

    return (
    <>
        <WsRefProvider>
            <TrackMetaDataProvider initialTrackMetaData={initialTrackMetaData}>
                <OfflineBanner isOffline={isOffline} />
                <SpotifyTrack/>
                {/*<SpotifyLogin/>*/}
                <SpotifyPlayer/>
                {/*
                userGrantedPermission ?
                        (isEligible ? <SpotifyPlayer/> :
                            (
                                <div className="player-container">
                                    <div className="player-track">
                                        <div className="login-prompt"
                                            style={{
                                                top: "25%"
                                            }}
                                        >
                                            Hey! it seems you don't have spotify Premium. This feature requires it... may be get one...?
                                        </div>
                                    </div>

                                    <div className="player-sep"
                                        style={{
                                            position: "absolute",
                                            width: "100%",
                                            top: "55.70%",
                                            left: "0%"
                                        }}
                                    >
                                    <hr
                                        style={{
                                            border: "none",
                                            height: "2px",
                                            width: "100%",
                                            backgroundColor: "#000000",
                                            margin: "0"
                                        }}
                                    />
                                    </div>

                                    <div className="control-container">
                                        <a href="https://www.spotify.com/in-en/premium/" target="_blank">
                                            <button className="login-btn"
                                                onMouseDown={loginBtnMouseDownHn}
                                                style={{
                                                    transform: `scale(${loginBtnDown ? 0.95 : 1})`
                                                }}
                                            ><img src={spotify_icon}
                                                style={{
                                                    position: "absolute",
                                                    top: "50%",
                                                    transform: "translateY(-50%)",
                                                    height: "40%",
                                                    width: "20%",
                                                    marginTop: "7.5%"
                                                }}
                                            /> Premium
                                            </button>
                                        </a>
                                    </div>
                                </div>
                            )

                    )
                :
                    (
                        <div className="player-container">
                            <div className="player-track">
                                <div className="login-prompt">
                                    Authorize Spotify to sync your playback with Atharv...
                                </div>
                            </div>

                            <div className="player-sep"
                                style={{
                                    position: "absolute",
                                    width: "100%",
                                    top: "55.70%",
                                    left: "0%"
                                }}
                            >
                                <hr
                                    style={{
                                        border: "none",
                                        height: "2px",
                                        width: "100%",
                                        backgroundColor: "#000000",
                                        margin: "0"
                                    }}
                                />
                            </div>

                            <div className="control-container">
                                <a href="http://127.0.0.1:3000/api/spotify_login_once">
                                    <button className="login-btn"
                                        onMouseDown={loginBtnMouseDownHn}
                                        style={{
                                            transform: `scale(${loginBtnDown ? 0.95 : 1})`
                                        }}
                                    >Log in with <img src={spotify_icon}
                                        style={{
                                            position: "absolute",
                                            top: "50%",
                                            transform: "translateY(-50%)",
                                            height: "40%",
                                            width: "20%",
                                            marginTop: "7.5%"
                                        }}
                                    />
                                    </button>
                                </a>
                            </div>
                        </div>
                    )
                        */}
                <Overlay/>
            </TrackMetaDataProvider>
        </WsRefProvider>
    </>
    )
}

export default App;
