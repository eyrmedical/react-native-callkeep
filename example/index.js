/**
 * @format
 */

import {AppRegistry, NativeModules} from 'react-native';
// We need to import the native module outside the registerHeadlessTask function to make
// sure the react context bundle it as well during headless task execution.
import BackgroundTimer from 'react-native-background-timer';
import App from './App1';
import {name as appName} from './app.json';

AppRegistry.registerComponent(appName, () => App);

AppRegistry.registerHeadlessTask(
  'CallNotificationEventEmitter',
  () => async taskData => {
    console.log('setTimeout: ', typeof BackgroundTimer.setTimeout);
    console.log('start: ', typeof BackgroundTimer.start);
    console.log('websocket: ', typeof WebSocket.name);
    const BackgroundCallBannerModule = NativeModules.BackgroundCallBannerModule;
    try {
      console.log(
        'CallNotificationEventEmitter called from background service',
      );
      console.log('Second log');
      //   if (data) {
      //     console.log('has data');
      //   } else {
      //     console.log('No data');
      //   }
    //   BackgroundCallBannerModule.startCallBanner({});
    } catch (error) {
      console.log(`Error trying to run headless task: ${error.message}`);
    }
    return new Promise(resolve => {
      const webSocket = new WebSocket('ws://localhost:9898');
      webSocket.onopen = event => {
        console.log('WebSocket connected');
        webSocket.send('Q1');
      };
      webSocket.onmessage = event => {
        console.log('Response from server: ', event.data);
      };
      BackgroundTimer.setTimeout(() => {
        webSocket.close();
        console.log('timer called CallNotificationEventEmitter');
        resolve();
      }, 10000);
    });
  },
);
