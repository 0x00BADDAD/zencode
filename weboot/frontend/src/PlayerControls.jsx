import {useState, useRef, useEffect} from 'react';

const STATUS = {
  SyncedIn: 0,
  SyncedOut: 1,
  LockedIn: 2
};

export default function PlayerControls({disabled, currStatus, setCurrStatus, syncTrack, setKeepInSync}){

    const [isClickedSync, setIsClickedSync] = useState(false);
    const [isClickedLock, setIsClickedLock] = useState(false);

    switch(currStatus){
        case STATUS.SyncedOut:
            return (
                <div className="control-container">
                    <div className={!disabled ? "Syncin-btn" : "Syncin-btn-disabled"}
                        onMouseDown={()=>{if(!disabled){setIsClickedSync(prev=>true);}}}
                        style={{
                            transform: `scale(${isClickedSync ? 0.8 : 1})`
                        }}
                        onMouseUp={()=>{
                            if(!disabled){
                                setIsClickedSync(prev=>false);
                                (async ()=>{await syncTrack(); setCurrStatus(prev=>0);})();
                            }
                        }}
                    ><div className="btn-text">Sync-In!</div></div>

                    <div className={!disabled ? "Lockin-btn" : "Lockin-btn-disabled"}
                        onMouseDown={()=>{if(!disabled){setIsClickedLock(prev=>true);}}}
                        style={{
                            transform: `scale(${isClickedLock ? 0.8 : 1})`
                        }}
                        onMouseUp={()=>{
                            if(!disabled){
                                setCurrStatus(prev=>2);
                                setIsClickedLock(prev=>false);
                                setKeepInSync(prev=>true);
                            }
                        }}
                    ><div className="btn-text">Lock-In!</div></div>
                </div>
            )
        case STATUS.SyncedIn:
            return (
                <div className="control-container">
                    <div className={!disabled ? "Lockin-mid-btn" : "Lockin-mid-btn-disabled"}
                        onMouseDown={()=>{if(!disabled){setIsClickedLock(prev=>true);}}}
                        style={{
                            transform: `scale(${isClickedLock ? 0.8 : 1})`
                        }}
                        onMouseUp={()=>{
                            if(!disabled){
                                setCurrStatus(prev=>2);
                                setIsClickedLock(prev=>false);
                                setKeepInSync(prev=>true);
                            }
                        }}
                    ><div className="btn-text">Lock-In!</div>
                    </div>
                </div>
            )
        case STATUS.LockedIn:
            return (
                <div className="control-container">
                    <div className={!disabled ? "Lockout-btn" : "Lockout-btn-disabled"}
                        onMouseDown={()=>{if(!disabled){setIsClickedLock(prev=>true);}}}
                        style={{
                            transform: `scale(${isClickedLock ? 0.8 : 1})`
                        }}
                        onMouseUp={()=>{
                            if(!disabled){
                                setCurrStatus(prev=>0);
                                setIsClickedLock(prev=>false);
                                setKeepInSync(prev=>false);
                            }
                        }}
                    >
                        <div className="btn-text-lockout">Lock-Out!</div>
                    </div>
                </div>
            )
    }

}
