import {useState, useEffect, useRef} from 'react';
import play from './static/images/play.png';
import pause from './static/images/pause.png';


export default function PlayButton({disable, pauseHandler, resumeHandler, is_playing}){
    const [isPaused, setIsPaused] = useState(!is_playing);

    useEffect(()=>{
        setIsPaused(!is_playing);
    },[is_playing]);

    function clickHandler(){
       if(isPaused){
           (async ()=>{await resumeHandler();})();
       }else{
           (async ()=>{await pauseHandler();})();
       }
       setIsPaused(prev=>!prev);
    }

    return (
        <div className={disable ? "pause-play-disabled-control": "pause-play"} onClick={()=>{if(!disable){clickHandler();}}}>
                <img src={isPaused ? play : pause}
                 style={{
                    width:"50%",
                    height: "50%",
                    objectFit: "contain",
                    position: 'absolute',
                    top: '50%',
                    left: '50%',
                    transform: `translate(-${!isPaused ? 50 : 40}%, -50%)`,
                     backgroundColor: `${disable ? "#B7AEAE" : "#FFFFFF"}`,
                    opacity: `${disable ? "0.3" : "1"}`
                }}
            /></div>
    )
}
