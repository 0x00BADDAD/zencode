import React from 'react';
import {createRoot} from 'react-dom/client'; // For React 18+
import App from './App'; // Assuming your component is in App.js or App.jsx
import App1 from './App1';


const root = createRoot(document.getElementById('root'));
console.log("Yup! this module is loaded from root.js!")
root.render(
    <App/>
);
