import * as React from 'react';
import * as ReactDOM from 'react-dom';

import App from './App';
import { resetEnvironment } from './resetEnvironment';

resetEnvironment();
const root = document.getElementById('root');
ReactDOM.render(<App/>, root);
