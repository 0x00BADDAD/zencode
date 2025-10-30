import {useState, useEffect, useContext} from 'react';
import {TrackMetaDataContext, TrackMetaDataDispatchContext} from './Contexts/TrackMetaDataContext.jsx';
import {DisableWebSocketContext} from './Contexts/DisableWebSocketContext.jsx';
import {WsRefDispatchContext} from './Contexts/WsRefContext.jsx';
import Slider from './Slider.jsx';
import DevicePane from './DevicePane.jsx';
import ScrollingBanner from './ScrollingBanner.jsx';
import LoadingBanner from './LoadingBanner.jsx';
import RebootButton from './RebootButton.jsx';
import ErrorBanner from './ErrorBanner.jsx';
import starboy from './static/images/starboy.png';
import play from './static/images/play.png';
import pause from './static/images/pause.png';
import next from './static/images/next.png';
import record_img from './static/images/record_img.png';
import {base_url, base_ws_url} from './url.js';

export default function SpotifyTrack() {
    //const [currTrackUri, setCurrTrackUri] = useState('');

    const trackMetaDataDispatch = useContext(TrackMetaDataDispatchContext);
    const disableWebSocket = useContext(DisableWebSocketContext);
    //const trackMetaData = useContext(TrackMetaDataContext);
    const [metaData, setMetaData] = useState({});
    const wsRefDispatch = useContext(WsRefDispatchContext);

    const [currErrMsgToSend, setCurrErrMsgToSend] = useState("initial err message");
    const [errContentToSend, setErrContentToSend] = useState({API_NAME: "/api/some_api", stacktrace: "some huge JVM trace"});
    const [hideError, setHideError] = useState(true);
    const [rebootDisable, setRebootDisable] = useState(true);
    const [errReported, setErrReported] = useState(false);
    const [reportingError, setReportingError] = useState(false);

    useEffect(()=>{
        let ws = null;
        if(!disableWebSocket){
             // Connect to WebSocket server
                ws = new WebSocket(`${base_ws_url}`);
                //setSocket(ws);

                // When message is received
                ws.onmessage = (event) => {
                  try {
                    const data = JSON.parse(event.data); // if message is JSON
                    console.log(`data is: ${event}`);
                    const {name, track_uri, resource_uri, artists , progress_ms, duration_ms, is_playing, disc_number, atharv_track, img_url, err_found, err_found_stack_trace} = data;
                      if(atharv_track && err_found){
                          const errContent = {
                              API_NAME: "ERR in SpotifyTrack.jsx, fetching from spotify backend!",
                              stacktrace: err_found_stack_trace
                          };

                          const errMsgToSend = `ERR in websocket scheduled task (backend)`;
                          setCurrErrMsgToSend(prev=>errMsgToSend);
                          setErrContentToSend(prev=>errContent);
                          setHideError(prev=>false);
                          return;
                      }
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
                                img_url: img_url,
                                err_found: err_found,
                                err_found_stack_trace: err_found_stack_trace
                            };
                            console.log(`progres_ms: ${progress_ms} and duration_ms: ${duration_ms}`);
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
                      //const err = {
                      //    'error' : "something went wrong on parsing the received message"
                      //};
                      //setMetaData(prev => err); // plain text fallback
                    const errContent = {
                        API_NAME: "ERR in SpotifyTrack.jsx, while parsing recd from ws JSON msg!",
                        stacktrace: "Err while parsing json response in Atharv track playback web socket!"
                    };

                    const errMsgToSend = `ERR in useEffect() while parsing recd ws JSON msg in SpotifyTrack.jsx [447]: res status: ${resp.status}:${resp.statusText}`;
                    setCurrErrMsgToSend(prev=>errMsgToSend);
                    setErrContentToSend(prev=>errContent);
                    setHideError(prev=>false);
                  }
                };

                ws.onerror = (error) => {
                  console.error("WebSocket error:", error);
                };

                ws.onclose = () => {
                  console.log("WebSocket connection closed");
                };
           // wsRefDispatch({
           //     type: 'enabled',
           //     wsRef: ws
           // });
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

    //const [showLoadingBanner, setShowLoadingBanner] = useState(true);
    //setTimeout(()=>{setShowLoadingBanner(prev => false);}, 2000);

    //const prettyJson = JSON.stringify(metaData, undefined, 2);
    //const deviceIds = ["This-device-1", "This-device-2", "This-device-3"];

    async function sendEmailReport(){
        setReportingError(prev=>true);

        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        // TODO: something to return from this request

        const formData = new FormData();
        formData.append("errMsg", currErrMsgToSend);
        formData.append("errContentApiName", errContentToSend.API_NAME);
        formData.append("errContentStackTrace", errContentToSend.stacktrace);

        const resp = await fetch(`${base_url}/api/send_err_report?${params.toString()}`, {
            method: 'POST',
            body: formData
        });
        if(!resp.ok){
                    //throw new Error("fetch to sending error report didn't work");
              const errContent = await resp.json();
              const errMsgToSend = `ERR in sendEmailReport() in SpotifyPlayer.jsx [762]: res status: ${resp.status}:${resp.statusText}`;
              setErrContentToSend(prev=>errContent);
              setCurrErrMsgToSend(prev=>errMsgToSend);
              setHideError(prev=>false);
              setErrReported(prev=>false);
              setReportingError(prev=>false);
              return;
        }
        setReportingError(prev=>false);
        setErrReported(prev=>true);
    }


    const [loadingCoverPic, setLoadingCoverPic] = useState(false);
    const showLoadingBanner = Object.keys(metaData).length === 0;
    const isInActive = metaData.name==="No music playing right now!" || metaData.name==="It seems Atharv is listening to a podcast!";
    const perCent = ((metaData.progress_ms || 0) / metaData.duration_ms) * 100;
    const elapsedTimeSec = Math.ceil(metaData.progress_ms/1000);
    const totalTimeSec = Math.ceil(metaData.duration_ms/1000);
    return (
        <>
            {
                !hideError &&
                    (
                        <ErrorBanner
                            errMsg={!errReported ? "Error! Click here to report! before Reboot": "Reported! Please Refresh."}
                            onClickTrigger={
                                    ()=>{
                                        if(!reportingError){
                                            (async ()=>{await sendEmailReport();})();
                                        }
                                    }
                            }
                            errReported={errReported}
                            reporting={reportingError}
                            isTrack={true}
                        />
                    )
            }




            { !hideError ?
                    (<LoadingBanner track={true} showReboot={true} errReported={errReported} setHideError={setHideError} setErrReported={setErrReported}/>)
            :
                (showLoadingBanner ? (<LoadingBanner track={true} showReboot={false}/>) : (
        <div className="track">
            {loadingCoverPic ? (<div className="loading-cover-pic"></div>) :
                    (<div className="cover-pic"><img src={!isInActive ? metaData.img_url : record_img} onLoadStart={()=>setLoadingCoverPic(prev=>true)} onLoad={()=>setLoadingCoverPic(prev=>false)}/></div>)
            }
            <div className="song-info">
                <ScrollingBanner songName={metaData.name}/>
                <div className="artist-name">{metaData.artists.reduce((acc, currArtist)=>{ if(acc){ return acc + ", " + currArtist;}else{ return currArtist}}, "")}</div>
            </div>
            {/*<DevicePane deviceIds={deviceIds}/>*/}
            <div className="pause-play"
            style={{
                backgroundColor: "#B7AEAE",
                border: "2px solid #B7AEAE"
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
            <Slider elapsedTime={elapsedTimeSec} totalTime={totalTimeSec} perCent={Math.ceil(perCent)} isTrack={true} isInActive={isInActive}/>
            { /*<div className="timeline"></div>*/}
        </div>
    )) }

    </>
    );

}
