declare module 'react-native-callkeep' {
  export type Events =
    'answerCall' |
    'endCall' |
    'didActivateAudioSession' |
    'didPerformSetMutedCallAction' |
    'didLoadWithEvents' |;

  type HandleType = 'generic' | 'number' | 'email';

  export type AudioRoute = {
    name: string,
    type: string
  }

  interface IOptions {
    ios: {
      appName: string,
      imageName?: string,
      supportsVideo?: boolean,
      maximumCallGroups?: string,
      maximumCallsPerCallGroup?: string,
      ringtoneSound?: string,
      includesCallsInRecents?: boolean
    },
    android: {
      alertTitle: string,
      alertDescription: string,
      cancelButton: string,
      okButton: string,
      imageName?: string,
      additionalPermissions: string[],
      selfManaged?: boolean,
      foregroundService?: {
        channelId: string,
        channelName: string,
        notificationTitle: string,
        notificationIcon?: string
      }
    }
  }

  export type DidReceiveStartCallActionPayload = { handle: string };
  export type AnswerCallPayload = { callUUID: string };
  export type EndCallPayload = AnswerCallPayload;
  export type DidDisplayIncomingCallPayload = string | undefined;
  export type DidPerformSetMutedCallActionPayload = boolean;

  export const CONSTANTS: {
    END_CALL_REASONS: {
      FAILED: 1,
      REMOTE_ENDED: 2,
      UNANSWERED: 3,
      ANSWERED_ELSEWHERE: 4,
      DECLINED_ELSEWHERE: 5 | 2,
      MISSED: 2 | 6
    }
  };

  export default class RNCallKeep {
    static getInitialEvents(): Promise<Array<Object>>

    static addEventListener(type: Events, handler: (args: any) => void): void

    static removeEventListener(type: Events): void

    static fulfillAnswerCallAction(): void

    static reportEndCallWithUUID(uuid: string, reason: number): void

    static rejectCall(uuid: string): void

    static endCall(uuid: string): void

    static fulfillEndCallAction(): void

    /**
     * @description setMutedCall method is available only on iOS.
     */
    static setMutedCall(uuid: string, muted: boolean): void

  }
}
