package io.nememe.app.receiver;

import static androidx.core.content.ContextCompat.startForegroundService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.preference.PreferenceManager;

import java.util.Objects;

import io.nememe.app.service.ShellService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            var mgr = PreferenceManager.getDefaultSharedPreferences(context);
            if (mgr.getBoolean("autostart", true)) {
                Intent i = new Intent(context, ShellService.class);
                startForegroundService(context, i);
            }
        }
    }
}
