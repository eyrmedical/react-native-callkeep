import {NativeModules} from 'react-native';

const RNBackgroundCallBannerModule = NativeModules.BackgroundCallBannerModule;

const {CALL_INCOMING_CHANNEL_ID} = RNBackgroundCallBannerModule.getConstants();

export const BackgroundCallBannerModule = {
  CALL_INCOMING_CHANNEL_ID,
  startCallBanner: RNBackgroundCallBannerModule.startCallBanner,
  stopCallBanner: RNBackgroundCallBannerModule.stopCallBanner,
};
