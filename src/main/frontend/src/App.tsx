import * as React from 'react';
import {FunctionComponent, useEffect, useState} from 'react';
import Tab from 'react-bootstrap/Tab';
import Tabs from 'react-bootstrap/Tabs';

import RoleType from './model/RoleType';

import './css/App.css';
import 'bootstrap/dist/css/bootstrap.min.css';

// @ts-ignore
const rootUrl = rootURL;
// @ts-ignore
const csrfCrumb = crumb.value;


const App: FunctionComponent = () => {
  const [authorizationStrategy, setAuthorizationStrategy] = useState(null);

  useEffect(() => {
    (async () => {
      const request = await fetch(`${rootUrl}/folder-auth/authorizationStrategy`, {
        headers: {
          'Jenkins-Crumb': csrfCrumb,
        },
      });
      const data = await request.json();
      setAuthorizationStrategy(data);
    })().catch(err => {
      throw new Error(`Unable to load authorization strategy: ${err}`);
    });
  });

  return (
    <Tabs defaultActiveKey={RoleType.GLOBAL} id='role-type-tabs' variant='tabs'>
      <Tab eventKey={RoleType.GLOBAL} title='Global Roles' tabClassName='tab'>
        Hello world - Global Roles
      </Tab>
      <Tab eventKey={RoleType.FOLDER} title='Folder Roles' tabClassName='tab'>
        Hello world - Folder Roles <br/>
        {authorizationStrategy && JSON.stringify(authorizationStrategy)}
      </Tab>
      <Tab eventKey={RoleType.AGENT} title='Agent Roles' tabClassName='tab'>
        Hello world - Agent Roles
      </Tab>
    </Tabs>
  );
}

export default App;
