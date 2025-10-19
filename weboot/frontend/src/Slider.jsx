import {useState, useRef, useEffect} from 'react';



export default function Slider({perCent, isTrack, seekTrack, isInActive}){
    const [isDragging, setIsDragging] = useState(false);
    const [currElapse, setCurrElapse] = useState(perCent);
    const [seeking, setSeeking] = useState(false);
    const [pointHovered, setPointHovered] = useState(false);
    const timelineRef = useRef(null);
    const currElapseRef = useRef(currElapse);

    // Use a useEffect to keep the ref updated with the latest state
    useEffect(() => {
        currElapseRef.current = currElapse;
    }, [currElapse]);



    const dragHandler = (e) =>{
        e.preventDefault();
        setIsDragging(prev => true);
        setSeeking(prev => true);
        setPointHovered(prev => true);
        setCurrElapse(prev => elapsedTime);
        //setCurrElapse(prev => elapsedTime);
        //console.log("dragging activated...");
        document.addEventListener("mousemove", moveHandler);
        document.addEventListener("mouseup", mouseUpHandler);
    }

    const moveHandler = (e) => {
        //if(!isDragging) return;
        //console.log("moveHandler fired!");
        const rect = timelineRef.current.getBoundingClientRect();
        let offsetX = e.clientX - rect.left;
        //console.log(`offsetX is: ${offsetX}`);
        //clamp within container
        if (offsetX < 0) {offsetX = 0;}
        if (offsetX > rect.width) {offsetX = rect.width;}
        const elapsed_ = Math.ceil((offsetX / rect.width) * 100);
        setCurrElapse(prev => elapsed_);
    }

    const mouseUpHandler = () =>{
        //console.log("mouseup fired! isDragging false");
        console.log(`-------> passing currElapse as: ${currElapseRef.current}`);
        (async ()=>{await seekTrack(Math.floor(currElapseRef.current))})();
        setIsDragging(prev => false);
        setPointHovered(prev => false);
        document.removeEventListener("mousemove", moveHandler);
        document.removeEventListener("mouseup", mouseUpHandler);
    }

    const onClickHandler = (e)=>{
        const rect = timelineRef.current.getBoundingClientRect();
        let offsetX = e.clientX - rect.left;
        if (offsetX < 0) {offsetX = 0;}
        if (offsetX > rect.width) {offsetX = rect.width;}
        const elapsed_ = Math.ceil((offsetX / rect.width) * 100);
        setSeeking(prev=>true);
        setCurrElapse(prev => elapsed_);
        (async ()=>{await seekTrack(Math.floor(elapsed_))})();
    }

    if(isTrack){
        console.log(`!!currElapse: ${currElapse} and perCent: ${perCent}`);
    }

    //let perCent = (elapsedTime / totalTime) * 100;
    console.log(`**************>the value of perCent is: ${perCent}`);
    if(seeking){
        //perCent = (currElapse / totalTime) * 100;
        if(!isDragging){
            if(Math.abs(elapsedTime - Math.floor(currElapse)) < 3000){setSeeking(prev=>false);}
        }
    }
    //else{
        //perCent = (elapsedTime / totalTime) * 100;
    //}

    if(isInActive){perCent=0;}
    //const perCent = (!isTrack && isDragging) ? (currElapse / totalTime) * 100 : (elapsedTime / totalTime) * 100;

    return (
        <div ref={timelineRef} className="timeline" onClick={!isTrack ? onClickHandler: ()=>{}}
            style={{
                cursor: `${isTrack? 'text': 'pointer'}`
            }}
            style={{ background: `linear-gradient(to right, red ${perCent}%, #D9D9D9 ${perCent}%)` }}
        >
            <div className={isDragging? "timeline-pointer" : "timeline-pointer"}
                style={{
                    left: `${Math.ceil(perCent)}%`,
                    transform: `translate(-50%, -50%) scale(${pointHovered ? 1.5: 1})`
                }}
                onMouseDown={!isTrack ? dragHandler : ()=>{}}
                onMouseEnter={!isTrack ? ()=>setPointHovered(true) : ()=>{}}
                onMouseLeave={()=>{if(!isDragging && !isTrack){setPointHovered(false);}}}
            >
            </div>
        </div>
    )
}
