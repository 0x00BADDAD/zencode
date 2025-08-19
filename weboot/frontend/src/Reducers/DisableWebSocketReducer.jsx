

export const disableWebSocketReducer = (disableWs, action) => {
    switch(action.type){
        case 'disable': {
            return {
                disable: action.disable
            };
        }
    }
}
