import {useState, useEffect, useContext, useRef} from 'react';
import {TrackMetaDataContext} from './Contexts/TrackMetaDataContext.jsx';
import {WsRefContext} from './Contexts/WsRefContext.jsx';
import {DisableWebSocketContext} from './Contexts/DisableWebSocketContext.jsx';
import {DisableWebSocketDispatchContext} from './Contexts/DisableWebSocketContext.jsx';

async function fetchAccessToken(sessionId){
        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        const resp = await fetch(`http://127.0.0.1:3000/api/fresh_token?${params.toString()}`, {
            method: "GET"
        });
        const token = await resp.json();
        return token.access_token;
}

    //function waitForOpen(ws) {
    //    return new Promise((resolve) => {
    //        if (ws.readyState === WebSocket.OPEN) {
    //            resolve();
    //        } else {
    //            ws.addEventListener("open", () => resolve());
    //        }
    //    });
    //}

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
    const [transferringPlayback, setTransferringPlayback] = useState(false);
    // currTrackMetaData is the most updated value of the track meta data right from the
    // spotify backend but with an extra websocket hop in between.
    const currTrackMetaData = useContext(TrackMetaDataContext);
    const disableWebSocket = useContext(DisableWebSocketContext);
    const disableWebSocketDispatch = useContext(DisableWebSocketDispatchContext);
    const wsRef = useContext(WsRefContext);

    const oldPlaybackId = useRef(null);
    const newPlaybackId = useRef(null);

    const [newPlaybackActive, setNewPlaybackActive] = useState(false);
    //const [playbackTransferred, setPlaybackTransferred] = useState(false);


    const initialMetaData = {
       track_uri: "No uri",
       progress_ms: 0,
       name: "No Name",
       is_playing: false,
       artists: []
    };

    const [metaData, setMetaData] = useState(initialMetaData);

    //const [playerUri, setPlayerUri] = useState(currTrackMetaData.track_uri);
    // const [playerUri, setPlayerUri] = useState('');

    function syncTrack2(){
        if(!wsRef){console.error("wsRef uninitialized... something went wrong"); return;}

        const msg = {
            email: userEmail,
        };
        setSyncing(true);
        wsRef.send(JSON.stringify(msg));
        setSyncing(false);
    }



    async function transferPlayback(device_id){
        setTransferringPlayback(true);
            console.log('transferring playback to Device ID ', device_id); //  this is what you use
            // transferring playback
            const resp = await fetch('http://127.0.0.1:3000/api/transfer_playback', {
            method: 'POST',
            body: JSON.stringify({ device_ids: [device_id], play: true }),
            headers: {
              'Content-Type': 'application/json'
              },
            });
            const resp_ = await resp.json();
        setTransferringPlayback(false);
            console.log(`Transferred playback! response: ${resp_}`);
    }



    async function syncTrack(track_uri, resource_uri, position_ms, is_playing, disc_number){
        const params = new URLSearchParams();
        params.append('track_uri', track_uri);
        params.append('position', position_ms);
        params.append('session_id', sessionId); // this is a global defined in thymeleaf "hello-world" templates...
        params.append('is_playing', is_playing);
        params.append('disc_number', disc_number);
        params.append('resource_uri', resource_uri);
        setSyncing(true);
        const resp = await fetch(`http://127.0.0.1:3000/api/play_track?${params.toString()}`);
        // only after the above fetch has been done
        if (!resp.ok){
            throw new Error("first fetch to play a new track failed!");
        }
        setSyncing(false);

        // TODO: check if these setStates are batched or not?

        //setMetaData(prev => currTrackMetaData);
    }

    async function nextTrack(){
        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        // TODO: something to return from this request
        setLoadingNextTrack(true);
        const resp = await fetch(`http://127.0.0.1:3000/api/next_track?${params.toString()}`);
        if(!resp.ok){
            throw new Error("fetch to play the next track didn't work");
        }
        setLoadingNextTrack(false);
       // setOutOfSync(true);
    }

    async function pauseTrack(){
        // this is to pause the track in the old playback of the device via spotify web api
        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        const resp = await fetch(`http://127.0.0.1:3000/api/pause_track?${params.toString()}`);
        if(!resp.ok){
            throw new Error("fetch to play the next track didn't work");
        }
    }

    //useEffect(() =>{
    //    const ws =  new WebSocket("ws://127.0.0.1:3000/ws1");
    //    await waitForOpen(ws);
    //    // I want to send the message to the backend every time I want to sync tracks
    //    setWsRef(prev => ws);
    //}, []);

    useEffect(() => {
            window.onSpotifyWebPlaybackSDKReady = () => {
                if (!userGrantedPermission) {
                      console.warn("User did not grant permission. Skipping player init.");
                      return;
                }
              const token = accessToken;

              const oAuthRefresh = async (cb) => {
                      const freshToken = await fetchAccessToken(sessionId);
                      cb(freshToken);
              };

            const player = new Spotify.Player({
                name: 'Sync with Atharv -- Playback',
                getOAuthToken: oAuthRefresh,
                volume: 0.8
            });

              // Ready
              //const readyCb = async ({device_id}) => {
              //      console.log('Ready with Device ID', device_id); //  this is what you use
              //      // transferring playback
              //      const resp = await fetch('http://127.0.0.1:3000/api/transfer_playback', {
              //      method: 'POST',
              //      body: JSON.stringify({ device_ids: [device_id], play: true }),
              //      headers: {
              //        'Content-Type': 'application/json',
              //        'X-Token': `${token}`
              //        },
              //      });
              //      const resp_ = await resp.json();
              //      console.log(`Transferred playback! response: ${resp_}`);
              //};
                const readyCb2 = ({device_id}) => {
                    newPlaybackId.current = device_id;
                    console.log(`New playback for this device has been setup with id: ${device_id}`);
                };

                player.addListener('ready', readyCb2);
                // Not Ready
                player.addListener('not_ready', ({ device_id }) => {
                    console.log('Device ID has gone offline', device_id);
                });

                player.addListener('initialization_error', ({ message }) => {
                    console.error(message);
                });

                player.addListener('authentication_error', ({ message }) => {
                    console.error(message);
                });

                player.addListener('account_error', ({ message }) => {
                    console.error(message);
                });


                player.connect().then(success => {
                    if (!success){
                        console.error("player didn't really connect... something went wrong!");
                        return;
                    }else{
                        console.log(`Player connected!! let's gooo!`);
                    }
                    setPlayerRef(prev => player);
                });
            }

            // load the external spotify script after defining the SDK callback as done above
            const script = document.createElement('script');
            script.src = 'https://sdk.scdn.co/spotify-player.js';
            script.async = true;
            script.onload = () => {
              console.log('Script loaded successfully!');
            };
            script.onerror = (error) => {
              console.error('Error loading script:', error);
            };
            document.body.appendChild(script); // this starts downloading the script


    }, []);

    // web socket that will receive messages from the backend for the metaData for old playback
    useEffect(()=>{
        let ws = null;
        if(!disableWebSocket){
             // Connect to WebSocket server
                ws = new WebSocket(`ws://127.0.0.1:3000/ws1?session_id=${sessionId}`);
                //setSocket(ws);
                // When message is received
                ws.onmessage = (event) => {
                  try {
                    const data = JSON.parse(event.data); // if message is JSON
                    console.log(`data recd in player socket is: ${data}`);
                    const {name, track_uri, resource_uri, artists , progress_ms, is_playing, disc_number, atharv_track, device_id}= data;


                      if(!atharv_track && !newPlaybackActive){

                          if(oldPlaybackId.current == null){
                                oldPlaybackId.current = device_id;
                          }
                          console.log(`value of deviceId for the remote device is: ${device_id}`);
                            //const newMetaData = {
                            //    name: name,
                            //    artists: artists,
                            //    track_uri: track_uri,
                            //    progress_ms: progress_ms,
                            //    is_playing: is_playing,
                            //    disc_number: disc_number,
                            //    resource_uri: resource_uri
                            //};


                           const newMetaData = {
                               track_uri: track_uri,
                               progress_ms: progress_ms,
                               name: name,
                               is_playing: is_playing,
                               artists: artists
                           };


                            if (!metaData || track_uri !== metaData.track_uri || progress_ms !== metaData.progress_ms || is_playing !== metaData.is_playing){
                                setMetaData(prev => newMetaData);
                               // setCurrTrackUri(prev => track_uri);
                               // trackMetaDataDispatch({
                               //     type: "update",
                               //     ...newMetaData
                               // });
                            }
                      }
                  } catch (e) {
                      const err = {
                          'error' : "something went wrong on parsing the received message"
                      };
                      setMetaData(prev => err); // plain text fallback
                  }
                };

                ws.onerror = (error) => {
                  console.error("WebSocket error:", error);
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
                if(!disableWebSocket){
                   // wsRefDispatch({
                   //     type: 'enabled',
                   //     wsRef: null
                   // });
                   ws.close();
                }
            };
    },[disableWebSocket]);




        const [disableSyncUpdates, setDisableSyncUpdates] = useState(false);
        useEffect(() => {
            if(disableSyncUpdates){
                console.log("disabled the sync updates");
                return;
            }
            console.log("running that effect");
            const newOutOfSync = !(currTrackMetaData.track_uri && currTrackMetaData.track_uri === metaData.track_uri && Math.abs(currTrackMetaData.progress_ms-metaData.progress_ms) < 5000 && currTrackMetaData.is_playing === metaData.is_playing);

            if(outOfSync !== newOutOfSync){
                setOutOfSync(prev => newOutOfSync);
            }


        }, [disableSyncUpdates, metaData, currTrackMetaData]);



    let isIn = false;
    useEffect(()=>{
        if(isIn){return;}
        isIn = true;
        console.log("is in the useEffect");

        if(outOfSync && keepInSync){
                          const onlyPaused = (currTrackMetaData.track_uri && currTrackMetaData.track_uri === metaData.track_uri && Math.abs(currTrackMetaData.progress_ms-metaData.progress_ms) < 5000) && currTrackMetaData.is_playing !== metaData.is_playing;
            if (!onlyPaused || currTrackMetaData.is_playing){
                                (async ()=>{
                                await syncTrack(currTrackMetaData.track_uri, currTrackMetaData.resource_uri, currTrackMetaData.progress_ms, currTrackMetaData.is_playing, currTrackMetaData.disc_number);
                               })();
                //syncTrack2();

            }else{
                if(newPlaybackActive){
                    playerRef.pause().then(() => {
                          console.log('Paused!');
                    });
                }else{
                    (async ()=>{await pauseTrack();})();
                }
            }
            console.log("completed the request");
        }
        isIn = false;
        console.log("going out of useEffect");
    }, [outOfSync, keepInSync]);



    const [runAgain, setRunAgain] = useState(false);
    useEffect(()=>{
        if(playerRef && newPlaybackActive){
            //while (!initPlayer){
            //console.log("helele");
            playerRef.getCurrentState().then(state => {
                if(!state){ setRunAgain(prev => !prev); console.log("no state while initing player state!!!"); return ()=>{};}
                   const newMetaData = {
                       track_uri: state.context.uri,
                       progress_ms: state.position,
                       name: state.track_window.current_track.name,
                       is_playing: !state.paused,
                       artists: state.track_window.current_track.artists.map(artist => {
                           return artist.name;
                       })
                   };
                    console.log("setting metadata");
                    setMetaData(prev => newMetaData);
                    console.log("was here setting initPlayer");
                    if(!initPlayer){setInitPlayer(prev => true);}
            });
        }
        return ()=>{};
    }, [playerRef, runAgain, newPlaybackActive]);



    const freezeLimit = useRef(0);
    const intervalId = useRef(null);
    const [disableMetaDataEffect, setDisableMetaDataEffect] = useState(false);
    useEffect(()=>{
        //let intervalId = null;
        if(disableMetaDataEffect){
                if(intervalId.current){
                    clearInterval(intervalId.current);
                }else{
                    console.log("tried to clear intervalId even before it was set.");
                }
                return;
        }

        if(!newPlaybackActive){
            if(intervalId.current){
                clearInterval(intervalId.cuurent);
            }
           // return;
        }

        if(playerRef && !disableMetaDataEffect && newPlaybackActive && initPlayer){
            intervalId.current = setInterval(()=>{
                playerRef.getCurrentState().then(state=>{
                    if(!state){
                        if(freezeLimit.current > 10){
                            setDisableSyncUpdates(true);
                            setDisableMetaDataEffect(true);
                            disableWebSocketDispatch({
                                type: "disable",
                                disable: true
                            });

                        }else{
                            freezeLimit.current++;
                        }
                        console.log("No state found returning...");
                        return;
                    }

                           const newMetaData = {
                               track_uri: state.context.uri,
                               progress_ms: state.position,
                               name: state.track_window.current_track.name,
                               is_playing: !state.paused,
                               artists: state.track_window.current_track.artists.map(artist => {
                                   return artist.name;
                               })
                           };
                            setMetaData(prev => newMetaData);
                });
            }, 1500);
        }

        return () =>{
            if (intervalId.current){
                clearInterval(intervalId.current);
            }
        };
    }, [playerRef, disableMetaDataEffect, newPlaybackActive]);

    async function transferNewPlayback(){
        await transferPlayback(newPlaybackId.current);
        setNewPlaybackActive(true);
    }

    async function transferOldPlayback(){
        setNewPlaybackActive(false);
        setInitPlayer(false);
        await transferPlayback(oldPlaybackId.current);
    }


    const prettyJson = JSON.stringify(metaData, undefined, 2);

    return (newPlaybackActive ? (!initPlayer ? (<p>loading player...</p>) : (loadingNextTrack ? <p>loading next track...</p>: <>
        <p> Playing on your device:</p> <pre>{prettyJson}</pre>
        {syncing ? <p> syncing...</p> : (outOfSync && !keepInSync &&
        <button onClick={() => {/*syncTrack2();*/ syncTrack(currTrackMetaData.track_uri, currTrackMetaData.resource_uri, currTrackMetaData.progress_ms, currTrackMetaData.is_playing, currTrackMetaData.disc_number); }}>
            Sync In!
        </button>)
        }
        {!keepInSync && <button onClick={() => nextTrack()}>Next track</button>}
        {!keepInSync ? <button onClick={() => setKeepInSync(true)}>Keep in Sync!</button>:
            <button onClick={() => setKeepInSync(false)}>Out of Sync</button>}
        {!transferringPlayback ? <button onClick={() => transferOldPlayback()}> Transfer to old playback</button>:
            <p> Transferring...</p>}

    </>)
    ): (
        oldPlaybackId.current?
        (loadingNextTrack ? <p>loading next track...</p>: <>
                <p> Playing on your device:</p> <pre>{prettyJson}</pre>
                {syncing ? <p> syncing...</p> : (outOfSync && !keepInSync &&
                <button onClick={() => {/*syncTrack2();*/ syncTrack(currTrackMetaData.track_uri, currTrackMetaData.resource_uri, currTrackMetaData.progress_ms, currTrackMetaData.is_playing, currTrackMetaData.disc_number); }}>
                    Sync In!
                </button>)
                }
                {!keepInSync && <button onClick={() => nextTrack()}>Next track</button>}
                {!keepInSync ? <button onClick={() => setKeepInSync(true)}>Keep in Sync!</button>:
                    <button onClick={() => setKeepInSync(false)}>Out of Sync</button>}
                {!transferringPlayback ? <button onClick={() => transferNewPlayback()}> Transfer to new playback</button>:
                    <p> Transferring...</p>}

        </>): (
            <p>It seems your device is offline...</p>
        )
    )
)
}
