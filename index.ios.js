import { NativeModules, NativeEventEmitter } from 'react-native';

const RNCallKeepModule = NativeModules.RNCallKeep;
const eventEmitter = new NativeEventEmitter(RNCallKeepModule);

export const emit = eventEmitter.emit.bind(eventEmitter);

const listeners = {
    answerCall: handler => eventEmitter.addListener('RNCallKeepPerformAnswerCallAction', handler),
    endCall: handler => eventEmitter.addListener('RNCallKeepPerformEndCallAction', handler),
    didPerformSetMutedCallAction: handler => eventEmitter.addListener('RNCallKeepDidPerformSetMutedCallAction', handler),
    didLoadWithEvents: handler => eventEmitter.addListener('RNCallKeepDidLoadWithEvents', handler),
};

export const CONSTANTS = {
    END_CALL_REASONS: {
        FAILED: 1,
        REMOTE_ENDED: 2,
        UNANSWERED: 3,
        ANSWERED_ELSEWHERE: 4,
        DECLINED_ELSEWHERE: 5,
        MISSED: 2,
    },
};

class RNCallKeep {
    constructor() {
        this._callkeepEventHandlers = new Map();
    }

    setMutedCall = (uuid, shouldMute) =>
        RNCallKeepModule.setMutedCall(uuid, shouldMute);

    fulfillAnswerCallAction = () => {
        RNCallKeepModule.fulfillAnswerCallAction();
    };

    endCall = (uuid) => RNCallKeepModule.endCall(uuid);

    reportEndCallWithUUID = (uuid, reason) =>
        RNCallKeepModule.reportEndCallWithUUID(uuid, reason);

    fulfillEndCallAction = () => {
        RNCallKeepModule.fulfillEndCallAction();
    };

    getInitialEvents = () => RNCallKeepModule.getInitialEvents();

    addEventListener = (type, handler) => {
        this._callkeepEventHandlers.set(type, listeners[type](handler));
    };

    removeEventListener = (type) => {
        const listener = this._callkeepEventHandlers.get(type);
        if (!listener) {
            return;
        }
        listener.remove();
        this._callkeepEventHandlers.delete(type);
    };
}

export default new RNCallKeep();
