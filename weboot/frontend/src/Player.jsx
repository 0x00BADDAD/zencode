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
    return (rendering || !!(metaData.loadingNext)) ? (<LoadingBanner track={false}/>) : (
        <div className="player-track">
            {loadingCoverPic ? (<div className="loading-cover-pic"></div>) :
                    (<div className="cover-pic"><img src={!isInActive ? metaData.img_url : record_img} onLoadStart={()=>setLoadingCoverPic(prev=>true)} onLoad={()=>setLoadingCoverPic(prev=>false)}/></div>)
            }
            <div className="song-info">
                <ScrollingBanner songName={metaData.name}/>
                <div className="artist-name">{metaData.artists.reduce((acc, currArtist)=>{ if(acc){ return acc + ", " + currArtist;}else{ return currArtist}}, "")}</div>
            </div>
            {!isInActive && <DevicePane deviceIds={deviceIds} activeDeviceId={activeDeviceId} transferNew={transferNewPlayback} transferOld={transferOldPlayback}/>}
            <PlayButton disable={ifCurrStatusIsLockedIn || isInActive} pauseHandler={pauseTrackHandleOldAndNew} resumeHandler={resumeTrackHandleOldAndNew} is_playing={metaData.is_playing} isInActive={isInActive}/>


            <div className="next-track"
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
            <div className="prev-track"
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



            <Slider elapsedTime={metaData.progress_ms} totalTime={metaData.duration_ms} isTrack={ifCurrStatusIsLockedIn || isInActive} seekTrack={seekTrack} isInActive={isInActive}/>
            { /*<div className="timeline"></div>*/}
        </div>
    );

}
