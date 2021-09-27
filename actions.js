import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const EYRCallKeepModule = NativeModules.EYRCallKeep;
const eventEmitter = new NativeEventEmitter(EYRCallKeepModule);

const EYRCallKeepPerformAnswerCallAction = 'EYRCallKeepPerformAnswerCallAction';
const EYRCallKeepPerformEndCallAction = 'EYRCallKeepPerformEndCallAction';
const EYRCallKeepDidPerformSetMutedCallAction = 'EYRCallKeepDidPerformSetMutedCallAction';
const EYRCallKeepDidLoadWithEvents = 'EYRCallKeepDidLoadWithEvents';

const isIOS = Platform.OS === 'ios';

const answerCall = handler => eventEmitter.addListener(EYRCallKeepPerformAnswerCallAction, function (data) {
  return handler(data);
});

const endCall = handler => eventEmitter.addListener(EYRCallKeepPerformEndCallAction, function (data) {
  return handler(data);
});

const didPerformSetMutedCallAction = handler => eventEmitter.addListener(EYRCallKeepDidPerformSetMutedCallAction, function (data) {
  return handler(data);
});

const didLoadWithEvents = handler => eventEmitter.addListener(EYRCallKeepDidLoadWithEvents, function (data) {
  return handler(data);
});

export const emit = (eventName, payload) => eventEmitter.emit(eventName, payload);

export const listeners = {
  answerCall,
  endCall,
  didPerformSetMutedCallAction,
  didLoadWithEvents,
};
