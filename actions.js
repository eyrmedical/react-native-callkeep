import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const EYRCallKeepModule = NativeModules.EYRCallKeep;
const eventEmitter = new NativeEventEmitter(EYRCallKeepModule);

const EYRCallKeepPerformAnswerCallAction = 'EYRCallKeepPerformAnswerCallAction';
const EYRCallKeepPerformEndCallAction = 'EYRCallKeepPerformEndCallAction';
const EYRCallKeepDidPerformSetMutedCallAction = 'EYRCallKeepDidPerformSetMutedCallAction';
const EYRCallKeepDidLoadWithEvents = 'EYRCallKeepDidLoadWithEvents';

const isIOS = Platform.OS === 'ios';

const answerCall = handler => eventEmitter.addListener(EYRCallKeepPerformAnswerCallAction, handler);

const endCall = handler => eventEmitter.addListener(EYRCallKeepPerformEndCallAction, handler);

const didPerformSetMutedCallAction = handler => eventEmitter.addListener(EYRCallKeepDidPerformSetMutedCallAction, handler);

const didLoadWithEvents = handler => eventEmitter.addListener(EYRCallKeepDidLoadWithEvents, handler);

export const emit = (eventName, payload) => eventEmitter.emit(eventName, payload);

export const listeners = {
  answerCall,
  endCall,
  didPerformSetMutedCallAction,
  didLoadWithEvents,
};
