import {useState, useEffect, useContext} from 'react';
import  {TrackMetaDataContext} from './Contexts/TrackMetaDataContext.jsx';


async function fetchAccessToken(email){
        const params = new URLSearchParams();
        params.append('email', email);
        const resp = await fetch(`http://127.0.0.1:3000/api/fresh_token?${params.toString()}`, {
            method: "GET"
        });
        const token = await resp.json();
        return token.access_token;
}



// SpotifyPlayer needs to depend upon reactive value of context_uri which will be fed from SpotifyTrack ws
// endpoint and do a PUT request to the spotify api every time the context_uri is changed
export default function SpotifyPLayer(){
    const [playerRef, setPlayerRef] = useState(null);
    const [deviceIds, setDeviceIds] = useState([]);
    const [activeDeviceId, setActiveDeviceId] = useState(null);
    const [outOfSync, setOutOfSync] = useState(true);
    const [keepInSync, setKeepInSync] = useState(false);
    const [syncing, setSyncing] = useState(false);
    const [initPlayer, setInitPlayer] = useState(false);
    const [loadingNextTrack, setLoadingNextTrack] = useState(false);
    // currTrackUri is the most updated value of the playerUri
    const currTrackMetaData = useContext(TrackMetaDataContext);

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


    async function syncTrack(track_uri, resource_uri, position_ms, is_playing, disc_number){
        const params = new URLSearchParams();
        params.append('track_uri', track_uri);
        params.append('position', position_ms);
        params.append('email', userEmail); // this is a global defined in thymeleaf "hello-world" templates...
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
        params.append('email', userEmail);
        // TODO: something to return from this request
        setLoadingNextTrack(true);
        const resp = await fetch(`http://127.0.0.1:3000/api/next_track?${params.toString()}`);
        if(!resp.ok){
            throw new Error("fetch to play the next track didn't work");
        }
        setLoadingNextTrack(false);
       // setOutOfSync(true);
    }

    useEffect(() => {
            window.onSpotifyWebPlaybackSDKReady = () => {
                if (!userGrantedPermission) {
                      console.warn("User did not grant permission. Skipping player init.");
                      return;
                }
              const token = accessToken;

              const oAuthRefersh = async (cb) => {
                      const freshToken = await fetchAccessToken(userEmail);
                      cb(freshToken);
              };

            const player = new Spotify.Player({
                name: 'Sync with Atharv -- Playback',
                getOAuthToken: oAuthRefersh,
                volume: 0.8
            });

              // Ready
              const readyCb = async ({device_id}) => {
                    console.log('Ready with Device ID', device_id); //  this is what you use
                    // transferring playback
                    const resp = await fetch('http://127.0.0.1:3000/api/transfer_playback', {
                    method: 'POST',
                    body: JSON.stringify({ device_ids: [device_id], play: true }),
                    headers: {
                      'Content-Type': 'application/json',
                      'X-Token': `${token}`
                      },
                    });
                    const resp_ = await resp.json();
                    console.log(`Transferred playback! response: ${resp_}`);


                };

                player.addListener('ready', readyCb);
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

        useEffect(() => {
            console.log("running that effect");
                          const newOutOfSync = !(currTrackMetaData.track_uri && currTrackMetaData.track_uri === metaData.track_uri && Math.abs(currTrackMetaData.progress_ms-metaData.progress_ms) < 5000 && currTrackMetaData.is_playing === metaData.is_playing);

            if(outOfSync !== newOutOfSync){
                setOutOfSync(prev => newOutOfSync);
            }


        }, [metaData, currTrackMetaData]);


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

            }else{
                playerRef.pause().then(() => {
                      console.log('Paused!');
                });
            }
            console.log("completed the request");
        }
        isIn = false;
        console.log("going out of useEffect");
    }, [outOfSync, keepInSync]);



    const [runAgain, setRunAgain] = useState(false);
    useEffect(()=>{
        if(playerRef){
            //while (!initPlayer){
            //console.log("helele");
            playerRef.getCurrentState().then(state => {
                if(!state){ setRunAgain(prev => !prev); /*console.log("no state!!!");*/ return;}
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
                    console.log("was here setting intiPlayer");
                    if(!initPlayer){setInitPlayer(prev => true);}
            });
        }
    }, [playerRef, runAgain]);

    useEffect(()=>{
        let intervalId = null;
        if(playerRef){
            intervalId = setInterval(()=>{
                playerRef.getCurrentState().then(state=>{
                        if(!state){return;}
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
            if (intervalId){
                clearInterval(intervalId);
            }
        };
    }, [playerRef]);




    const prettyJson = JSON.stringify(metaData, undefined, 2);
//    const shouldSync = !(currTrackMetaData.track_uri && currTrackMetaData.track_uri === metaData.track_uri && Math.abs(currTrackMetaData.progress_ms-metaData.progress_ms) < 2000  && currTrackMetaData.is_playing === metaData.is_playing);
    return (!initPlayer ? (<p>loading player...</p>) : (loadingNextTrack ? <p>loading next track...</p>: <>
        <p> Playing on your device:</p> <pre>{prettyJson}</pre>
        {syncing ? <p> syncing...</p> : (outOfSync && !keepInSync &&
            <button onClick={() => {syncTrack(currTrackMetaData.track_uri, currTrackMetaData.resource_uri, currTrackMetaData.progress_ms, currTrackMetaData.is_playing, currTrackMetaData.disc_number); }}>
            Sync In!
        </button>)
        }
        {!keepInSync && <button onClick={() => nextTrack()}>Next track</button>}
        {!keepInSync ? <button onClick={() => setKeepInSync(true)}>Keep in Sync!</button>:
            <button onClick={() => setKeepInSync(false)}>Out of Sync</button>}

    </>)
    )
}
