import {useState, useRef, useEffect} from 'react';

import {stages} from './stages.jsx';
import rightArrow from './static/images/right-arrow.png';




const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

function isValidEmail(email) {
    return emailRegex.test(email);
}

export default function EmailInput({emailSent, setEmailSent, setCurrStage}){

    //const [hideEmailInput, setHideEmailInput] = useState(false);
    //useEffect(()=>{
    //    setTimeout(()=>{setHideEmailInput(prev=>true)}, 2000);
    //}, [emailSent]);

    const [sendingMail, setSendingMail] = useState(false);
    const [emailRegError, setEmailRegError] = useState(false);


    async function sendMailReg(){
        setSendingMail(prev=>true);

        const formData = new FormData();
        formData.append('email', emailId);
        currEmailGlobal = emailId; // to be returned from backend
        const resp = await fetch("http://127.0.0.1:3000/api/send_mail_reg", {
            method: 'POST',
            body: formData
        });

        if(!resp.ok){
            setEmailRegError(prev=>true);
            setSendingMail(prev=>false);
            return;
        }

        setSendingMail(prev=>false);
        setEmailSent(prev=>true);
        setCurrStage(prev=>stages.OTP_UNVERIFIED);
    }

    useEffect(()=>{
        const editableDiv = document.querySelector("div[class~='email-input']");
        window.addEventListener("DOMContentLoaded", () => {
          editableDiv.blur();
        });
        //editableDiv.textContent = "";
        //editableDiv.blur();
    }, []);


    useEffect(()=>{
        const editableDiv = document.querySelector("div[class~='email-input']");
        editableDiv.textContent = "";
        editableDiv.focus();
    }, [emailRegError]);



    const [editActive, setEditActive] = useState(false);
    const editActiveRef = useRef(editActive);
    useEffect(()=>{
        editActiveRef.current =  editActive;
    }, [editActive]);


    const [isValid, setIsValid] = useState(false);
    const isValidRef = useRef(isValid);
    useEffect(()=>{
        isValidRef.current =  isValid;
    }, [isValid]);

    const [emailId, setEmailId] = useState("");

    useEffect(()=>{

        const editableDiv = document.querySelector("div[class~='email-input']");
        const labelDiv = document.querySelector("div[class~='email-input-label']");

        editableDiv.addEventListener('focus', (e)=>{
            e.preventDefault();
            setEditActive(prev=>true);
        });

        editableDiv.addEventListener('blur', (e)=>{
            e.preventDefault();

            if (editableDiv.textContent.trim().length <= 0) {
                setEditActive(prev=>false);
            }else{
                setEditActive(prev=>true);
            }
        });

        editableDiv.addEventListener('input', (e)=>{
            e.preventDefault();
            const val = editableDiv.textContent;
            console.log(`input event fired and val is: ${val}`);
            if(val.trim().length > 0 && !!!editActiveRef.current){setEditActive(prev=>true);}
            if (isValidEmail(val)) {
                console.log(`Yup set the isValid state to true.`);
                setIsValid(prev=>true);
                setEmailId(prev=>val);
            }else{
                if(isValidRef.current){
                    setIsValid(prev=>false);
                }
            }
        });

        labelDiv.addEventListener('click', (e)=>{
            e.preventDefault();
            editableDiv.focus();
            setEditActive(prev=>true);
        });

    }, []);


    const [showSubmit, setShowSubmit] = useState(false);
    useEffect(()=>{
        if(isValid){
            setTimeout(()=>{setShowSubmit(prev=>true);}, 200);
        }else{
            setTimeout(()=>{setShowSubmit(prev=>false);}, 200);
        }
    }, [isValid]);


    return (<div className={sendingMail ? "email-input-cont-sending" : "email-input-cont"}>
            <div className={editActive ? "email-input-label-focus" : "email-input-label"}
            style={{
                transition: "height 0.1s linear, width 0.1s linear, top 0.1s linear, left 0.1s linear, font-size 0.1s linear"
                }}>
                <p>Your Spotify account mail</p>
            </div>
            <div contentEditable="true" type="text" className="email-input"/>
        {showSubmit && (<div className={`email-submit-btn ${isValid ? 'fade-in' : 'fade-out'}`} onClick={()=>{(async ()=>{await sendMailReg();})();}}><div className="email-submit-btn-cont"> <img src={rightArrow}/> </div></div>)}
            {emailRegError && (<div className="email-reg-error"><p>Something went wrong! try again</p></div>)}
        </div>);
}

