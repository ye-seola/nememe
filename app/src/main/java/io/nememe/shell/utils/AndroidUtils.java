package io.nememe.shell.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManagerHidden;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.InstrumentationHidden;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.rikka.tools.refine.Refine;
import io.nememe.shell.android.AEnvironment;

/**
 * @noinspection JavaReflectionMemberAccess
 */
@SuppressLint("BlockedPrivateApi")
public class AndroidUtils {
    public static void startActivity(Intent intent) {
        getStartActivity().apply(intent);
    }

    public static void startService(Intent intent) {
        getStartService().apply(intent);
    }

    private static Function<Intent, Void> getStartActivity() {
        try {
            return (intent) -> {
                try {
                    Context appContext = AEnvironment.getAppContext();

                    InstrumentationHidden ih = Refine.unsafeCast(Objects.requireNonNull(AEnvironment.getMainThread().getInstrumentation()));
                    ih.execStartActivity(appContext, null, null, null, intent, -1, new Bundle());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            };
        } catch (Exception ignored) {
        }

        logFailedToGetMethods("startActivity");
        throw new RuntimeException("startActivity를 찾을 수 없습니다.");
    }

    private static Function<Intent, Void> getStartService() {
        try {
            // IApplicationThread caller, Intent service, String resolvedType,
            // boolean requireForeground, String callingPackage, String callingFeatureId, int userId
            Method m = IActivityManager.class.getDeclaredMethod("startService",
                    IApplicationThread.class,
                    Intent.class,
                    String.class,
                    Boolean.TYPE,
                    String.class,
                    String.class,
                    Integer.TYPE
            );

            return (intent) -> {
                try {
                    IActivityManager am = ActivityManagerHidden.getService();
                    m.invoke(
                            am, null, intent, null, false, AEnvironment.PACKAGE_NAME, null, -3
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            };
        } catch (Exception ignored) {
        }

        try {
            // IApplicationThread caller, Intent service, String resolvedType,
            // boolean requireForeground, in String callingPackage, int userId
            Method m = IActivityManager.class.getDeclaredMethod("startService",
                    IApplicationThread.class,
                    Intent.class,
                    String.class,
                    Boolean.TYPE,
                    String.class,
                    Integer.TYPE
            );

            return (intent) -> {
                try {
                    IActivityManager am = ActivityManagerHidden.getService();
                    m.invoke(
                            am, null, intent, null, false, AEnvironment.PACKAGE_NAME, -3
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            };
        } catch (Exception ignored) {
        }

        logFailedToGetMethods("startService");
        throw new RuntimeException("startService를 찾을 수 없습니다.");
    }

    private static void logFailedToGetMethods(String methodName) {
        System.out.printf("%s를 찾을 수 없습니다\nSDK 버전: %d\n", methodName, Build.VERSION.SDK_INT);
        System.out.println(getMethodsByName(methodName));
    }

    private static String getMethodsByName(String methodName) {
        return Arrays.stream(IActivityManager.class.getMethods())
                .map(Method::toString)
                .map(String::trim)
                .filter(m -> m.contains(methodName))
                .collect(Collectors.joining("\n"));
    }
}
