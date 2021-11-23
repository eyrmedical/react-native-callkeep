import { NativeModules, NativeEventEmitter } from 'react-native';

const EYRCallKeepModule = NativeModules.EyrCallBannerControllerModule;
const eventEmitter = new NativeEventEmitter(EYRCallKeepModule);

export const CONSTANTS = EYRCallKeepModule.getConstants();

export const emit = eventEmitter.emit.bind(eventEmitter);
export const addEventListener = eventEmitter.addListener.bind(eventEmitter);
export default EYRCallKeepModule;