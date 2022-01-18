import { NativeModules, NativeEventEmitter } from 'react-native';

const EYRCallKeepModule = NativeModules.EYRCallKeep;
const eventEmitter = new NativeEventEmitter(EYRCallKeepModule);

export const emit = eventEmitter.emit.bind(eventEmitter);

const listeners = {
    answerCall: handler => eventEmitter.addListener('EYRCallKeepPerformAnswerCallAction', handler),
    endCall: handler => eventEmitter.addListener('EYRCallKeepPerformEndCallAction', handler),
    didPerformSetMutedCallAction: handler => eventEmitter.addListener('EYRCallKeepDidPerformSetMutedCallAction', handler),
    didLoadWithEvents: handler => eventEmitter.addListener('EYRCallKeepDidLoadWithEvents', handler),
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
        EYRCallKeepModule.setMutedCall(uuid, shouldMute);

    fulfillAnswerCallAction = () => {
        EYRCallKeepModule.fulfillAnswerCallAction();
    };

    endCall = (uuid) => EYRCallKeepModule.endCall(uuid);

    reportEndCallWithUUID = (uuid, reason) =>
        EYRCallKeepModule.reportEndCallWithUUID(uuid, reason);

    fulfillEndCallAction = () => {
        EYRCallKeepModule.fulfillEndCallAction();
    };

    getInitialEvents = () => EYRCallKeepModule.getInitialEvents();

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
