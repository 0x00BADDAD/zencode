import React from 'react';
import {createRoot} from 'react-dom/client'; // For React 18+
import App from './App'; // Assuming your component is in App.js or App.jsx
import DisableWebSocketProvider from './Providers/DisableWebSocketProvider.jsx';
import './imports/styles.js';
import './imports/images.js';

const root = createRoot(document.getElementById('root'));
console.log("Yup! this module is loaded from root.js!")
root.render(
    <DisableWebSocketProvider>
        <App/>
    </DisableWebSocketProvider>
);
