package com.callkeepdemo.call;

import android.app.Application;
import androidx.annotation.NonNull;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BackgroundCallBannerPackage implements ReactPackage {

    private Application application;

    public BackgroundCallBannerPackage(Application application) {
        super();
        this.application = application;
    }

    @NonNull
    @NotNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull @NotNull ReactApplicationContext reactContext) {
        return Arrays.asList(new BackgroundCallBannerModule(reactContext, this.application));
    }

    @NonNull
    @NotNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull @NotNull ReactApplicationContext reactContext) {
        return new ArrayList<>();
    }
}
