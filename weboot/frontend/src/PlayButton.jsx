import {useState, useEffect, useRef} from 'react';
import play from './static/images/play.png';
import pause from './static/images/pause.png';


export default function PlayButton({disable, pauseHandler, resumeHandler, is_playing, isInActive}){
    const [isPaused, setIsPaused] = useState(null);
    const [wasClicked, setWasClicked] = useState(false);
    console.log(`is_playing is: ${is_playing}`);
    useEffect(()=>{
        setIsPaused(prev=>!is_playing);
    },[]);

    //if(disable && is_playing !== !isPaused){
    //    setIsPaused(prev=>!is_playing);
    //}
    const is_playing_ = is_playing;
    useEffect(()=>{
        //if(disable){
            // playbtn is in lockedin state hence should update the isPaused based on is_playing's value
            console.log(`[USE EFFECT] setting isPaused from useEffect based in ${is_playing_}`);
        if(wasClicked){
            setIsPaused(prev=>!is_playing_);
            setWasClicked(prev=>false);
        }
       // }
    }, [is_playing_]);

    function clickHandler(){
       if(isPaused){
           (async ()=>{await resumeHandler();})();
       }else{
           (async ()=>{await pauseHandler();})();
       }
    }
    if(!wasClicked){
        if(!is_playing !== isPaused){
            setIsPaused(prev=>!is_playing);
        }
    }
    let btn;
    if(isPaused === null){
        btn = (!is_playing) ? play : pause;
    }else{
        //console.log(`was here in PLAY button: isPaused: ${isPaused}`);
        btn = (isPaused) ? play : pause;
    }
    return (
        <div className={disable ? "pause-play-disabled-control": "pause-play"} onClick={()=>{
            if(!disable && !isInActive && !wasClicked){
                setWasClicked(prev=>true);
                clickHandler();
                //console.log(`[ON CLICK] setting isPaused from onClick event on button.`);
                setIsPaused(prev=>!prev);
            }
            }}>
                <img src={btn}
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
