package com.eyr.callkeep;

import android.app.Application;
import androidx.annotation.NonNull;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EyrCallkeepPackage implements ReactPackage {

    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        ArrayList<NativeModule> modules = new ArrayList<>();
        modules.add(new EyrCallBannerControllerModule(reactContext));
        return modules;
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return new ArrayList<>();
    }
}
