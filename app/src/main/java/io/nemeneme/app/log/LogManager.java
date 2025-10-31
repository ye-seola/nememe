package io.nemeneme.app.log;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.nemeneme.app.App;

public class LogManager {
    private static final int MAX_LOG_SIZE = 0xFFFF;
    private static final String LOG_FILE_NAME = "service_log.txt";
    private static volatile LogManager instance;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final StringBuilder logBuffer = new StringBuilder();
    private final List<LogListener> listeners = new CopyOnWriteArrayList<>();

    public interface LogListener {
        void onLogUpdate(String log);
    }

    private LogManager() {
        loadLogsFromFile();
    }

    public static LogManager getInstance() {
        if (instance == null) {
            synchronized (LogManager.class) {
                if (instance == null) instance = new LogManager();
            }
        }
        return instance;
    }

    public synchronized void addLog(String message) {
        logBuffer.append(message).append("\n");
        trimToMaxSize();
        saveLogsAsync();
        notifyListeners();
    }

    public synchronized String getFullLog() {
        return logBuffer.toString();
    }

    public void addListener(LogListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LogListener listener) {
        listeners.remove(listener);
    }

    public synchronized void clear() {
        logBuffer.setLength(0);
        saveLogsAsync();
        notifyListeners();
    }

    private void notifyListeners() {
        String logCopy;
        synchronized (this) {
            logCopy = logBuffer.toString();
        }
        for (LogListener listener : listeners) listener.onLogUpdate(logCopy);
    }

    private void saveLogsAsync() {
        final String content = getFullLog();
        executor.execute(() -> {
            File file = new File(App.getContext().getFilesDir(), LOG_FILE_NAME);
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadLogsFromFile() {
        File file = new File(App.getContext().getFilesDir(), LOG_FILE_NAME);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[(int) file.length()];
            reader.read(buffer);
            synchronized (this) {
                logBuffer.setLength(0);
                logBuffer.append(buffer);
                trimToMaxSize();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void trimToMaxSize() {
        if (logBuffer.length() <= MAX_LOG_SIZE) return;
        int excess = logBuffer.length() - MAX_LOG_SIZE;
        logBuffer.delete(0, excess);
        int firstNewline = logBuffer.indexOf("\n");
        if (firstNewline > 0) logBuffer.delete(0, firstNewline + 1);
    }
}
