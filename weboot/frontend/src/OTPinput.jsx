import {useContext, useEffect, useState, useRef} from 'react';






export default function OTPinput({emailSent, setEmailSent}){


    const [hideOTPinput, setHideOTPinput] =  useState(true);
    const [OTPshow, setOTPshow] =  useState(false);
    useEffect(()=>{
        setHideOTPinput(prev=>false);
        setTimeout(()=>{
            setOTPshow(prev=>true);
        }, 200);
    },[emailSent]);
    const [editActive, setEditActive] = useState(false);
    const editActiveRef = useRef(editActive);

    const validKeys = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"];

    useEffect(() => {
      editActiveRef.current = editActive;
    }, [editActive]);

    const [activeIdx, setActiveIdx] = useState(1);
    const activeIdxRef = useRef(activeIdx);

    useEffect(() => {
      activeIdxRef.current = activeIdx;
    }, [activeIdx]);

    const [d1, setD1] = useState("");
    const [d2, setD2] = useState("");
    const [d3, setD3] = useState("");
    const [d4, setD4] = useState("");

    useEffect(()=>{
        const digitElements = document.querySelectorAll("div[class^='otp-digit-']");
        for(const digitEle of digitElements){
            digitEle.addEventListener("click", (e)=>{
                e.preventDefault();
                e.stopPropagation();
                if(!editActiveRef.current){
                    setEditActive(prev=>true);
                }
                console.log("FFFFFFFFired! click event in digitEle");
            });
        }

        window.addEventListener("click", (e)=>{
            e.preventDefault();
            e.stopPropagation();
            console.log("OOOOOOOOOout of the OTP input click");
            if(editActiveRef.current){
                setEditActive(prev=>false);
            }
        });

        window.addEventListener("keydown", (e)=>{
            if(editActiveRef.current){
            e.preventDefault();
            const keyName = e.key;
            const meta = e.metaKey;
            const shift = e.shiftKey;
            const isRepeat = e.repeat;
            const isBackSpaceKey = keyName === "Backspace";

            console.log(`!!!!!!! without validation was here setting the digit with value ${keyName}`);

            const isValidNumKey = ((validKeys.indexOf(keyName)!==-1) && !!!meta && !!!shift && !!!isRepeat);
                if(isValidNumKey){
                    console.log(`!!!!!!!was here setting the digit with value ${keyName}`);
                    if(activeIdxRef.current === 1){
                        setD1(prev=>keyName);
                    }else if(activeIdxRef.current === 2){
                        setD2(prev=>keyName);
                    }else if(activeIdxRef.current === 3){
                        setD3(prev=>keyName);
                    }else if(activeIdxRef.current === 4){
                        setD4(prev=>keyName);
                    }
                    setActiveIdx(prev=>prev+1);
                }

                if(isBackSpaceKey){
                    if(activeIdxRef.current === 2){
                        setD1(prev=>"");
                    }else if(activeIdxRef.current === 3){
                        setD2(prev=>"");
                    }else if(activeIdxRef.current === 4){
                        setD3(prev=>"");
                    }else if(activeIdxRef.current === 5){
                        setD4(prev=>"");
                    }
                    setActiveIdx(prev=>Math.max(1,prev-1));
                }
            }
        })

    }, []);

    return !hideOTPinput && (<form className={OTPshow ? "otp-form" : "otp-form-hide"} action="" method="post">
        <div className="otp-digit-1"><input type="number" value={d1} min="0" max="9" className={editActive ? "custom-input-active" : "custom-input"}
            style={{
                transition: "border 0.1s cubic-bezier(0, 0.95, 0.13, 1.83)"
            }}
        /> </div>
                <div className="otp-digit-2"><input type="number" value={d2} min="0" max="9" className={editActive ? "custom-input-active" : "custom-input"}
            style={{
                transition: "border 0.1s cubic-bezier(0, 0.95, 0.13, 1.83)"
            }}

        /> </div>
                <div className="otp-digit-3"><input type="number" value={d3} min="0" max="9" className={editActive ? "custom-input-active" : "custom-input"}
            style={{
                transition: "border 0.1s cubic-bezier(0, 0.95, 0.13, 1.83)"
            }}
        /> </div>
                <div className="otp-digit-4"><input type="number" value={d4} min="0" max="9" className={editActive ? "custom-input-active" : "custom-input"}
            style={{
                transition: "border 0.1s cubic-bezier(0, 0.95, 0.13, 1.83)"
            }}

        /> </div>
           </form>);
}

