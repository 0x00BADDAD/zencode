import {useState, useEffect} from 'react';




export default function SpotifyTrack() {
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
                setMetaData(prev => data);
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
        <><pre>
            {prettyJson}
        </pre></>
    );
}
