import {useReducer} from 'react';
import {trackMetaDataReducer} from '../Reducers/TrackMetaDataReducer.jsx';
import {TrackMetaDataContext, TrackMetaDataDispatchContext} from '../Contexts/TrackMetaDataContext.jsx';

export default function TrackMetaDataProvider({children, initialTrackMetaData}){
    const [trackMetaData, dispatch] = useReducer(trackMetaDataReducer, initialTrackMetaData);

    return (
        <TrackMetaDataContext value={trackMetaData}>
            <TrackMetaDataDispatchContext value={dispatch}>
                {children}
            </TrackMetaDataDispatchContext>
        </TrackMetaDataContext>
    )
}
