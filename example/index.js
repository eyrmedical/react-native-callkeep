/**
 * @format
 */

import {AppRegistry} from 'react-native';
// We need to import the native module outside the registerHeadlessTask function to make
// sure the react context bundle it as well during headless task execution.
import BackgroundTimer from 'react-native-background-timer';
import {Socket} from 'phoenix';
import {BackgroundCallBannerModule} from './BackgroundCallBannerModule';
import App from './App1';
import {name as appName} from './app.json';

AppRegistry.registerComponent(appName, () => App);

AppRegistry.registerHeadlessTask(
  'CallNotificationEventEmitter',
  () => async taskData => {
    // Bind to the BackgroundTimer object to preserve the `this` object in its own implementation
    global.setTimeout = BackgroundTimer.setTimeout.bind(BackgroundTimer);
    global.clearTimeout = BackgroundTimer.clearTimeout.bind(BackgroundTimer);
    global.setInterval = BackgroundTimer.setInterval.bind(BackgroundTimer);
    console.log('setTimeout: ', typeof BackgroundTimer.setTimeout);
    console.log('start: ', typeof BackgroundTimer.start);
    console.log('websocket: ', typeof WebSocket.name);
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
      BackgroundCallBannerModule.startCallBanner({});
    } catch (error) {
      console.log(`Error trying to run headless task: ${error.message}`);
    }
    return new Promise(resolve => {
      const socket = new Socket('ws://localhost:9898');
      socket.onMessage(msg => console.log('Got message'));
      socket.onOpen(() => {
        console.log('WebSocket connected, sending Q1');
        socket.push('Q1');
      });
      socket.connect();
      const timeoutId = setTimeout(() => {
        socket.disconnect(() => {
          BackgroundCallBannerModule.stopCallBanner();
          console.log('call banner dismissed');
          resolve();
        });
      }, 40000);
      socket.onClose = event => {
        clearTimeout(timeoutId);
        BackgroundCallBannerModule.stopCallBanner();
        console.log('server closed connection');
        resolve();
      };
    });
  },
);
