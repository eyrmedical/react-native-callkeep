package com.callkeepdemo.call;

import androidx.annotation.NonNull;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BackgroundCallBannerPackage implements ReactPackage {
    @NonNull
    @NotNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull @NotNull ReactApplicationContext reactContext) {
        return Arrays.asList(new BackgroundCallBannerModule(reactContext));
    }

    @NonNull
    @NotNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull @NotNull ReactApplicationContext reactContext) {
        return new ArrayList<>();
    }
}
