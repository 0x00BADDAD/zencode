import {useState, useEffect, useContext, useRef} from 'react';
import {TrackMetaDataContext} from './Contexts/TrackMetaDataContext.jsx';
import {WsRefContext} from './Contexts/WsRefContext.jsx';
import {DisableWebSocketContext} from './Contexts/DisableWebSocketContext.jsx';
import {DisableWebSocketDispatchContext} from './Contexts/DisableWebSocketContext.jsx';
import Player from './Player.jsx';
import RebootButton from './RebootButton.jsx';
import ErrorBanner from './ErrorBanner.jsx';
import EmailInput from './EmailInput.jsx';
import OTPinput from './OTPinput.jsx';
import LoggedoutBanner from './LoggedoutBanner.jsx';
import PlayerControls from './PlayerControls.jsx';
import {stages} from './stages.jsx';
import spotify_icon from './static/images/spotify-icon.png';

async function fetchAccessToken(sessionId){
        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        const resp = await fetch(`https://unmei.space/api/fresh_token?${params.toString()}`, {
            method: "GET"
        });
        const token = await resp.json();
        return token.access_token;
}


// SpotifyPlayer needs to depend upon reactive value of context_uri which will be fed from SpotifyTrack ws
// endpoint and do a PUT request to the spotify api every time the context_uri is changed
export default function SpotifyPLayer(){


    const [playerRef, setPlayerRef] = useState(null);
    //const [wsRef, setWsRef] = useState(null);
    //const [deviceIds, setDeviceIds] = useState([]);
    //const [activeDeviceId, setActiveDeviceId] = useState(null);
    const [outOfSync, setOutOfSync] = useState(true);
    const [keepInSync, setKeepInSync] = useState(false);
    const [syncing, setSyncing] = useState(false);
    const [initPlayer, setInitPlayer] = useState(false);

    const [loadingNextTrack, setLoadingNextTrack] = useState(false);

    const [loadingPrevTrack, setLoadingPrevTrack] = useState(false);

    //const [transferringPlayback, setTransferringPlayback] = useState(false);
    // currTrackMetaData is the most updated value of the track meta data right from the
    // spotify backend but with an extra websocket hop in between.
    const currTrackMetaData = useContext(TrackMetaDataContext);
    const disableWebSocket = useContext(DisableWebSocketContext);
    const disableWebSocketDispatch = useContext(DisableWebSocketDispatchContext);
    const wsRef = useContext(WsRefContext);

    const oldPlaybackId = useRef(null);
    const newPlaybackId = useRef(null);

    const [newPlaybackActive, setNewPlaybackActive] = useState(false);
    const [currDeviceId, setCurrDeviceId] = useState(null);

    const [currErrMsgToSend, setCurrErrMsgToSend] = useState("initial err message");
    const [errContentToSend, setErrContentToSend] = useState({API_NAME: "/api/some_api", stacktrace: "some huge JVM trace"});
    //const [playbackTransferred, setPlaybackTransferred] = useState(false);

    const [currStage, setCurrStage] = useState(stageFromServer);
    //const [currStage, setCurrStage] = useState(stages.NOT_PREMIUM);

    const [prevStage, setPrevStage] = useState(null);

    const initialMetaData = {
       track_uri: "No uri",
       progress_ms: 0,
       duration_ms: 0,
       name: "No Name",
       is_playing: false,
       artists: [],
       img_url: ""
    };

    const [metaData, setMetaData] = useState(initialMetaData);

    async function syncTrack(){

        const isTrackInActive = currTrackMetaData.name === "No music playing right now!" || currTrackMetaData.name === "It seems Atharv is listening to a podcast!";
        if(isTrackInActive){return;}
        const params = new URLSearchParams();
        params.append('session_id', sessionId); // this is a global defined in thymeleaf "hello-world" templates...
        //const deviceId = (newPlaybackActive ? newPlaybackId.current : oldPlaybackId.current);
        //params.append('device_id', deviceId);
        setSyncing(prev=>true);
        const resp = await fetch(`https://unmei.space/api/sync_track?${params.toString()}`);
        // only after the above fetch has been done
        if (!resp.ok){
            //throw new Error("first fetch to play a new track failed!");
            const errContent = await resp.json();
            const errMsgToSend = `ERR in syncTrack() in SpotifyPlayer.jsx [150]: res status: ${resp.status}:${resp.statusText}`;
            setCurrErrMsgToSend(prev=>errMsgToSend);
            setErrContentToSend(prev=>errContent);

            if(currStage !== stages.ERR){
                setPrevStage(prev=>currStage);
                setCurrStage(prev=>stages.ERR);
            }
            //setHideError(prev=>false);
        }
        setSyncing(false);

    }



    async function lockTrack(){

        const isTrackInActive = currTrackMetaData.name === "No music playing right now!" || currTrackMetaData.name === "It seems Atharv is listening to a podcast!";
        if(isTrackInActive){return;}

        const params = new URLSearchParams();
        params.append('session_id', sessionId); // this is a global defined in thymeleaf "hello-world" templates...
        //setSyncing(prev=>true);
        const resp = await fetch(`https://unmei.space/api/lock_track?${params.toString()}`);
        // only after the above fetch has been done
        if (!resp.ok){
            //throw new Error("first fetch to play a new track failed!");
            const errContent = await resp.json();
            const errMsgToSend = `ERR in lockTrack() in SpotifyPlayer.jsx [168]: res status: ${resp.status}:${resp.statusText}`;
            setCurrErrMsgToSend(prev=>errMsgToSend);
            setErrContentToSend(prev=>errContent);

            if(currStage !== stages.ERR){
                setPrevStage(prev=>currStage);
                setCurrStage(prev=>stages.ERR);
            }
            //setHideError(prev=>false);
        }
    }

    const [lockingOut, setLockingOut] = useState(false);

    async function lockOutTrack(){
        const params = new URLSearchParams();
        params.append('session_id', sessionId); // this is a global defined in thymeleaf "hello-world" templates...
        setLockingOut(prev=>true);
        const resp = await fetch(`https://unmei.space/api/lock_out_track?${params.toString()}`);
        // only after the above fetch has been done
        if (!resp.ok){
            //throw new Error("first fetch to play a new track failed!");
            const errContent = await resp.json();
            const errMsgToSend = `ERR in lockOutTrack() in SpotifyPlayer.jsx [174]: res status: ${resp.status}:${resp.statusText}`;
            setCurrErrMsgToSend(prev=>errMsgToSend);
            setErrContentToSend(prev=>errContent);

            if(currStage !== stages.ERR){
                setPrevStage(prev=>currStage);
                setCurrStage(prev=>stages.ERR);
            }
            //setHideError(prev=>false);
        }
        setLockingOut(prev=>false);
    }


    async function nextTrack(){
        setLoadingNextTrack(prev=>true);
        if(newPlaybackActive){
            playerRef.nextTrack().then(() => {
              console.log('Skipped to next track!');
            });
            return;
        }
        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        // TODO: something to return from this request
        const resp = await fetch(`https://unmei.space/api/next_track?${params.toString()}`);
        if(!resp.ok){
            //throw new Error("fetch to play the next track didn't work");
            const errContent = await resp.json();
            const errMsgToSend = `ERR in lockTrack() in SpotifyPlayer.jsx [196]: res status: ${resp.status}:${resp.statusText}`;
            setCurrErrMsgToSend(prev=>errMsgToSend);
            setErrContentToSend(prev=>errContent);

            if(currStage !== stages.ERR){
                setPrevStage(prev=>currStage);
                setCurrStage(prev=>stages.ERR);
            }
            //setHideError(prev=>false);
        }
        setLoadingNextTrack(prev=>false);
    }

    async function prevTrack(){
        setLoadingPrevTrack(prev=>true);
        if(newPlaybackActive){
            playerRef.previousTrack().then(() => {
              console.log('Set to previous track!');
            });
            //setLoadingPrevTrack(false);
            return;
        }
        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        // TODO: something to return from this request
        const resp = await fetch(`https://unmei.space/api/prev_track?${params.toString()}`);
        if(!resp.ok){
            //throw new Error("fetch to play the prev track didn't work");
            const errContent = await resp.json();
            const errMsgToSend = `ERR in lockTrack() in SpotifyPlayer.jsx [218]: res status: ${resp.status}:${resp.statusText}`;
            setCurrErrMsgToSend(prev=>errMsgToSend);
            setErrContentToSend(prev=>errContent);

            if(currStage !== stages.ERR){
                setPrevStage(prev=>currStage);
                setCurrStage(prev=>stages.ERR);
            }

            //setHideError(prev=>false);
        }
        setLoadingPrevTrack(prev=>false);
    }

    async function pauseTrack(){
        // this is to pause the track in the old playback of the device via spotify web api
        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        const resp = await fetch(`https://unmei.space/api/pause_track?${params.toString()}`);
        if(!resp.ok){
            //throw new Error("fetch to puase the track didn't work");
            const errContent = await resp.json();
            const errMsgToSend = `ERR in lockTrack() in SpotifyPlayer.jsx [232]: res status: ${resp.status}:${resp.statusText}`;
            setCurrErrMsgToSend(prev=>errMsgToSend);
            setErrContentToSend(prev=>errContent);

            if(currStage !== stages.ERR){
                setPrevStage(prev=>currStage);
                setCurrStage(prev=>stages.ERR);
            }

            //setHideError(prev=>false);
        }
    }



    async function resumeTrack(){
        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        params.append('device_id', oldPlaybackId.current);
        const resp = await fetch(`https://unmei.space/api/resume_track?${params.toString()}`);
        if(!resp.ok){
            //throw new Error("fetch to resume the track didn'tm work");
            const errContent = await resp.json();
            const errMsgToSend = `ERR in lockTrack() in SpotifyPlayer.jsx [245]: res status: ${resp.status}:${resp.statusText}`;
            setCurrErrMsgToSend(prev=>errMsgToSend);
            setErrContentToSend(prev=>errContent);

            if(currStage !== stages.ERR){
                setPrevStage(prev=>currStage);
                setCurrStage(prev=>stages.ERR);
            }

            //setHideError(prev=>false);
        }
    }




    const [seeking, setSeeking] = useState(false);


    async function seekTrack(seekMs){
        if(newPlaybackActive){
            playerRef.seek(seekMs).then(() => {
              console.log('Changed position!');
            });
            return;
        }
        setSeeking(prev=>true);
        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        params.append('seek_ms', seekMs);
        const resp = await fetch(`https://unmei.space/api/seek_track?${params.toString()}`);
        if(!resp.ok){
            //throw new Error("fetch to resume the track didn't work");
            const errContent = await resp.json();
            const errMsgToSend = `ERR in lockTrack() in SpotifyPlayer.jsx [265]: res status: ${resp.status}:${resp.statusText}`;
            setCurrErrMsgToSend(prev=>errMsgToSend);
            setErrContentToSend(prev=>errContent);
            if(currStage != stages.ERR){
                setPrevStage(prev=>currStage);
                setCurrStage(prev=>stages.ERR);
            }

            //setHideError(prev=>false);
        }
        setSeeking(prev=>false);
    }


    async function pauseTrackHandleOldAndNew(){
            if(newPlaybackActive){
                playerRef.pause().then(() => {
                      console.log('Paused!');
                });
            }else{
                (async ()=>{await pauseTrack();})();
            }
    }

    async function resumeTrackHandleOldAndNew(){
            if(newPlaybackActive){
                playerRef.resume().then(() => {
                      console.log('Resumed!');
                });
            }else{
                (async ()=>{await resumeTrack();})();
            }
    }

    const [hidePlayerControls, setHidePlayerControls] = useState(false);

    useEffect(()=>{
        if(!!currTrackMetaData.err_found){
            //const errContent = {
            //    API_NAME: "SpotifyTasks scheduled in currTrackMetaData fetch error in backend",
            //    stacktrace: currTrackMetaData.err_found_stack_trace
            //};

            //const errMsgToSend = `ERR in SpotifyTasks fetch backend!`;
            //setCurrErrMsgToSend(prev=>errMsgToSend);
            //setErrContentToSend(prev=>errContent);
            //setHideError(prev=>false);
            setHidePlayerControls(prev=>true);
        }else{
            setHidePlayerControls(prev=>false);
        }
    }, [currTrackMetaData]);



    const [triggerLoadingNextTrack, setTriggerLoadingNextTrack] = useState(false);

    //web socket that will receive messages from the backend for the metaData for old playback
    useEffect(()=>{
        let ws = null;
        if(!disableWebSocket && currStage === stages.ALL_CLEAR){
                //Connect to WebSocket server
                ws = new WebSocket(`wss://unmei.space/ws1?session_id=${sessionId}`);
                //setSocket(ws);
                //When message is received
                ws.onmessage = (event) => {
                  try {
                    const data = JSON.parse(event.data); // if message is JSON
                    console.log(`data recd in player socket is: ${data}`);
                    const {name, track_uri, resource_uri, artists , progress_ms, duration_ms, is_playing, disc_number, atharv_track, device_id, img_url, can_skip_prev, is_in_sync, err_found, err_found_stack_trace} = data;

                    if(!atharv_track && err_found){
                        const errContent = {
                            API_NAME: "remotePlayback fetch error in backend",
                            stacktrace: err_found_stack_trace
                        };

                        const errMsgToSend = `ERR in remotePlayback fetch backend!`;
                        setCurrErrMsgToSend(prev=>errMsgToSend);
                        setErrContentToSend(prev=>errContent);

                        if(currStage !== stages.ERR){
                            setPrevStage(prev=>currStage);
                            setCurrStage(prev=>stages.ERR);
                        }
                        //setHideError(prev=>false);
                    }


                    if(!atharv_track && !newPlaybackActive){
                          //if(oldPlaybackId.current === null || oldPlaybackId.current === "No-device-active" || (oldPlaybackId.current !== device_id && newPlaybackId.current !== device_id)) {
                          //      oldPlaybackId.current = device_id;
                          //}
                          //setCurrDeviceId(device_id);

                          console.log(`value of deviceId for the remote device is: ${device_id}`);

                          const newMetaData = {
                               track_uri: track_uri,
                               progress_ms: progress_ms,
                               duration_ms: duration_ms,
                               name: name,
                               is_playing: is_playing,
                               disc_number: disc_number,
                               artists: artists,
                               img_url: img_url,
                               can_skip_prev: can_skip_prev,
                               is_in_sync: is_in_sync
                          };


                          if(!metaData || track_uri !== metaData.track_uri || progress_ms !== metaData.progress_ms || is_playing !== metaData.is_playing){
                                    setMetaData(prev=>newMetaData);
                          }
                      }
                  } catch (e) {
                      //const err = {
                      //    'error' : "something went wrong on parsing the received message"
                      //};
                      //setMetaData(prev => err); // plain text fallback

                    const errContent = {
                        API_NAME: "json parsing error",
                        stacktrace: "Err while parsing json response in remote playback web socket!"
                    };

                    const errMsgToSend = `ERR in useEffect() while parsing recd ws JSON msg in SpotifyPlayer.jsx [447]: res status: ${resp.status}:${resp.statusText}`;
                    setCurrErrMsgToSend(prev=>errMsgToSend);
                    setErrContentToSend(prev=>errContent);

                    if(currStage !== stages.ERR){
                        setPrevStage(prev=>currStage);
                        setCurrStage(prev=>stages.ERR);
                    }
                    //setHideError(prev=>false);
                  }
                };

                ws.onerror = (error) => {
                    //console.error("WebSocket error:", error);
                    const errContent = {
                        API_NAME: "websocket/remote playback",
                        stacktrace: "Err received in remote playback web socket!"
                    };

                    const errMsgToSend = `ERR in useEffect() while polling backend via ws in SpotifyPlayer.jsx [455]: res status: ${resp.status}:${resp.statusText}`;
                    setCurrErrMsgToSend(prev=>errMsgToSend);
                    setErrContentToSend(prev=>errContent);

                    if(currStage !== stages.ERR){
                        setPrevStage(prev=>currStage);
                        setCurrStage(prev=>stages.ERR);
                    }

                    //setHideError(prev=>false);
                    };

                    ws.onclose = () => {
                      console.log("WebSocket connection closed");
                    };
                //wsRefDispatch({
                //    type: 'enabled',
                //    wsRef: ws
                //});
            }

            // Cleanup on component unmount
            return () => {
                if(!disableWebSocket && ws){
                   // wsRefDispatch({
                   //     type: 'enabled',
                   //     wsRef: null
                   // });
                   ws.close();
                }
            };
    },[disableWebSocket, currStage]);



    const [hideError, setHideError] = useState(false);
    const [rebootDisable, setRebootDisable] = useState(true);
    const [errReported, setErrReported] = useState(false);
    const [reportingError, setReportingError] = useState(false);

    async function sendEmailReport(){
        setReportingError(prev=>true);

        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        // TODO: something to return from this request

        const formData = new FormData();
        formData.append("errMsg", currErrMsgToSend);
        formData.append("errContentApiName", errContentToSend.API_NAME);
        formData.append("errContentStackTrace", errContentToSend.stacktrace);

        const resp = await fetch(`https://unmei.space/api/send_err_report?${params.toString()}`, {
            method: 'POST',
            body: formData
        });
        if(!resp.ok){
                    //throw new Error("fetch to sending error report didn't work");
                    const errContent = await resp.json();
                    const errMsgToSend = `ERR in sendEmailReport() in SpotifyPlayer.jsx [762]: res status: ${resp.status}:${resp.statusText}`;
                    setErrContentToSend(prev=>errContent);
                    setCurrErrMsgToSend(prev=>errMsgToSend);

                    if(currStage !== stages.ERR){
                        setPrevStage(prev=>currStage);
                        setCurrStage(prev=>stages.ERR);
                    }

                    //setHideError(prev=>false);
                    setErrReported(prev=>false);
                    setReportingError(prev=>false);
                    return;
        }
        setReportingError(prev=>false);
        setErrReported(prev=>true);
    }



    const [emailSent, setEmailSent] = useState(false);

    const deviceIds = playerRef ? ["This-device", "Other-device"] : ["Other-device"];

    const [currStatus, setCurrStatus] = useState(1);


    if(!keepInSync && !metaData.is_in_sync && currStatus !== 1){
        setCurrStatus(prev=>1);
    }

    if(!keepInSync && metaData.is_in_sync && currStatus !== 0){
        setCurrStatus(prev=>0);
    }

    const isInActive = metaData.name === "No music playing right now!" || metaData.name === "It seems you are listening to a podcast!" || oldPlaybackId.current === "No-device-active";
    const isPodcast = metaData.name === "It seems you are listening to a podcast!";
    //const prettyJson = JSON.stringify(metaData, undefined, 2);
    const isTrackInActive = currTrackMetaData.name === "No music playing right now!" || currTrackMetaData.name === "It seems Atharv is listening to a podcast!";
    const currMetaDataToBePassed = (keepInSync && !isInActive && !isTrackInActive) ? currTrackMetaData : metaData;

    console.log(`currTrackMetaData.duration_ms: ${currTrackMetaData.duration_ms}`);
    const rendering = ((loadingNextTrack || loadingPrevTrack || syncing || metaData.name === "No Name") || (!metaData.is_in_sync && keepInSync));

    console.log(`value of syncing is: ${syncing}`);

    const [loginBtnDown, setLoginBtnDown] =  useState(false);

    const loginBtnMouseDownHn = (e) => {
        e.preventDefault();
        setLoginBtnDown(prev=>true);
        document.addEventListener("mouseup", loginBtnMouseUpHn);
    }

    const loginBtnMouseUpHn = () => {
        setLoginBtnDown(prev=>false);
        document.removeEventListener("mouseup", loginBtnMouseUpHn);
    }


    let currPlayerJsx = <> </>;

    switch(currStage){
        case stages.ERR:
            currPlayerJsx =  <>
                {
                        (
                            <ErrorBanner
                                errMsg={!errReported ? "Error! Click here to report! before Reboot": "Reported! Please Refresh."}
                                errReported={errReported}
                                onClickTrigger={
                                        ()=>{
                                            if(!reportingError){
                                                (async ()=>{await sendEmailReport();})();
                                            }
                                        }
                                }
                                reporting={reportingError}
                                isTrack={false}
                            />
                        )
                }
                <div className="player-container">
                            <RebootButton
                                 disabled={!errReported}
                                 setCurrStage={setCurrStage}
                                 setPrevStage={setPrevStage}
                                 prevStage={prevStage}
                                 setErrReported={setErrReported}
                                 isTrack={false}
                            />
                </div>
                </>;
            break;

        case stages.MAIL:
           currPlayerJsx = <>
                <div className="player-container">
                    <EmailInput emailSent={emailSent} setEmailSent={setEmailSent} setCurrStage={setCurrStage}/>
                </div>
            </>;
            break;
        case stages.OTP_UNVERIFIED:
            currPlayerJsx = <>
                <div className="player-container">
                    <OTPinput emailSent={emailSent} setEmailSent={setEmailSent} setCurrStage={setCurrStage}/>
                </div>
                </>;
            break;
        case stages.UNAPPROVED:
            currPlayerJsx = <>
                <div className="player-container">
                    <div className="player-track">
                        <div className="login-prompt">
                            Admin Approval pending, check your mail for Approval. Refresh when approved.
                        </div>
                    </div>
                </div>
                </>;
            break;
        case stages.UNAUTHORIZED:
            currPlayerJsx = <>
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
                                <a href={`https://unmei.space/api/spotify_login_once/authorize?session_id=${sessionId}`}>
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
                </>;
            break;
        case stages.NOT_PREMIUM:
            currPlayerJsx = <>
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
                </>;
            break;
        case stages.ALL_CLEAR:
                currPlayerJsx = <>
                        <div className="player-container">
                            <Player
                                 rendering={rendering}
                                 currStatus={currStatus}
                                 isInActive={isInActive}
                                 isTrackInActive={isTrackInActive}
                                 canSkipPrev={(!newPlaybackActive && metaData.can_skip_prev) || newPlaybackActive}
                                 metaData={metaData}
                                 activeDeviceId={newPlaybackActive ? "This-device" : "Other-device"}
                                 deviceIds={deviceIds}
                                 pauseTrackHandleOldAndNew={pauseTrackHandleOldAndNew}
                                 resumeTrackHandleOldAndNew={resumeTrackHandleOldAndNew}
                                 nextTrack={nextTrack}
                                 setLoadingNextTrack={setLoadingNextTrack}
                                 prevTrack={prevTrack}
                                 setLoadingPrevTrack={setLoadingPrevTrack}
                                 seekTrack={seekTrack}
                            />

                        <div className="player-sep">
                            <hr
                                className="sep"
                            />
                        </div>

                {!isInActive && !isTrackInActive && !hidePlayerControls && !isPodcast &&(
                <PlayerControls
                    disabled={rendering || lockingOut || seeking}
                    currStatus={currStatus}
                    setCurrStatus={setCurrStatus}
                    syncTrack={syncTrack}
                    lockTrack={lockTrack}
                    lockOutTrack={lockOutTrack}
                    setKeepInSync={setKeepInSync}
                />)}

                {isInActive  && !isPodcast && (
                <div className="control-container">
                    <div className="open-spotify-msg">
                        <div className="open-spotify-msg-text">
                            Seems like spotify is offline. May be open the app on your mobile?
                        </div>
                    </div>
                </div>)}

                {isInActive  && isPodcast && (
                <div className="control-container">
                    <div className="open-spotify-msg">
                        <div className="open-spotify-msg-text">
                            Public Spotify APIs do not support podcasts at the moment...
                        </div>
                    </div>
                </div>)}

                </div>
                </>;

    }


    return currPlayerJsx;
}
