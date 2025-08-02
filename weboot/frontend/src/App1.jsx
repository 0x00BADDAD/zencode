import {useState, useEffect} from 'react';

export default function App1({code, client_id, client_secret}){
    const [inProc, setInProc] = useState(true);
    const [respBody, setRespBody] = useState({gotNothing: 'Nothing'});
    useEffect(()=>{
        (async () => {

        try{
            // The input string you want to encode
            const inputString = `${client_id}:${client_secret}`;

            // Step 1: Create a UTF-8 encoded Uint8Array from the string
            const encoder = new TextEncoder();
            const utf8Bytes = encoder.encode(inputString);

            // Step 2: Convert the Uint8Array to a binary string
            const binaryString = String.fromCharCode(...utf8Bytes);

            // Step 3: Encode the binary string to Base64
            const base64String = btoa(binaryString);
            const data = {
              'redirect_uri': "http://127.0.0.1:3000/api/spotify_login_success",
              'code': code,
              'grant_type': "authorization_code",
            };
            const formBody = new URLSearchParams(data);
            // aim to proxy request to https://accounts.spotify.com/api/token
            const resp = await fetch("http://127.0.0.1:3000/api/token", {
                method: "POST",
                headers: {
                    'Content-Type':'application/x-www-form-urlencoded',
                    'Authorization': `Basic ${base64String}`,
                },
                body: formBody,
            });
            if (!resp.ok) {
                  throw new Error(`HTTP error! Status: ${resp.status}`);
            }
            const respB = await resp.json();
            setRespBody(respB);
        }catch(e){
            console.error(`something went wrong when POST req from client: ${e}`)
        }finally{
            setInProc(false);
        }


        })();

        return () => {};
    }, []);

    const prettyJson = JSON.stringify(respBody, undefined, 2);
    return (inProc?
            (<p> Initialiazing the refresh token... kindly wait</p>) :
        (<>

            <p>Access token retrieved from spotify</p>
            <hr/>
            <pre>{prettyJson}</pre>

        </>)
        )


}
