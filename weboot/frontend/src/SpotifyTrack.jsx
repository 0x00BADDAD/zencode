import {useState, useEffect, useContext} from 'react';
import {TrackMetaDataContext, TrackMetaDataDispatchContext} from './Contexts/TrackMetaDataContext.jsx';
import {DisableWebSocketContext} from './Contexts/DisableWebSocketContext.jsx';
import {WsRefDispatchContext} from './Contexts/WsRefContext.jsx';
import Slider from './Slider.jsx';
import DevicePane from './DevicePane.jsx';
import ScrollingBanner from './ScrollingBanner.jsx';
import LoadingBanner from './LoadingBanner.jsx';
import starboy from './static/images/starboy.png';
import play from './static/images/play.png';
import pause from './static/images/pause.png';
import next from './static/images/next.png';


export default function SpotifyTrack() {
    //const [currTrackUri, setCurrTrackUri] = useState('');

    const trackMetaDataDispatch = useContext(TrackMetaDataDispatchContext);
    const disableWebSocket = useContext(DisableWebSocketContext);
    //const trackMetaData = useContext(TrackMetaDataContext);
    const [metaData, setMetaData] = useState({});
    const wsRefDispatch = useContext(WsRefDispatchContext);

    useEffect(()=>{
        let ws = null;
        if(!disableWebSocket){
             // Connect to WebSocket server
                ws = new WebSocket("ws://127.0.0.1:3000/ws1");
                //setSocket(ws);

                // When message is received
                ws.onmessage = (event) => {
                  try {
                    const data = JSON.parse(event.data); // if message is JSON
                    console.log(`data is: ${event}`);
                    const {name, track_uri, resource_uri, artists , progress_ms, duration_ms, is_playing, disc_number, atharv_track, img_url}= data;
                      if(atharv_track){
                            const newMetaData = {
                                name: name,
                                artists: artists,
                                track_uri: track_uri,
                                progress_ms: progress_ms,
                                duration_ms: duration_ms,
                                is_playing: is_playing,
                                disc_number: disc_number,
                                resource_uri: resource_uri,
                                img_url: img_url
                            };

                            if (!metaData || resource_uri !== metaData.resource_uri || progress_ms !== metaData.progress_ms || is_playing !== metaData.is_playing){
                                setMetaData(prev => newMetaData);
                               // setCurrTrackUri(prev => track_uri);
                                trackMetaDataDispatch({
                                    type: "update",
                                    ...newMetaData
                                });
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
            wsRefDispatch({
                type: 'enabled',
                wsRef: ws
            });
        }

            // Cleanup on component unmount
            return () => {
                if(!disableWebSocket){
                    wsRefDispatch({
                        type: 'enabled',
                        wsRef: null
                    });
                    ws.close();
                }
            };
    },[disableWebSocket]);

    //const [showLoadingBanner, setShowLoadingBanner] = useState(true);
    //setTimeout(()=>{setShowLoadingBanner(prev => false);}, 2000);

    //const prettyJson = JSON.stringify(metaData, undefined, 2);
    //const deviceIds = ["This-device-1", "This-device-2", "This-device-3"];
    const showLoadingBanner = Object.keys(metaData).length === 0;
    return showLoadingBanner ? (<LoadingBanner track={true}/>) : (
        <div className="track">
            <div className="cover-pic"><img src={metaData.img_url}/></div>
            <div className="song-info">
                <ScrollingBanner songName={metaData.name}/>
                <div className="artist-name">{metaData.artists.reduce((acc, currArtist)=>{ if(acc){ return acc + ", " + currArtist;}else{ return currArtist}}, "")}</div>
            </div>
            {/*<DevicePane deviceIds={deviceIds}/>*/}
            <div className="pause-play"
            style={{
                backgroundColor: "#B7AEAE"
                }}>
                <img src={metaData.is_playing ? pause: play}
                 style={{
                    width:"50%",
                    height: "50%",
                    objectFit: "contain",
                    position: 'absolute',
                    top: '50%',
                    left: '50%',
                    transform: `translate(-${metaData.is_playing ? 50 : 40}%, -50%)`,
                     backgroundColor: "#B7AEAE",
                     opacity: "0.3"

                }}
            /></div>
            <div className="next-track" style={{opacity: "0.3"}}><img src={next}/></div>
            <div className="prev-track" style={{opacity: "0.3"}}><img src={next} style={{transform: "rotate(180deg)"}}/></div>
            <Slider elapsedTime={metaData.progress_ms} totalTime={metaData.duration_ms} isTrack={true}/>
            { /*<div className="timeline"></div>*/}
        </div>
    );

    //return (
    //    <>
    //        <p>Atharv's device:</p>
    //        {Object.keys(metaData).length !== 0? (<pre>{prettyJson}</pre>) : (<p> loading track...</p>)}
    //    </>
    //);
}
