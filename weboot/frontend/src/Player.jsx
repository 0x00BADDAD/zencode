import {useState, useEffect, useRef} from 'react';
import PlayButton from './PlayButton.jsx';
import LoadingBanner from './LoadingBanner.jsx';
import ScrollingBanner from './ScrollingBanner.jsx';
import DevicePane from './DevicePane.jsx';
import Slider from './Slider.jsx';
import next from './static/images/next.png';
import record_img from './static/images/record_img.png';


export default function Player({
                                rendering,
                                currStatus,
                                isInActive,
                                isTrackInActive,
                                canSkipPrev,
                                metaData,
                                activeDeviceId,
                                deviceIds,
                                pauseTrackHandleOldAndNew,
                                resumeTrackHandleOldAndNew,
                                nextTrack,
                                setLoadingNextTrack,
                                prevTrack,
                                setLoadingPrevTrack,
                                transferOldPlayback,
                                transferNewPlayback,
                                seekTrack
                              }){
    const [loadingCoverPic, setLoadingCoverPic] = useState(false);
    //const [showLoadingBanner, setShowLoadingBanner] = useState(true);
    //setTimeout(()=>{setShowLoadingBanner(prev => false);}, 2000);
    const ifCurrStatusIsLockedIn = !!(currStatus === 2);
    console.log(`---->value of song name: ${metaData.name}`);
    const perCent = ((metaData.progress_ms || 0) / metaData.duration_ms) * 100;
    const elapsedTimeSec = Math.ceil(metaData.progress_ms/1000);
    const totalTimeSec = Math.ceil(metaData.duration_ms/1000);

    return (rendering || !!(metaData.loadingNext)) ? (<LoadingBanner track={false} showReboot={false}/>) : (
        <div className="player-track">
            {loadingCoverPic ? (<div className="player-loading-cover-pic"></div>) :
                    (<div className="player-cover-pic"><img src={!isInActive ? metaData.img_url : record_img} onLoadStart={()=>setLoadingCoverPic(prev=>true)} onLoad={()=>setLoadingCoverPic(prev=>false)}/></div>)
            }
            <div className="player-song-info">
                <ScrollingBanner track={false} songName={metaData.name}/>
                <div className="player-artist-name">{metaData.artists.reduce((acc, currArtist)=>{ if(acc){ return acc + ", " + currArtist;}else{ return currArtist}}, "")}</div>
            </div>
            {/*!isInActive && <DevicePane deviceIds={deviceIds} activeDeviceId={activeDeviceId} transferNew={transferNewPlayback} transferOld={transferOldPlayback}/>*/}
            <PlayButton track={false} disable={ifCurrStatusIsLockedIn || isInActive} pauseHandler={pauseTrackHandleOldAndNew} resumeHandler={resumeTrackHandleOldAndNew} is_playing={metaData.is_playing} isInActive={isInActive}/>


            <div className="player-next-track"
                style={{
                    opacity: `${(ifCurrStatusIsLockedIn && !isTrackInActive) || isInActive? "0.3" : "1"}`
                }}
                onClick={()=>{

                if(currStatus!==2 && !isInActive){
                    setLoadingNextTrack(prev=>true);
                    (async ()=>{
                        //loadingNextTrackRef.current = true;
                        await nextTrack();
                        //setLoadingNextTrack(prev=>false);
                        //loadingNextTrackRef.current = false;
                    })()
                }

                }}>
                <img src={next}/>
            </div>
            <div className="player-prev-track"
                style={{
                    opacity: `${(ifCurrStatusIsLockedIn&&!isTrackInActive) || isInActive || !canSkipPrev? "0.3" : "1"}`
                }}
                 onClick={()=>{
                    if(currStatus!==2 && !isInActive && canSkipPrev){
                        setLoadingPrevTrack(prev=>true);
                        (async ()=>{
                            await prevTrack();
                        })();
                    }
                }}>
                <img src={next} style={{transform: "rotate(180deg)"}}/>
            </div>

            <Slider elapsedTime={elapsedTimeSec} track={false} totalTime={totalTimeSec} isTrack={ifCurrStatusIsLockedIn || isInActive} seekTrack={seekTrack} isInActive={isInActive}/>
            { /*<div className="timeline"></div>*/}
        </div>
    );

}
