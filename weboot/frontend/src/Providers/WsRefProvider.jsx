import {useReducer} from 'react';
import {wsRefReducer} from '../Reducers/WsRefReducer.jsx';
import {WsRefContext, WsRefDispatchContext} from '../Contexts/WsRefContext.jsx';

export default function WsRefProvider({children}){
    const [state, dispatch] = useReducer(wsRefReducer, null);

    return (
        <WsRefContext value={state}>
            <WsRefDispatchContext value={dispatch}>
                {children}
            </WsRefDispatchContext>
        </WsRefContext>
    )
}
