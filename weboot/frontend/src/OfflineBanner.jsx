import {useState, useEffect, useContext} from 'react';



export default function OfflineBanner({isOffline}){

    return isOffline ? (
        <div className="offline-banner">
            <div className="offline-banner-text">
                It seems You're offline...
            </div>
        </div>
    )
        :
    (
        <div className="online-banner">
            <div className="online-banner-text">
                Yay! you are back online!
            </div>
        </div>
    );

}
