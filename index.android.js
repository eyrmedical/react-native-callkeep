import { NativeModules, NativeEventEmitter } from 'react-native';

const CallKeepModule = NativeModules.CallKeepModule;
const eventEmitter = new NativeEventEmitter(CallKeepModule);

export const CONSTANTS = CallKeepModule.getConstants();

export const emit = eventEmitter.emit.bind(eventEmitter);
export const addEventListener = eventEmitter.addListener.bind(eventEmitter);
export default CallKeepModule;
