import {useState, useRef, useEffect} from 'react';



export default function LoadingBanner({track}){


    return (
        <div className={track ? "loading-track" : "player-track"}>
            <div className="loading-cover-pic"></div>
            <div className="loading-song-name"></div>
            <div className="loading-artist-name"></div>
            <div className="loading-controls"></div>
            <div className="loading-timeline"></div>
        </div>
    )

}
