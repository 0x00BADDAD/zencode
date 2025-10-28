import {useContext, useEffect, useState, useRef} from 'react';






export default function OTPinput({emailSent, setEmailSent, setCurrStage}){


   //const [hideOTPinput, setHideOTPinput] =  useState(true);
   //const [OTPshow, setOTPshow] =  useState(false);
   //useEffect(()=>{
   //    setHideOTPinput(prev=>false);
   //    setTimeout(()=>{
   //        setOTPshow(prev=>true);
   //    }, 200);
   //},[emailSent]);

    const [isClickedVerifyOTP, setIsClickedVerifyOTP] = useState(false);
    const [disableVerifyOTP, setDisableVerifyOTP] = useState(true);

    const [errVerifyOTP, setErrVerifyOTP] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");

    const [OTPexpired, setOTPexpired] = useState(false);
    const OTPexpiredRef = useRef(OTPexpired);
    //useEffect(()=>{
    //    OTPexpiredRef.current = OTPexpired;
    //}, [OTPexpired]);

    const resend_event_handler = (e) =>{
        e.preventDefault();
        e.stopPropagation(); // stop event bubbling
        if(OTPexpiredRef.current){
            (async ()=>{await resendOTP();})();
            setOTPexpired(prev=>false);
        }
    };

    useEffect(()=>{
        OTPexpiredRef.current = OTPexpired;
        const resend_btn = document.querySelector("p[class~='resend-otp-btn']");
        if(OTPexpired){
            if(resend_btn){
                resend_btn.addEventListener("click", resend_event_handler);
            }
        }else{
            if(resend_btn){
                resend_btn.removeEventListener("click", resend_event_handler);
            }
        }
    }, [OTPexpired]);

    const [verifyingOTP, setVerifyingOTP] = useState(false);
    const verifyingOTPRef = useRef(verifyingOTP);

    useEffect(() => {
      verifyingOTPRef.current = verifyingOTP;
    }, [verifyingOTP]);

    async function verifyOTP(){
        setVerifyingOTP(prev=>true);
        const formBody = new FormData();
        formBody.append("d1", d1);
        formBody.append("d2", d2);
        formBody.append("d3", d3);
        formBody.append("d4", d4);
        formBody.append("emailId", currEmailGlobal);
        const resp = await fetch("https://unmei.space/api/verify_otp", {
            method: 'POST',
            body: formBody
        });
        if(!resp.ok){
            setErrVerifyOTP(prev=>true);
            setVerifyingOTP(prev=>false);
            setErrorMsg(prev=><div className="err-otp-form">Error! Please Try Again.</div>);
            return;
        }
        const respBody = await resp.json();
        const {otp_status, stage_code, session_id} = respBody;
        if(otp_status !== "success"){
            setErrVerifyOTP(prev=>true);
            switch(otp_status){
                case "invalid":
                    setErrorMsg(prev=><div className="err-otp-form">Invalid OTP. Try Again.</div>);
                case "expired":
                    setOTPexpired(prev=>true);
                    setErrorMsg(prev=><div className="err-otp-form">OTP Expired! <p className="resend-otp-btn" style={{textDecoration:"underline"}} onClick={resendOTP}>Resend</p></div>);
            }

        }else{
            sessionId = session_id; // setting the global sessionId
            setErrVerifyOTP(prev=>false);
            setErrorMsg(prev=><div className="err-otp-form" style={{color: "green"}}>OTP verified!</div>);
            setTimeout(()=>{setCurrStage(prev=>stage_code);}, 1000);
        }
        setVerifyingOTP(prev=>false);
    }

    async function resendOTP(){
        const formBody = new FormData();
        formBody.append("emailId", currEmailGlobal);

        const resp = await fetch("https://unmei.space/api/resend_otp", {
            method: "POST",
            body: formBody
        });

        if(!resp.ok){
            setErrVerifyOTP(prev=>true);
            setErrorMsg(prev=><div className="err-otp-form">Something wrong! Try later</div>);
            return;
        }
        setErrVerifyOTP(prev=>true);
        setErrorMsg(prev=><div className="err-otp-form" style={{color: "green"}}>OTP Resent!</div>);
        setTimeout(()=>{setErrVerifyOTP(prev=>false);}, 3000)
    }

    const onMouseDown_Verify_OTP_Handler = () => {
        setIsClickedVerifyOTP(prev=>true);
        document.addEventListener("mouseup", onMouseUp_Verify_OTP_Handler);
    }

    const onMouseUp_Verify_OTP_Handler = () => {
        setIsClickedVerifyOTP(prev=>false);
        (async ()=>{await verifyOTP();})();
        document.removeEventListener("mouseup", onMouseUp_Verify_OTP_Handler);
    }

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
            digitEle.addEventListener("mousedown", (e)=>{
                e.preventDefault();
                e.stopPropagation();
                if(!editActiveRef.current){
                    setEditActive(prev=>true);
                }
                console.log("FFFFFFFFired! click event in digitEle");
            });
        }

        window.addEventListener("mousedown", (e)=>{
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

            //console.log(`!!!!!!! without validation was here setting the digit with value ${keyName}`);

            const isValidNumKey = ((validKeys.indexOf(keyName)!==-1) && !!!meta && !!!shift && !!!isRepeat);
                if(isValidNumKey){
                    //console.log(`!!!!!!!was here setting the digit with value ${keyName}`);
                    if(activeIdxRef.current === 1){
                        setD1(prev=>keyName);
                    }else if(activeIdxRef.current === 2){
                        setD2(prev=>keyName);
                    }else if(activeIdxRef.current === 3){
                        setD3(prev=>keyName);
                    }else if(activeIdxRef.current === 4){
                        setD4(prev=>keyName);
                        setDisableVerifyOTP(prev=>false);
                    }
                    if(activeIdxRef.current <= 4){
                        setActiveIdx(prev=>prev+1);
                    }
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
                        setDisableVerifyOTP(prev=>true);
                    }
                    setActiveIdx(prev=>Math.max(1,prev-1));
                }
            }
        })

    }, []);

    let submit_btn_class = null;

    if(disableVerifyOTP){
        submit_btn_class = "disable-otp-submit-btn";
    }else{
        if(isClickedVerifyOTP){
            submit_btn_class = "otp-submit-btn-clicked";
        }else{
            submit_btn_class = "otp-submit-btn";
        }
    }

    //let errorMsg = "Invalid OTP. Try again.";

    return (
        <>
            {/* errVerifyOTP && (<div className="err-otp-form">{errorMsg}</div>)*/}
            { errVerifyOTP && errorMsg}
            <div className="otp-form">

                <div className="otp-digit-1"><input type="number" value={d1} min="0" max="9" className={editActive ? "custom-input-active" : "custom-input"}/> </div>

                <div className="otp-digit-2"><input type="number" value={d2} min="0" max="9" className={editActive ? "custom-input-active" : "custom-input"}/> </div>

                <div className="otp-digit-3"><input type="number" value={d3} min="0" max="9" className={editActive ? "custom-input-active" : "custom-input"}/> </div>

                <div className="otp-digit-4"><input type="number" value={d4} min="0" max="9" className={editActive ? "custom-input-active" : "custom-input"}/> </div>

                <input type="submit" value="Verify OTP" className={submit_btn_class} onMouseDown={onMouseDown_Verify_OTP_Handler}/>

           </div>
        </>
    );
}

