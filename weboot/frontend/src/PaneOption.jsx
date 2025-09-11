import {useState, useEffect, useRef} from 'react';




export default function PaneOption({id, offset, isLast, setIsOpen, transferHandler}) {
    const radiusOpt = isLast ? "0px 0px 15px 15px" : "0px";
    const [hovered, setHovered] = useState(false);
    const color = hovered ? "#D9D9D9" : "#FFFFFF";

    return (
        <div className="pane-container" style={{
          top: `${49.06 + 4.98*offset}%`,
          borderRadius: `${radiusOpt}`,
          backgroundColor: `${color}`,
          left: "53.9%",
          width: "9.72%",
          height: "4.98%"
        }}
        onMouseEnter={()=>setHovered(prev=>true)}
        onMouseLeave={()=>setHovered(prev=>false)}
            onClick={()=>{setIsOpen(prev=>false); (async ()=>{await transferHandler();})()}}
        >
            <div className="pane-choice" style={{fontWeight: "0"}}>
                {id}
            </div>
        </div>
    )
}
