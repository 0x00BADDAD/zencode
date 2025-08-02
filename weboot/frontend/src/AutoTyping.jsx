import {useState, useEffect, useEffectEvent} from 'react';

const text = ['H', 'e', 'l', 'l', 'o', ',', ' W', 'o', 'r', 'l', 'd', '!'];

export default function AutoTyping(){
    const [charsOut, setCharsOut] = useState(0);



      useEffect(() => {
        const intervalId = setInterval(() => {
            setCharsOut(charsOut => charsOut + 1); // Use the functional update to avoid stale data
        }, 250);

        return () => {
            console.log(`comp unmounted!`)
            clearInterval(intervalId);
        };
      }, []);


    return(
        <div><h1>
        {
           text.map((char, idx)=>{
               if (idx < charsOut){
                   return (<span key={idx}>{char}</span>);
               }
          })
        }
        </h1></div>
    )

}
