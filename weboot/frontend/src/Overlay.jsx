import {useState, useRef, useEffect, useContext} from 'react';
import {DisableWebSocketContext, DisableWebSocketDispatchContext} from './Contexts/DisableWebSocketContext.jsx';

const useBroadcastChannel = (channelName, dispatch, setChannel) => {
  useEffect(() => {
    const channel = new BroadcastChannel(channelName);

    channel.onmessage = (event) => {
        if(event.data === "new-tab-opened"){
              dispatch({
                  type: "disable",
                  disable: true
              });
        }
    };
      setChannel(prev=>channel);

    return () => {
      channel.close();
    };
  }, [channelName, dispatch]);
};



export default function Overlay() {
    const disableWebSocket = useContext(DisableWebSocketContext);
    const dispatch = useContext(DisableWebSocketDispatchContext);

    const [channel, setChannel] = useState(null);


    useBroadcastChannel("zencode-channel", dispatch, setChannel);
    useEffect(()=>{
        if(channel){
            channel.postMessage('new-tab-opened');
        }
    }, [channel]);


    return (disableWebSocket && (
                        <>
                          {/* Overlay with partial blur */}
                          <div className="overlay" />

                          {/* Modal content */}
                          <div className="modal">
                            <h2>Attention</h2>
                            <p>This playback can't be opened in multiple contexts. kindly close this tab/window.</p>
                          </div>
                        </>
                  )
            )

}
