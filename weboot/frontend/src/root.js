// src/index.js (or your main entry file)
import React from 'react';
import {createRoot} from 'react-dom/client'; // For React 18+
import App from './app'; // Assuming your component is in App.js or App.jsx
import SubApp from './subapp'
import SubSubApp from './subsubapp'
const root = createRoot(document.getElementById('root'));
console.log("Yup! this module is loaded from root.js!")
root.render(
    <App>
        <SubApp>
            <SubSubApp/>
        </SubApp>
    </App>
);
