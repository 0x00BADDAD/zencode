import {useState, useEffect, useRef} from 'react';
import paneIcon from './static/images/down-arrow.png';
import PaneOption from './PaneOption.jsx';


export default function DevicePane({deviceIds, activeDeviceId, transferNew, transferOld}) {
  const [isOpen, setIsOpen] = useState(false);
  //const [chosenIdx, setChosenIdx] = useState(1);


  const openHandler = (e) => {
      if(e){
          e.preventDefault();
      }
    console.log("openHandler fired!");
    setIsOpen(true);
  };

  const closeHandler = (e) => {
      if(e){
          e.preventDefault();
      }
    //console.log("closeHandler fired!");
    setIsOpen(false);
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (!event.target.closest(".pane-container")) {
        //console.log("Clicked outside, closing");
        closeHandler();
      }
    };
    document.addEventListener("click", handleClickOutside);
    return () => document.removeEventListener("click", handleClickOutside);
  }, []);

 //console.log("value of isOpen is:", isOpen);

 // const chosenDeviceId = chosenIdx === -1 ? "No-device" : deviceIds[chosenIdx];
  return isOpen ? (
        <div className="pane-overlay"
            style={{
             position: "fixed",
             top: "0",
             left: "0",
             width: "100vw",
             height: "100vh",
             background: "rgba(0,0,0,0.5)",
             zIndex: "99"
            }}
        >
      <div
        className="pane-container"
        style={{
          borderRadius: "15px 15px 0px 0px",
          left: "53.9%",
          top: "49.06%",
          width: "9.72%",
          height: "4.98%"
        }}
        onClick={(e) => {e.stopPropagation(); closeHandler();}} // don't close immediately
      >
        <div className="pane-choice">{activeDeviceId}</div>
        <div className="pane-icon">
          <img style={{ transform: "rotate(180deg)" }} src={paneIcon} />
        </div>
        <div
          style={{
            position: "absolute",
            top: "98%",
            left: "50%",
            transform: "translateX(-50%)",
            width: "85%",
          }}
        >
          <hr
            style={{
              margin: "0",
              border: "none",
              height: "2px",
              backgroundColor: "black",
            }}
          />
        </div>
      </div>
      {deviceIds.map((deviceId, idx) => (
        <PaneOption
          key={idx}
          id={deviceId}
          offset={idx + 1}
          isLast={deviceIds.length === idx + 1}
          setIsOpen={setIsOpen}
          transferHandler={deviceId === "This-device" ? transferNew : transferOld}
        />
      ))}
        </div>
  ) : (
    <div className="pane-container" onClick={openHandler}>
      <div className="pane-choice">{activeDeviceId}</div>
      <div className="pane-icon">
        <img src={paneIcon} />
      </div>
    </div>
  );
}

