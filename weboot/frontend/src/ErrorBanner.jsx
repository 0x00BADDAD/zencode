import {useState, useEffect, useRef} from 'react';


export default function ErrorBanner({errMsg, errReported, onClickTrigger, reporting, isTrack}){

    const className = (!reporting ? (isTrack ? "error-banner-track" : "error-banner-player") : (!isTrack ? "loading-error-banner-player" : "loading-error-banner-track"));
    console.log(`cdcdcdcdcdcdcdcd className from ErrorBanner is: ${className}`);
    const bgColor = errReported ? "#1ED760" : "#F22222";
    return (
        <div className={className} style={{backgroundColor: `${bgColor}`}} onClick={onClickTrigger}>
            <div className="error-banner-text">
                {errMsg}
            </div>
        </div>
    );

}
