import {useState, useRef, useEffect} from 'react';
import RebootButton from './RebootButton.jsx';


export default function LoadingBanner({track, showReboot, errReported, setHideError, setErrReported}){


    return showReboot ? (
                 <div className="track">
                      <RebootButton
                              disabled={!errReported}
                              setHideError={setHideError}
                              setErrReported={setErrReported}
                              isTrack={track}
                      />
                 </div>
            )
            :
            (
                 <div className={track ? "loading-track" : "player-track"}>
                     <div className="loading-cover-pic"></div>
                     <div className="loading-song-name"></div>
                     <div className="loading-artist-name"></div>
                     <div className="loading-controls"></div>
                     <div className="loading-timeline"></div>
                 </div>
            )


}
