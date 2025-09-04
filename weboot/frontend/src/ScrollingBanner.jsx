import {useState, useRef, useEffect} from 'react';




export default function ScrollingBanner({songName}){

      const parentRef = useRef(null);
      const childRef = useRef(null);
      const [restart, setRestart] = useState(false);

      useEffect(() => {
        if(!parentRef.current || !childRef.current) return;
        const parentWidth = parentRef.current.offsetWidth;
        const childWidth = childRef.current.offsetWidth;
        const v1 = (574/11000);
        const v2 = ((574 + 174.11)/13000);
        const v3 = (174.11/3000);

        console.log("Parent width:", parentWidth);
        console.log("Child width:", childWidth);

        const start = parentWidth;

        const end = -childWidth;

        const anim1 = childRef.current.animate(
          [
            { transform: `translateX(0px)` },
            { transform: `translateX(${end}px)` }
          ],
          {
           duration: (childWidth/v1), // ms
            iterations: 1,
            easing: "linear",
            delay: 3000
          }
        );


        anim1.finished.then(()=>{
            if(!childRef.current) return;

            const anim2 = childRef.current.animate(
                  [
                    { transform: `translateX(${start}px)` },
                    { transform: `translateX(${end}px)` }
                  ],
                  {
                    duration: ((childWidth+parentWidth)/v2), // ms
                    iterations: 2,
                    easing: "linear"
                  }
            );

            anim2.finished.then(()=>{
                if(!childRef.current) return;
                  const anim3 = childRef.current.animate(
                        [
                            {transform: `translateX(${start}px)`},
                            {transform: `translateX(0px)`}
                        ],
                        {
                            duration: (parentWidth/v3),
                            iterations: 1,
                            easing: "linear"
                        }
                    );
                  anim3.finished
                    .then(()=>{
                      setRestart(prev => !prev);
                  })
                 .catch(err => {
                      if (err.name !== "AbortError") throw err;
                  });

              })
             .catch(err => {
                      if (err.name !== "AbortError") throw err;
            });
          })
        .catch(err => {
             if (err.name !== "AbortError") throw err;
        });

        return ()=>{
             // Cleanup on unmount or deps change
            if(childRef.current){
                 childRef.current.getAnimations().forEach(anim => anim.cancel());
                 childRef.current.style.transform = "translateX(0%)";
            }
        };
      }, [restart, songName]);

// 573.52px
// 147.11px
    return (
            <div className="song-name-container" ref={parentRef}>
                    <div className="song-name-text" ref={childRef}>
                        {songName}
                    </div>
            </div>
        )


}
