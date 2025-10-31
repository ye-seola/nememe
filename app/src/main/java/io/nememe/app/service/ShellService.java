package io.nememe.app.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import io.nememe.app.App;
import io.nememe.app.log.LogManager;
import io.nememe.app.ui.activity.MainActivity;

public class ShellService extends Service {
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "ShellService";
    public static final String ACTION_STOP = "io.nememe.shell.STOP_SERVICE";
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    private Thread serverThread;
    private Process process = null;


    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(""));

        acquireLocks();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(""));

        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        updateNotification("실행 중");
        startServer();

        return START_STICKY;
    }

    private void updateNotification(String status) {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, createNotification(status));
    }

    private void startServer() {
        LogManager.getInstance().addLog("서버 시작");

        serverThread = new Thread(() -> {
            process = null;

            try {
                ApplicationInfo appInfo;
                try {
                    appInfo = App.getContext().getPackageManager().getApplicationInfo(App.getContext().getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }

                String apkPath = appInfo.sourceDir;

                ProcessBuilder pb = new ProcessBuilder("su", "-c", String.format("/system/bin/app_process -cp %s / --nice-name=nememe_shell io.nememe.shell.Main", apkPath));
                pb.redirectErrorStream(true);
                process = pb.start();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );

                String line;
                while ((line = reader.readLine()) != null) {
                    LogManager.getInstance().addLog(line);
                }
            } catch (Exception e) {
                LogManager.getInstance().addLog("오류: " + e.getMessage());
            } finally {
                if (process != null) {
                    process.destroyForcibly();

                    try {
                        process.waitFor(5, TimeUnit.SECONDS);
                    } catch (InterruptedException ignored) {
                    }

                    process = null;
                }

                serverThread = null;
                stopSelf();
            }
        });
        serverThread.start();
    }


    private void stopServer() {
        if (process != null) {
            try {
                process.getOutputStream().close();
            } catch (IOException ignored) {
            }
        }

        if (serverThread != null) {
            serverThread.interrupt();
            try {
                serverThread.join(5000);
            } catch (InterruptedException ignored) {
            }
            serverThread = null;
        }

        LogManager.getInstance().addLog("서버 중지됨");
    }

    @Override
    public void onDestroy() {
        stopServer();
        releaseLocks();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** @noinspection deprecation*/
    @SuppressLint("WakelockTimeout")
    private void acquireLocks() {
        // Wake Lock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK,
                "ServerService::WakeLock"
        );
        wakeLock.acquire();

        // WiFi Lock
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(
                WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                "ServerService::WifiLock"
        );
        wifiLock.acquire();
    }

    private void releaseLocks() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock = null;

        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
        wifiLock = null;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Nememe 서비스",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Nememe가 백그라운드에서 실행 중입니다");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private Notification createNotification(String status) {
        Intent stopIntent = new Intent(this, ShellService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                this,
                0,
                mainIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Nememe")
                .setContentText(status)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setContentIntent(mainPendingIntent)
                .addAction(
                        android.R.drawable.ic_delete,
                        "중지",
                        stopPendingIntent
                )
                .build();
    }
}
