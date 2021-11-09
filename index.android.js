import {NativeModules} from 'react-native';

const RNBackgroundCallBannerModule = NativeModules.BackgroundCallBannerModule;

export const CONSTANTS = RNBackgroundCallBannerModule.getConstants();
export const { startCallBanner, stopCallBanner } = RNBackgroundCallBannerModule;
