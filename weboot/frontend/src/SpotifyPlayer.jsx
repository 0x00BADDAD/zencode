import {useState, useEffect} from 'react';



async function fetchAccessToken(email){
        const params = new URLSearchParams();
        params.append('email', email);
        const resp = await fetch(`http://127.0.0.1:3000/api/fresh_token?${params.toString()}`, {
            method: "GET"
        });
        const token = await resp.json();
        return token.access_token;
}


// SpotifyPlayer needs to depend upon reactive value of context_uri which will be fed from SpotifyTrack ws
// endpoint and do a PUT request to the spotify api every time the context_uri is changed
export default function SpotifyPLayer(){
    const [playerRef, setPlayerRef] = useState(null);
    //const [deviceIds, setDeviceIds] = useState([]);
    //const [activeDeviceId, setActiveDeviceId] = useState();

    useEffect(() => {
            window.onSpotifyWebPlaybackSDKReady = () => {
                if (!userGrantedPermission) {
                      console.warn("User did not grant permission. Skipping player init.");
                      return;
                }
              //const token = 'BQC-uSfZKu7nIgOmAw_KIjcc05DorR0Z_pGuld9cDJCQNRmqwLoizjYhWOkjJ0rC73YqIbrqFntrkNcSSFIeA9fkGI60gAK6CgJDdgHdZd-O06kRNDQMqyvUsfD91MAS30R0x_VR0pZoTpiASWuX3IE5wMVNHsyNihxhBm8OTWGjE_GB_ZJ9_VTkoi4dCbE1CavgbzvZpgkg6VXvgmcIn3PjBDXcSMq5Oakbt99ZS5BJ80d4OyNqwJrEikgw6eYCp4He1Lpl1cS8N9xxOvEmFh4XiiyMEKYVz43-vf38wzQ_Dnb5694hMhru81Ja_maF';
              const token = accessToken;

              const oAuthRefersh = async (cb) => {
                      const freshToken = await fetchAccessToken(userEmail);
                      cb(freshToken);
              };

                const player = new Spotify.Player({
                    name: 'Web Playback SDK Quick Start Player',
                    getOAuthToken: oAuthRefersh,
                    volume: 0.8
                });

              // Ready
              const readyCb = async ({device_id}) => {
                    console.log('Ready with Device ID', device_id); //  this is what you use
                    // transferring playback
                    const resp = await fetch('http://127.0.0.1:3000/api/transfer_playback', {
                    method: 'POST',
                    body: JSON.stringify({ device_ids: [device_id], play: true }),
                    headers: {
                      'Content-Type': 'application/json',
                      'X-Token': `${token}`
                      },
                    });
                    const resp_ = await resp.json();
                    console.log(`Transferred playback! response: ${resp_}`);

                };

                player.addListener('ready', readyCb);

                // Not Ready
                player.addListener('not_ready', ({ device_id }) => {
                    console.log('Device ID has gone offline', device_id);
                });

                player.addListener('initialization_error', ({ message }) => {
                    console.error(message);
                });

                player.addListener('authentication_error', ({ message }) => {
                    console.error(message);
                });

                player.addListener('account_error', ({ message }) => {
                    console.error(message);
                });


                player.connect();
                setPlayerRef(player);
            }

            // load the external spotify script after defining the SDK callback as done above
            const script = document.createElement('script');
            script.src = 'https://sdk.scdn.co/spotify-player.js';
            script.async = true;
            script.onload = () => {
              console.log('Script loaded successfully!');
            };
            script.onerror = (error) => {
              console.error('Error loading script:', error);
            };
            document.body.appendChild(script); // this starts downloading the script


    }, []);




    return (
        <p> Playback shoulb be tranferred</p>
    )
}
