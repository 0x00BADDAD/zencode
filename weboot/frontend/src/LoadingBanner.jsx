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
                 <div className={track ? "track" : "player-track"}>
                     <div className={ track ? "loading-cover-pic" : "player-loading-cover-pic"}></div>
                     <div className={ track ? "loading-song-name" : "player-loading-song-name"}></div>
                     <div className={ track ? "loading-artist-name" : "player-loading-artist-name"}></div>
                     <div className={ track ? "loading-controls" : "player-loading-controls"}></div>
                     <div className={ track ? "loading-timeline" : "player-loading-timeline"}></div>
                 </div>
            )


}
