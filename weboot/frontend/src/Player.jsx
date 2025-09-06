import {useState, useEffect, useRef} from 'react';
import PlayButton from './PlayButton.jsx';
import LoadingBanner from './LoadingBanner.jsx';
import ScrollingBanner from './ScrollingBanner.jsx';
import DevicePane from './DevicePane.jsx';
import Slider from './Slider.jsx';
import next from './static/images/next.png';


export default function Player({
                                rendering,
                                currStatus,
                                metaData,
                                activeDeviceId,
                                deviceIds,
                                pauseTrackHandleOldAndNew,
                                resumeTrackHandleOldAndNew,
                                nextTrack,
                                setLoadingNextTrack,
                                loadingNextTrackRef,
                                prevTrack,
                                transferOldPlayback,
                                transferNewPlayback,
                                seekTrack
                              }){

    //const [showLoadingBanner, setShowLoadingBanner] = useState(true);
    //setTimeout(()=>{setShowLoadingBanner(prev => false);}, 2000);
    const ifCurrStatusIsLockedIn = !!(currStatus === 2);
    console.log(`---->value of song name: ${metaData.name}`);
    return (rendering || !!(metaData.loadingNext)) ? (<LoadingBanner track={false}/>) : (
        <div className="player-track">
            <div className="cover-pic"><img src={metaData.img_url}/></div>
            <div className="song-info">
                <ScrollingBanner songName={metaData.name}/>
                <div className="artist-name">{metaData.artists.reduce((acc, currArtist)=>{ if(acc){ return acc + ", " + currArtist;}else{ return currArtist}}, "")}</div>
            </div>
            <DevicePane deviceIds={deviceIds} activeDeviceId={activeDeviceId} transferNew={transferNewPlayback} transferOld={transferOldPlayback}/>
            <PlayButton disable={ifCurrStatusIsLockedIn} pauseHandler={pauseTrackHandleOldAndNew} resumeHandler={resumeTrackHandleOldAndNew} is_playing={metaData.is_playing}/>


            <div className="next-track"
                style={{
                    opacity: `${ifCurrStatusIsLockedIn ? "0.3" : "1"}`
                }}
                onClick={()=>{

                if(currStatus!==2){
                    (async ()=>{
                        //setLoadingNextTrack(prev=>true);
                        //loadingNextTrackRef.current = true;
                        await nextTrack();
                        //setLoadingNextTrack(prev=>false);
                        //loadingNextTrackRef.current = false;
                    })()
                }

                }}>
                <img src={next}/>
            </div>
            <div className="prev-track"
                style={{
                    opacity: `${ifCurrStatusIsLockedIn ? "0.3" : "1"}`
                }}
                 onClick={()=>{if(currStatus!==2){(async ()=>{await prevTrack();})()}}}>
                <img src={next} style={{transform: "rotate(180deg)"}}/>
            </div>



            <Slider elapsedTime={metaData.progress_ms} totalTime={metaData.duration_ms} isTrack={ifCurrStatusIsLockedIn} seekTrack={seekTrack}/>
            { /*<div className="timeline"></div>*/}
        </div>
    );

}
