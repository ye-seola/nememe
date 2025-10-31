package io.nemeneme.app.ui.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.nemeneme.R;
import io.nemeneme.app.log.LogManager;
import io.nemeneme.app.service.ShellService;
import io.nemeneme.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements LogManager.LogListener {
    private static final int NOTIFICATION_PERMISSION_REQUEST = 100;
    private ActivityMainBinding binding;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binding.onoffButton.setChecked(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binding.onoffButton.setChecked(false);

            binding.getRoot().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, ShellService.class);
                bindService(intent, connection, 0);
            }, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.myToolbar);

        binding.onoffButton.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                requestNotificationPermission();
            } else {
                stopShellService();
            }
        });

        binding.clearButton.setOnClickListener(v -> LogManager.getInstance().clear());

        binding.console.setText(LogManager.getInstance().getFullLog());
        scrollToBottom();

        Intent intent = new Intent(MainActivity.this, ShellService.class);
        bindService(intent, connection, 0);
    }

    private void scrollToBottom() {
        ScrollView scrollView = binding.consoleScrollview;
        View content = scrollView.getChildAt(0);
        if (content != null) {
            int diff = content.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
            if (diff <= 10) {
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST
                );
            } else {
                startShellService();
            }
        } else {
            startShellService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogManager.getInstance().addListener(this);

        binding.console.setText(LogManager.getInstance().getFullLog());
        scrollToBottom();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogManager.getInstance().removeListener(this);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startShellService();
            } else {
                LogManager.getInstance().addLog("알림 권한이 거부되었습니다");
            }
        }
    }

    private void startShellService() {
        Intent intent = new Intent(this, ShellService.class);
        startForegroundService(intent);
    }

    private void stopShellService() {
        Intent intent = new Intent(this, ShellService.class);
        stopService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_setting) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onLogUpdate(String log) {
        logQueue.add(log);
        mainHandler.post(this::processNextLog);
    }

    private void processNextLog() {
        String log;
        while ((log = logQueue.poll()) != null) {
            binding.console.setText(log);
        }
        scrollToBottom();
    }
}