import * as React from 'react';

import Container from "@material-ui/core/Container";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import AppBar from "@material-ui/core/AppBar";
import {Theme, withStyles} from "@material-ui/core/styles";
import {Box} from "@material-ui/core";

const styles = (theme: Theme) => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
  }
});

export interface AppState {
  readonly value: number;
  authStrategy: string;
}

class App extends React.Component<{ classes: any }, AppState> {
  constructor(props: { classes: any }) {
    super(props);
    this.state = {
      value: 0,
      authStrategy: '',
    }
  }

  async componentDidMount(): Promise<void> {
    // @ts-ignore
    const rootUrl = rootURL;
    // @ts-ignore
    const csrfCrumb = crumb.value;

    const request = await fetch(`${rootUrl}/folder-auth/authorizationStrategy`, {
      headers: {
        'Jenkins-Crumb': csrfCrumb,
      }
    })
    const data = await (await request.blob()).text();

    this.setState({ value: this.state.value, authStrategy: data});
  }

  render() {
    const {classes} = this.props;
    return (
      <Container>
        <div className={classes.root}>
          <AppBar position='static'>
            <Tabs value={this.state.value}
                  centered
                  onChange={(_, newValue: number) => {
                    this.setState({value: newValue});
                  }}>
              <Tab label='Global Roles'/>
              <Tab label='Folder Roles'/>
              <Tab label='Agent Roles'/>
            </Tabs>
          </AppBar>
          <Box>
            {this.state.authStrategy}
          </Box>
        </div>
      </Container>
    )
  }
}

export default withStyles(styles)(App);
