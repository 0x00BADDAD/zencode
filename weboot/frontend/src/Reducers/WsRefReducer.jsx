
export const wsRefReducer = (wsRef, action) => {
    switch(action.type){
        case 'enabled':{
            return action.wsRef;
        }
    }
}
