


export const trackMetaDataReducer = (currTrackMetaData, action) =>{
    switch(action.type){
        case 'update': {
            return {
                track_uri: action.track_uri,
                progress_ms: action.progress_ms,
                is_playing : action.is_playing,
                name: action.name,
                artists: action.artists,
                disc_number: action.disc_number
            }
        }
    }
}
