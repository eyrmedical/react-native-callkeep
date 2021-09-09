
import { NativeModules, Platform, Alert } from 'react-native';

import { listeners, emit } from './actions';

const EYRCallKeepModule = NativeModules.EYRCallKeep;
const isIOS = Platform.OS === 'ios';
const supportConnectionService = !isIOS && Platform.Version >= 23;

const CONSTANTS = {
  END_CALL_REASONS: {
    FAILED: 1,
    REMOTE_ENDED: 2,
    UNANSWERED: 3,
    ANSWERED_ELSEWHERE: 4,
    DECLINED_ELSEWHERE: isIOS ? 5 : 2, // make declined elsewhere link to "Remote ended" on android because that's kinda true
    MISSED: isIOS ? 2 : 6,
  },
};

export { emit, CONSTANTS };

class RNCallKeep {
  constructor() {
    this._callkeepEventHandlers = new Map();
  }

  addEventListener = (type, handler) => {
    const listener = listeners[type](handler);

    this._callkeepEventHandlers.set(type, listener);
  };

  removeEventListener = (type) => {
    const listener = this._callkeepEventHandlers.get(type);
    if (!listener) {
      return;
    }

    listener.remove();
    this._callkeepEventHandlers.delete(type);
  };

  fulfillAnswerCallAction = () => {
      EYRCallKeepModule.fulfillAnswerCallAction();
  }
    
  reportEndCall = (uuid, reason) => EYRCallKeepModule.reportEndCall(uuid, reason);
    
  endCall = (uuid) => EYRCallKeepModule.endCall(uuid);
      
  fulfillEndCallAction = () => {
     EYRCallKeepModule.fulfillEndCallAction();
  }
    
  /*
   * Android explicitly states we reject a call
   * On iOS we just notify of an endCall
   */
  rejectCall = (uuid) => {
      EYRCallKeepModule.endCall(uuid);
  };

  setMutedCall = (uuid, shouldMute) => {
    EYRCallKeepModule.setMutedCall(uuid, shouldMute);
  };

  getInitialEvents() {

      return EYRCallKeepModule.getInitialEvents()
  }
}

export default new RNCallKeep();


