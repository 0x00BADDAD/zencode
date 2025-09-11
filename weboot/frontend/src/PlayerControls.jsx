import {useState, useRef, useEffect} from 'react';

const STATUS = {
  SyncedIn: 0,
  SyncedOut: 1,
  LockedIn: 2
};

export default function PlayerControls({disabled, currStatus, setCurrStatus, syncTrack, setKeepInSync, isInActive}){

    const [isClickedSync, setIsClickedSync] = useState(false);
    const [isClickedLock, setIsClickedLock] = useState(false);
//----------------------------------------- SyncedOut---------------
    const onMouseDown_SyncedOut_SyncIn_Handler = () => {
        setIsClickedSync(prev=>true);
        document.addEventListener("mouseup", onMouseUp_SyncedOut_SyncIn_Handler);
    }

    const onMouseUp_SyncedOut_SyncIn_Handler = () => {
        setIsClickedSync(prev=>false);
        (async ()=>{await syncTrack(); setCurrStatus(prev=>0);})();
        document.removeEventListener("mouseup", onMouseUp_SyncedOut_SyncIn_Handler);
    }
//----------------------------------------- SyncedOut----------------
    const onMouseDown_SyncedOut_LockIn_Handler = () => {
        setIsClickedLock(prev=>true);
        document.addEventListener("mouseup", onMouseUp_SyncedOut_LockIn_Handler);
    }

    const onMouseUp_SyncedOut_LockIn_Handler = () => {
        setIsClickedLock(prev=>false);
        setKeepInSync(prev=>true);
        (async ()=>{await syncTrack(); setCurrStatus(prev=>2);})();
        document.removeEventListener("mouseup", onMouseUp_SyncedOut_LockIn_Handler);
    }

//------------------------------------------- SyncedIn-----------------------------------

    const onMouseDown_SyncedIn_LockIn_Handler = () => {
        setIsClickedLock(prev=>true);
        document.addEventListener("mouseup", onMouseUp_SyncedIn_LockIn_Handler);
    }

    const onMouseUp_SyncedIn_LockIn_Handler = () => {
        setCurrStatus(prev=>2);
        setIsClickedLock(prev=>false);
        setKeepInSync(prev=>true);
        document.removeEventListener("mouseup", onMouseUp_SyncedIn_LockIn_Handler);
    }

//------------------------------------------- LockedIn------------------------------------
    const onMouseDown_LockedIn_Lockout_Handler = () => {
        setIsClickedLock(prev=>true);
        document.addEventListener("mouseup", onMouseUp_LockedIn_Lockout_Handler);
    }

    const onMouseUp_LockedIn_Lockout_Handler = () => {
        setCurrStatus(prev=>0);
        setIsClickedLock(prev=>false);
        setKeepInSync(prev=>false);
        document.removeEventListener("mouseup", onMouseUp_LockedIn_Lockout_Handler);
    }


    switch(currStatus){
        case STATUS.SyncedOut:
            return (
                <div className="control-container">
                    <div className={!disabled ? "Syncin-btn" : "Syncin-btn-disabled"}
                         onMouseDown={()=>{if(!disabled){onMouseDown_SyncedOut_SyncIn_Handler();}}}
                         style={{
                            transform: `scale(${isClickedSync ? 0.95 : 1})`
                         }}
                    ><div className="btn-text">Sync-In!</div></div>

                    <div className={!disabled ? "Lockin-btn" : "Lockin-btn-disabled"}
                        onMouseDown={()=>{if(!disabled){onMouseDown_SyncedOut_LockIn_Handler();}}}
                        style={{
                            transform: `scale(${isClickedLock ? 0.95 : 1})`
                        }}
                    ><div className="btn-text">Lock-In!</div></div>
                </div>
            )
        case STATUS.SyncedIn:
            return (
                <div className="control-container">
                    <div className={!disabled ? "Lockin-mid-btn" : "Lockin-mid-btn-disabled"}
                        onMouseDown={()=>{if(!disabled){onMouseDown_SyncedIn_LockIn_Handler();}}}
                        style={{
                            transform: `scale(${isClickedLock ? 0.95 : 1})`
                        }}
                    ><div className="btn-text">Lock-In!</div>
                    </div>
                </div>
            )
        case STATUS.LockedIn:
            return (
                <div className="control-container">
                    <div className={!disabled ? "Lockout-btn" : "Lockout-btn-disabled"}
                        onMouseDown={()=>{if(!disabled){onMouseDown_LockedIn_Lockout_Handler();}}}
                        style={{
                            transform: `scale(${isClickedLock ? 0.95 : 1})`
                        }}
                    >
                        <div className="btn-text-lockout">Lock-Out!</div>
                    </div>
                </div>
            )
    }

}
