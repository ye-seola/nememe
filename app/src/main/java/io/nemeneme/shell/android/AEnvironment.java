package io.nemeneme.shell.android;

import android.app.ActivityThread;
import android.content.Context;

import java.util.Objects;

import dev.rikka.tools.refine.Refine;

public class AEnvironment {
    public static final String PACKAGE_NAME = "com.nemeneme";
    private static ActivityThread mainThread;
    private static Context appContext;

    public static ActivityThread getMainThread() {
        if (mainThread == null) {
            mainThread = ActivityThread.systemMain();
        }
        return Objects.requireNonNull(mainThread);
    }

    public static Context getAppContext() {
        if (appContext == null) {
            Context sContext = Refine.unsafeCast(getMainThread().getSystemContext());

            try {
                appContext = sContext.createPackageContext(
                        AEnvironment.PACKAGE_NAME,
                        Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to create app context", e);
            }
        }
        return appContext;
    }
}