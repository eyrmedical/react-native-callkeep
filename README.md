# React Native CallKeep

[![npm version](https://badge.fury.io/js/react-native-callkeep.svg)](https://badge.fury.io/js/react-native-callkeep)
[![npm downloads](https://img.shields.io/npm/dm/react-native-callkeep.svg?maxAge=2592000)](https://img.shields.io/npm/dm/react-native-callkeep.svg?maxAge=2592000)

**React Native CallKeep** utilises a brand new iOS 10 framework **CallKit** and Android **ConnectionService** to make the life easier for VoIP developers using React Native.

For more information about **CallKit** on iOS, please see [Official CallKit Framework Document](https://developer.apple.com/reference/callkit?language=objc) or [Introduction to CallKit by Xamarin](https://developer.xamarin.com/guides/ios/platform_features/introduction-to-ios10/callkit/)

For more information about **ConnectionService** on Android, please see [Android Documentation](https://developer.android.com/reference/android/telecom/ConnectionService) and [Build a calling app](https://developer.android.com/guide/topics/connectivity/telecom/selfManaged)

⚠️ **CallKit** and **ConnectionService** are only available on real devices, this library will not work on simulators.

#  Summary
- [Demo](#Demo)
- [Installation](#Installation)
- [Usage](#Usage)
  - [Constants](#Constants)
  - [Android Self Managed](#Android-Self-Managed-Mode)
  - [API](#Api)
  - [Example](##Example)
- [PushKit](#PushKit)
- [Android 11](#Android-11)
- [Debug](#Debug)
- [Troubleshooting](#Troubleshooting)
- [Contributing](#Contributing)
- [License](#License)


# Demo
A demo of `react-native-callkeep` is available in the [wazo-react-native-demo](https://github.com/wazo-pbx/wazo-react-native-demo) repository.

#### Android
![Connection Service](docs/pictures/connection-service.jpg)

#### iOS
![Connection Service](docs/pictures/call-kit.png)

# Installation
```sh
npm install --save react-native-callkeep
# or
yarn add react-native-callkeep
```

- [iOS](docs/ios-installation.md)
- [Android](docs/android-installation.md)

# Usage

#### Setup

```js
import RNCallKeep from 'react-native-callkeep';

const options = {
  ios: {
    appName: 'My app name',
  },
  android: {
    alertTitle: 'Permissions required',
    alertDescription: 'This application needs to access your phone accounts',
    cancelButton: 'Cancel',
    okButton: 'ok',
    imageName: 'phone_account_icon',
    additionalPermissions: [PermissionsAndroid.PERMISSIONS.example],
    // Required to get audio in background when using Android 11
    foregroundService: {
      channelId: 'com.company.my',
      channelName: 'Foreground service for my app',
      notificationTitle: 'My app is running on background',
      notificationIcon: 'Path to the resource icon of the notification',
    }, 
  }
};

RNCallKeep.setup(options).then(accepted => {});
```

iOS only.

Alternative on iOS you can perform setup in `AppDelegate.m`. Doing this allows capturing events prior to the react native event bridge being up. Please be aware that calling setup in `AppDelegate.m` will ignore any subsequent calls to `RNCallKeep.setup();`.

```objective-c
@implementation AppDelegate
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{ 
  self.bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:launchOptions];

  [RNCallKeep setup:@{
    @"appName": @"Awesome App",
    @"maximumCallGroups": @3,
    @"maximumCallsPerCallGroup": @1,
    @"supportsVideo": @NO,
  }];

  RCTRootView *rootView = [[RCTRootView alloc] initWithBridge:self.bridge
                                                   moduleName:@"App"
                                            initialProperties:nil];

  // ======== OTHER CODE REDACTED ==========

  return YES;
}

```

- `options`: Object
  - `ios`: object
    - `appName`: string (required)
      It will be displayed on system UI when incoming calls received
    - `imageName`: string (optional)
      If provided, it will be displayed on system UI during the call
    - `ringtoneSound`: string (optional)
      If provided, it will be played when incoming calls received; the system will use the default ringtone if this is not provided
    - `includesCallsInRecents`: boolean (optional)
      If provided, calls will be shown in the recent calls when true and not when false (ios 11 and above) (Default: true)
    - `maximumCallGroups`: string (optional)
      If provided, the maximum number of call groups supported by this application (Default: 3)
    - `maximumCallsPerCallGroup`: string (optional)
      If provided, the maximum number of calls in a single group, used for conferencing (Default: 1, no conferencing)
    - `supportsVideo`: boolean (optional)
      If provided, whether or not the application supports video calling (Default: true)
  - `android`: object
    - `alertTitle`: string (required)
      When asking for _phone account_ permission, we need to provider a title for the `Alert` to ask the user for it
    - `alertDescription`: string (required)
      When asking for _phone account_ permission, we need to provider a description for the `Alert` to ask the user for it
    - `cancelButton`: string (required)
      Cancel button label
    - `okButton`: string (required)
      Ok button label
    - `imageName`: string (optional)
      The image to use in the Android Phone application's native UI for enabling/disabling calling accounts. Should be a 48x48 HDPI
      grayscale PNG image. Must be in your drawable resources for the parent application. Must be lowercase and underscore (_) characters
      only, as Java doesn't like capital letters on resources.
    - `additionalPermissions`: [PermissionsAndroid] (optional)
      Any additional permissions you'd like your app to have at first launch. Can be used to simplify permission flows and avoid
      multiple popups to the user at different times.
    - `selfManaged`: boolean (optional)
      When set to true, call keep will configure itself to run as a self managed connection service. This is an advanced topic, and it's best to refer to [Googles Documentation](https://developer.android.com/guide/topics/connectivity/telecom/selfManaged) on the matter.
      
`setup` calls internally `registerPhoneAccount` and `registerEvents`.

# Constants

To make passing the right integer into methods easier, there are constants that are exported from the module.

```
const CONSTANTS = {
  END_CALL_REASONS: {
    FAILED: 1,
    REMOTE_ENDED: 2,
    UNANSWERED: 3,
    ANSWERED_ELSEWHERE: 4,
    DECLINED_ELSEWHERE: 5,
    MISSED: 6
  }
};

const { CONSTANTS as CK_CONSTANTS, RNCallKeep } from 'react-native-callkeep';

console.log(CK_CONSTANTS.END_CALL_REASONS.FAILED) // outputs 1
```

# Android Self Managed Mode
_This feature is available only on Android._

Android supports calling apps running in what's called "Self Managed". This means the apps are able (and required) to provide their own UI for managing calls. This includes both in call UI elements and incoming call notification UI. This method is all or nothing. You can't mix partial elements, such as having a custom in call view, but use the default incoming call UI.

To implement a self managed calling app, the following steps are necessary:
- Set `selfManaged: true` in setup.
- On an incoming call, from react native, call `RNCallKeep.displayIncomingCall`
- CallKeep will then fire the `showIncomingCallUi` event.
- When `showIncomingCallUi` is fired, you must show an incoming call UI. This would be a high priority notification ([Android: Display time-sensitive notifications](https://developer.android.com/training/notify-user/time-sensitive)).
- If the user answers the call, you call the appropriate RNCallKeep actions such as `answerCall` or `endCall`

Self Managed calling apps are an advanced topic, and there are many steps involved in implementing them, but here are some things to keep in mind:
- React Native Headless Tasks are a great way to execute React Native code. Remember to start up the headless task as a Foreground Service.
- Android will deprioritize your high priority FCM notifications if you fail to show an incoming call ui when receiving them.
- You can avoid getting flooded with sticky foreground service notifications by not defining a Foreground Service for CallKeep, and instead managing this on your own.

⚠️ To be able to use the self managed mode, you'll have to add the `READ_CALL_LOG` permission in your `android/src/main/AndroidManifest.xml` file:
```
<uses-permission android:name="android.permission.READ_CALL_LOG" />
```

# API

| Method                                                            | Return Type         |  iOS | Android |
| ----------------------------------------------------------------- | ------------------- | :--: | :-----: |
| [getInitialEvents()](#getInitialEvents)                           | `Promise<String[]>` |  ✅  |   ❌    |
| [setAvailable()](#setAvailable)                                   | `Promise<void>`     |  ❌  |   ✅    |
| [setForegroundServiceSettings()](#setForegroundServiceSettings)   | `Promise<void>`     |  ❌  |   ✅    |
| [canMakeMultipleCalls()](#canMakeMultipleCalls)                   | `Promise<void>`     |  ❌  |   ✅    |
| [setCurrentCallActive()](#setCurrentCallActive)                   | `Promise<void>`     |  ❌  |   ✅    |
| [isCallActive()](#isCallActive)                                   | `Promise<Boolean>`  |  ✅  |   ❌    |
| [getCalls()](#getCalls)                                           | `Promise<Object[]>` |  ✅  |   ❌    |
| [displayIncomingCall()](#displayIncomingCall)                     | `Promise<void>`     |  ✅  |   ✅    |
| [answerIncomingCall()](#answerIncomingCall)                       | `Promise<void>`     |  ✅  |   ✅    |
| [startCall()](#startCall)                                         | `Promise<void>`     |  ✅  |   ✅    |
| [updateDisplay()](#updateDisplay)                                 | `Promise<void>`     |  ✅  |   ✅    |
| [endCall()](#endCall)                                             | `Promise<void>`     |  ✅  |   ✅    |
| [endAllCalls()](#endAllCalls)                                     | `Promise<void>`     |  ✅  |   ✅    |
| [rejectCall()](#rejectCall)                                       | `Promise<void>`     |  ✅  |   ✅    |
| [reportEndCallWithUUID()](#reportEndCallWithUUID)                 | `Promise<void>`     |  ✅  |   ✅    |
| [setMutedCall()](#setMutedCall)                                   | `Promise<void>`     |  ✅  |   ✅    |
| [setOnHold()](#setOnHold)                                         | `Promise<void>`     |  ✅  |   ✅    |
| [checkIfBusy()](#checkIfBusy)                                     | `Promise<Boolean>`  |  ✅  |   ❌    |
| [checkSpeaker()](#checkSpeaker)                                   | `Promise<Boolean>`  |  ✅  |   ❌    |
| [toggleAudioRouteSpeaker()](#toggleAudioRouteSpeaker)             | `Promise<void>`     |  ❌  |   ✅    |
| [supportConnectionService()](#supportConnectionService)           | `Promise<Boolean>`  |  ❌  |   ✅    |
| [hasPhoneAccount()](#hasPhoneAccount)                             | `Promise<Boolean>`  |  ❌  |   ✅    |
| [hasOutgoingCall()](#hasOutgoingCall)                             | `Promise<Boolean>`  |  ❌  |   ✅    |
| [hasDefaultPhoneAccount()](#hasDefaultPhoneAccount)               | `Promise<Boolean>`  |  ❌  |   ✅    |
| [checkPhoneAccountEnabled()](#checkPhoneAccountEnabled)           | `Promise<Boolean>`  |  ❌  |   ✅    |
| [isConnectionServiceAvailable()](#isConnectionServiceAvailable)   | `Promise<Boolean>`  |  ❌  |   ✅    |
| [backToForeground()](#backToForeground)                           | `Promise<void>`     |  ❌  |   ✅    |
| [removeEventListener()](#removeEventListener)                     | `void`              |  ✅  |   ✅    |
| [registerPhoneAccount()](#registerPhoneAccount)                   | `void`              |  ❌  |   ✅    |
| [registerAndroidEvents()](#registerAndroidEvents)                 | `void`              |  ❌  |   ✅    |


### getInitialEvents
_This feature is available only on iOS._

If there were some actions performed by user before JS context has been created, this method would return early fired events. This is alternative to "didLoadWithEvents" event.

```js
RNCallKeep.getInitialEvents();
```

### endCall

When finish an incoming/outgoing call.  
(When user actively chooses to end the call from your app's UI.)

```js
RNCallKeep.endCall(uuid);
```

- `uuid`: string
  - The `uuid` used for `startCall` or `displayIncomingCall`

### rejectCall

When you reject an incoming call.

```js
RNCallKeep.rejectCall(uuid);
```

- `uuid`: string
  - The `uuid` used for `startCall` or `displayIncomingCall`

### reportEndCallWithUUID

Report that the call ended without the user initiating.  
(Not ended by user, is usually due to the following reasons)


```js
RNCallKeep.reportEndCallWithUUID(uuid, reason);
```

- `uuid`: string
  - The `uuid` used for `startCall` or `displayIncomingCall`
- `reason`: int
  - Reason for the end call
    - Call failed: 1
    - Remote user ended call: 2
    - Remote user did not answer: 3
    - Call Answered elsewhere: 4
    - Call declined elsewhere: 5 (on Android this will map to Remote user ended call if you use the constants)
    - Missed: 6 (on iOS this will map to remote user ended call)
  - Access reasons as constants
  ```js
  const { CONSTANTS as CK_CONSTANTS, RNCallKeep } from 'react-native-callkeep';

  RNCallKeep.reportEndCallWithUUID(uuid, CK_CONSTANTS.END_CALL_REASONS.FAILED);
  ```

### setMutedCall

Switch the mic on/off.

```js
RNCallKeep.setMutedCall(uuid, true);
```

- `uuid`: string
  - uuid of the current call.
- `muted`: boolean

### removeEventListener

Allows to remove the listener on an event.

```js
RNCallKeep.removeEventListener('checkReachability');
```


## Events

| Event                                                             |  iOS | Android |
| ----------------------------------------------------------------- | :--: | :-----: |
| [endCall()](#endCall)                                             |  ✅  |   ✅    |
| [didActivateAudioSession()](#didActivateAudioSession)             |  ✅  |   ✅    |
| [didPerformSetMutedCallAction()](#didPerformSetMutedCallAction)   |  ✅  |   ✅    |
| [didLoadWithEvents()](#didLoadWithEvents)                         |  ✅  |   ✅    |

### - endCall

User finish the call.

```js
RNCallKeep.addEventListener('endCall', ({ callUUID }) => {
  // Do your normal `Hang Up` actions here
});
```

- `callUUID` (string)
  - The UUID of the call that is to be ended.

### - didActivateAudioSession

The `AudioSession` has been activated by **RNCallKeep**.

```js
RNCallKeep.addEventListener('didActivateAudioSession', () => {
  // you might want to do following things when receiving this event:
  // - Start playing ringback if it is an outgoing call
});
```

### - didPerformSetMutedCallAction

A call was muted by the system or the user:

```js
RNCallKeep.addEventListener('didPerformSetMutedCallAction', ({ muted, callUUID }) => {

});
```

- `muted` (boolean)
- `callUUID` (string)
  - The UUID of the call.

### - didLoadWithEvents

iOS only.

Called as soon as JS context initializes if there were some actions performed by user before JS context has been created.

Since iOS 13, you must display incoming call on receiving PushKit push notification. But if app was killed, it takes some time to create JS context. If user answers the call (or ends it) before JS context has been initialized, user actions will be passed as events array of this event. Similar situation can happen if user would like to start a call from Recents or similar iOS app, assuming that your app was in killed state.

In order for this event to reliably fire, it's necessary to perform setup in `AppDelegate.m`

**NOTE: You still need to subscribe / handle the rest events as usuall. This is just a helper whcih cache and propagate early fired events if and only if for "the native events which DID fire BEFORE js bridge is initialed", it does NOT mean this will have events each time when the app reopened.**

```js
// register `didLoadWithEvents` somewhere early in your app when it is ready to handle callkeep events.

RNCallKeep.addEventListener('didLoadWithEvents', (events) => {
  // `events` is passed as an Array chronologically, handle or ignore events based on the app's logic
  // see example usage in https://github.com/react-native-webrtc/react-native-callkeep/pull/169 or https://github.com/react-native-webrtc/react-native-callkeep/pull/205
});
```

- `events` Array
  - `name`: string
    Native event name like: `RNCallKeepPerformAnswerCallAction`
  - `data`: object
    Object with data passed together with specific event so it can be handled in the same way like original event, for example `({ callUUID })` for `answerCall` event if `name` is `RNCallKeepPerformAnswerCallAction`

## Example

A full example is available in the [example](https://github.com/react-native-webrtc/react-native-callkeep/tree/master/example) folder.

```javascript
import React from 'react';
import RNCallKeep from 'react-native-callkeep';
import uuid from 'uuid';

class RNCallKeepExample extends React.Component {
  constructor(props) {
    super(props);

    this.currentCallId = null;

    // Add RNCallKeep Events
    RNCallKeep.addEventListener('didReceiveStartCallAction', this.didReceiveStartCallAction);
    RNCallKeep.addEventListener('answerCall', this.onAnswerCallAction);
    RNCallKeep.addEventListener('endCall', this.onEndCallAction);
    RNCallKeep.addEventListener('didDisplayIncomingCall', this.onIncomingCallDisplayed);
    RNCallKeep.addEventListener('didPerformSetMutedCallAction', this.onToggleMute);
    RNCallKeep.addEventListener('didToggleHoldCallAction', this.onToggleHold);
    RNCallKeep.addEventListener('didPerformDTMFAction', this.onDTMFAction);
    RNCallKeep.addEventListener('didActivateAudioSession', this.audioSessionActivated);
  }

  // Initialise RNCallKeep
  setup = () => {
    const options = {
      ios: {
        appName: 'ReactNativeWazoDemo',
        imageName: 'sim_icon',
        supportsVideo: false,
        maximumCallGroups: '1',
        maximumCallsPerCallGroup: '1'
      },
      android: {
        alertTitle: 'Permissions Required',
        alertDescription:
          'This application needs to access your phone calling accounts to make calls',
        cancelButton: 'Cancel',
        okButton: 'ok',
        imageName: 'sim_icon',
        additionalPermissions: [PermissionsAndroid.PERMISSIONS.READ_CONTACTS]
      }
    };

    try {
      RNCallKeep.setup(options);
      RNCallKeep.setAvailable(true); // Only used for Android, see doc above.
    } catch (err) {
      console.error('initializeCallKeep error:', err.message);
    }
  }

  // Use startCall to ask the system to start a call - Initiate an outgoing call from this point
  startCall = ({ handle, localizedCallerName }) => {
    // Your normal start call action
    RNCallKeep.startCall(this.getCurrentCallId(), handle, localizedCallerName);
  };

  reportEndCallWithUUID = (callUUID, reason) => {
    RNCallKeep.reportEndCallWithUUID(callUUID, reason);
  }

  // Event Listener Callbacks

  didReceiveStartCallAction = (data) => {
    let { handle, callUUID, name } = data;
    // Get this event after the system decides you can start a call
    // You can now start a call from within your app
  };

  onAnswerCallAction = (data) => {
    let { callUUID } = data;
    // Called when the user answers an incoming call
  };

  onEndCallAction = (data) => {
    let { callUUID } = data;
    RNCallKeep.endCall(this.getCurrentCallId());

    this.currentCallId = null;
  };

  // Currently iOS only
  onIncomingCallDisplayed = (data) => {
    let { error } = data;
    // You will get this event after RNCallKeep finishes showing incoming call UI
    // You can check if there was an error while displaying
  };

  onToggleMute = (data) => {
    let { muted, callUUID } = data;
    // Called when the system or user mutes a call
  };

  onToggleHold = (data) => {
    let { hold, callUUID } = data;
    // Called when the system or user holds a call
  };

  onDTMFAction = (data) => {
    let { digits, callUUID } = data;
    // Called when the system or user performs a DTMF action
  };

  audioSessionActivated = (data) => {
    // you might want to do following things when receiving this event:
    // - Start playing ringback if it is an outgoing call
  };

  getCurrentCallId = () => {
    if (!this.currentCallId) {
      this.currentCallId = uuid.v4();
    }

    return this.currentCallId;
  };

  render() {
  }
}
```

## Receiving a call when the application is not reachable.

In some case your application can be unreachable :
- when the user kill the application
- when it's in background since a long time (eg: after ~5mn the os will kill all connections).

To be able to wake up your application to display the incoming call, you can use [https://github.com/react-native-webrtc/react-native-voip-push-notification](react-native-voip-push-notification) on iOS or BackgroundMessaging from [react-native-firebase](https://rnfirebase.io/messaging/usage#receiving-messages)-(Optional)(Android-only)-Listen-for-FCM-messages-in-the-background).

You have to send a push to your application, like with Firebase for Android and with a library supporting PushKit pushes for iOS.

## PushKit

Since iOS 13, you'll have to report the incoming calls that wakes up your application with a VoIP push. Add this in your `AppDelegate.m` if you're using VoIP pushes to wake up your application :

```objective-c
- (void)pushRegistry:(PKPushRegistry *)registry didReceiveIncomingPushWithPayload:(PKPushPayload *)payload forType:(PKPushType)type withCompletionHandler:(void (^)(void))completion {
  // Process the received push
  [RNVoipPushNotificationManager didReceiveIncomingPushWithPayload:payload forType:(NSString *)type];

  // Retrieve information like handle and callerName here
  // NSString *uuid = /* fetch for payload or ... */ [[[NSUUID UUID] UUIDString] lowercaseString];
  // NSString *callerName = @"caller name here";
  // NSString *handle = @"caller number here";
  // NSDictionary *extra = [payload.dictionaryPayload valueForKeyPath:@"custom.path.to.data"]; /* use this to pass any special data (ie. from your notification) down to RN. Can also be `nil` */

  [RNCallKeep reportNewIncomingCall: uuid
                             handle: handle
                         handleType: @"generic"
                           hasVideo: NO
                localizedCallerName: callerName
                    supportsHolding: YES
                       supportsDTMF: YES
                   supportsGrouping: YES
                 supportsUngrouping: YES
                        fromPushKit: YES
                            payload: extra
              withCompletionHandler: completion];
}
```

## Android 11

Since Android 11, your application [requires to start a foregroundService](https://developer.android.com/about/versions/11/privacy/foreground-services) in order to access the microphone in background.
You'll need to upgrade your `compileSdkVersion` to `30` to be able to use this feature.

You have to set the `foregroundService` key in the [`setup()`](#setup) method and add a `foregroundServiceType` in the [`AndroidManifest` file](docs/android-installation.md#android-common-step-installation).

## Debug

### Android

```
adb logcat *:S RNCallKeep:V
```

## Troubleshooting
- Ensure that you construct a valid `uuid` by importing the `uuid` library and running `uuid.v4()` as shown in the examples. If you don't do this and use a custom string, the incoming call screen will never be shown on iOS.

## Contributing

Any pull request, issue report and suggestion are highly welcome!

## License

This work is dual-licensed under ISC and MIT.
Previous work done by @ianlin on iOS is on ISC Licence.
We choose MIT for the rest of the project.

`SPDX-License-Identifier: ISC OR MIT`
