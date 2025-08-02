import {useState} from 'react';



export default function SpotifyLogin(){
    const [inProc, setInProc] = useState(false);

    async function spotifyLogin () {
        setInProc(true);
        try {
            const resp = await fetch("http://127.0.0.1:3000/api/spotify_login_once",{
                method: "GET",
                redirect: 'manual',
            });
            console.log(`resp status is: ${resp.status} and type: ${resp.type}`);
            console.log(`resp location: ${resp.headers.get('Location')}`)
            // supposed to get redirected by the backend from here so we are not inspecting the response.
             // Check if the response is a redirect
                if (resp.status === 302 || resp.status === 303) {
                    console.log(`resp is redirected!`)
                    // Get the final URL after the redirect
                    const finalUrl = resp.headers.get('Location');

                    // Manually redirect the browser to the new URL
                    window.location.href = finalUrl;
                    
                    // Return early to prevent the rest of the code from running
                    return;
                }
        } catch (e) {
            console.error(`Some error occurred while /api/spotify_login_once: ${e}`);
        }finally{
            setInProc(false);
        }
    }
    return (
        <div>
            <p> Click on the button below to initiate Spotify login.</p>
            <a href="http://127.0.0.1:3000/api/spotify_login_once">
              <button>Log in with Spotify</button>
            </a>
        </div>
    )


}
