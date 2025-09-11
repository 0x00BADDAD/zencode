import {useReducer, useState, useEffect} from 'react';
import {disableWebSocketReducer} from '../Reducers/DisableWebSocketReducer.jsx';
import {DisableWebSocketContext, DisableWebSocketDispatchContext} from '../Contexts/DisableWebSocketContext.jsx';



export default function DisableWebSocketProvider({children}){
    const [state, dispatch] = useReducer(disableWebSocketReducer, false);

    return (<DisableWebSocketContext value={state}>
        <DisableWebSocketDispatchContext value={dispatch}>
            {children}
        </DisableWebSocketDispatchContext>
    </DisableWebSocketContext>)


}
