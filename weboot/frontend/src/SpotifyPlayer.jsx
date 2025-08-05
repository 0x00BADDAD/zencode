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

export default function SpotifyPLayer(){
    const [playerRef, setPlayerRef] = useState();

    useEffect(() => {
          window.onSpotifyWebPlaybackSDKReady = () => {
              const token = accessToken;

              const player = new Spotify.Player({
                name: 'Web Playback SDK Quick Start Player',
                getOAuthToken: async cb => {
                    const freshToken = await fetchAccessToken(userEmail);
                    cb(freshToken);
                },
                volume: 0.8
              });
              player.addListener('ready', async ({ device_id }) => {
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

              });

             player.addListener('initialization_error', ({ message }) => {
                  console.error(message);
              });
                player.connect();
              //setPlayerRef(player);
          }
    }, []);




    return (
        <p> Playback shoulb be tranferred</p>
    )
}
