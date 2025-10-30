package io.nemeneme.shell.database;

import io.nemeneme.shell.models.TalkMessage;

import java.util.List;
import java.util.function.Function;

public class TalkMonitor {

    private final TalkDBReader reader = new TalkDBReader();
    private final Function<TalkMessage, Void> messageHandler;
    private volatile boolean running = false;
    private Thread monitorThread;
    private long maxDBID;

    public TalkMonitor(Function<TalkMessage, Void> messageHandler) {
        this.messageHandler = messageHandler;
        this.maxDBID = reader.getMaxDBID();
    }

    public synchronized void start() {
        if (running) {
            throw new IllegalStateException("이미 실행 중입니다");
        }

        running = true;
        monitorThread = new Thread(() -> {
            System.out.println("채팅 모니터링이 실행되었습니다");

            while (running) {
                try {
                    for (TalkMessage talkMessage : reader.getMessagesAfter(maxDBID)) {
                        maxDBID = talkMessage.dbId;
                        try {
                            messageHandler.apply(talkMessage);
                        } catch (Exception e) {
                            e.printStackTrace(System.out);
                        }
                    }

                    //noinspection BusyWait
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }, "TalkMonitorThread");

        monitorThread.start();
    }

    public synchronized void stop() {
        if (!running) return;

        running = false;
        monitorThread.interrupt();
        try {
            monitorThread.join(2000);
        } catch (InterruptedException ignored) {
        }
    }
}
