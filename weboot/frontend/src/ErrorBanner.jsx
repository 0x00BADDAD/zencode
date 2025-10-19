import {useState, useEffect, useRef} from 'react';


export default function ErrorBanner({errMsg, onClickTrigger, reporting, isTrack}){

    const className = (!reporting ? (isTrack ? "error-banner-track" : "error-banner-player") : (!isTrack ? "loading-error-banner-player" : "loading-error-banner-track"));
    console.log(`cdcdcdcdcdcdcdcd className from ErrorBanner is: ${className}`);
    return (
        <div className={className} onClick={onClickTrigger}>
            <div className="error-banner-text">
                {errMsg}
            </div>
        </div>
    );

}
