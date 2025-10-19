import {useState, useEffect, useContext, useRef} from 'react';
import reboot_icon from './static/images/refresh.png';
import {stages} from './stages.jsx';

export default function RebootButton({disabled, setCurrStage, setPrevStage, prevStage, setErrReported, isTrack}){

    const [isClickedReboot, setIsClickedReboot] = useState(false);

    const onMouseDown_Reboot_Handler = () => {
        setIsClickedReboot(prev=>true);
        document.addEventListener("mouseup", onMouseUp_Reboot_Handler);
    }

    const onMouseUp_Reboot_Handler = () => {
        setIsClickedReboot(prev=>false);
        //once we are out of err stage we dont keep the history of what happend
        setPrevStage(prev=>null);
        setCurrStage(prev=>(prevStage || stages.MAIL));
        //setHideError(prev=>true);
        setErrReported(prev=>false);
        document.removeEventListener("mouseup", onMouseUp_Reboot_Handler);
    }

    const className = (isTrack ? (!disabled ? "Reboot-mid-btn-track" : "Reboot-mid-btn-disabled-track") : (!disabled ? "Reboot-mid-btn-player" : "Reboot-mid-btn-disabled-player"));

    return (
            <button className={className}
                onMouseDown={()=>{if(!disabled){onMouseDown_Reboot_Handler();}}}
                style={{
                    transform: `scale(${isClickedReboot ? 0.97 : 1})`
                }}
            >
            <img src={reboot_icon}
                 style={{
                     position: "absolute",
                     top: "50%",
                     transform: "translateY(-50%)",
                     height: "40%",
                     width: "20%",
                     marginTop: "7.5%"
                 }}
            />
                Reboot
            </button>

    );
}

