import {useState, useEffect, useContext} from 'react';
import {TrackMetaDataContext, TrackMetaDataDispatchContext} from './Contexts/TrackMetaDataContext.jsx';



export default function SpotifyTrack() {
    //const [currTrackUri, setCurrTrackUri] = useState('');

    const trackMetaDataDispatch = useContext(TrackMetaDataDispatchContext);
    const trackMetaData = useContext(TrackMetaDataContext);
    const [metaData, setMetaData] = useState({});


    useEffect(()=>{
         // Connect to WebSocket server
        const ws = new WebSocket("ws://127.0.0.1:3000/ws1");
            //setSocket(ws);

            // When message is received
            ws.onmessage = (event) => {
              try {
                const data = JSON.parse(event.data); // if message is JSON
                console.log(`data is: ${event}`);
                const {name, track_uri, resource_uri, artists , progress_ms, is_playing, disc_number}= data;

                const newMetaData = {
                    name: name,
                    artists: artists,
                    track_uri: track_uri,
                    progress_ms: progress_ms,
                    is_playing: is_playing,
                    disc_number: disc_number,
                    resource_uri: resource_uri
                };

                if (!metaData || resource_uri !== metaData.resource_uri || track_uri !== currTrackUri || progress_ms !== metaData.progress_ms || is_playing !== metaData.is_playing){
                    setMetaData(prev => newMetaData);
                   // setCurrTrackUri(prev => track_uri);
                    trackMetaDataDispatch({
                        type: "update",
                        ...newMetaData
                    });
                }
              } catch (e) {
                  const err = {
                      'error' : "something went wrong on parsing the received message"
                  };
                  setMetaData(prev => err); // plain text fallback
              }
            };

            ws.onerror = (error) => {
              console.error("WebSocket error:", error);
            };

            ws.onclose = () => {
              console.log("WebSocket connection closed");
            };

            // Cleanup on component unmount
            return () => {
              ws.close();
            };
    },[]);
    const prettyJson = JSON.stringify(metaData, undefined, 2);

    return (
        <>
            <p>Atharv's device:</p>
            {Object.keys(metaData).length !== 0? (<pre>{prettyJson}</pre>) : (<p> loading track...</p>)}
            </>
    );
}
