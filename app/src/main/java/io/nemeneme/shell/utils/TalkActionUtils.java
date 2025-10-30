package io.nemeneme.shell.utils;

import android.app.RemoteInput;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public class TalkActionUtils {
    public static void sendMessage(long chatId, Long threadId, String msg) {
        String referer;
        try {
            referer = TalkUtils.readNotificationReferer();
        } catch (Exception ignored) {
            return;
        }

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                "com.kakao.talk",
                "com.kakao.talk.notification.NotificationActionService"
        ));
        intent.putExtra("noti_referer", referer);
        intent.putExtra("chat_id", chatId);

        intent.putExtra("is_chat_thread_notification", threadId != null);
        if (threadId != null) {
            intent.putExtra("thread_id", threadId);
        }

        intent.setAction("com.kakao.talk.notification.REPLY_MESSAGE");

        Bundle results = new Bundle();
        results.putCharSequence("reply_message", msg);

        RemoteInput remoteInput = new RemoteInput.Builder("reply_message").build();
        RemoteInput.addResultsToIntent(new RemoteInput[]{remoteInput}, intent, results);

        AndroidUtils.startService(intent);
    }

    public static void reactMessage(long chatId, long logId, Long threadId) {
        String referer;
        try {
            referer = TalkUtils.readNotificationReferer();
        } catch (Exception ignored) {
            return;
        }

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                "com.kakao.talk",
                "com.kakao.talk.notification.NotificationActionService"
        ));

        intent.putExtra("noti_referer", referer);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("chat_log_id", logId);

        intent.putExtra("is_chat_thread_notification", threadId != null);
        if (threadId != null) {
            intent.putExtra("thread_id", threadId);
        }

        intent.setAction("com.kakao.talk.notification.REACTION_MESSAGE");

        AndroidUtils.startService(intent);
    }

    public static void readMessage(long chatId, Long threadId) {
        String referer;
        try {
            referer = TalkUtils.readNotificationReferer();
        } catch (Exception ignored) {
            return;
        }

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                "com.kakao.talk",
                "com.kakao.talk.notification.NotificationActionService"
        ));
        intent.putExtra("noti_referer", referer);
        intent.putExtra("chat_id", chatId);

        intent.putExtra("is_chat_thread_notification", threadId != null);
        if (threadId != null) {
            intent.putExtra("thread_id", threadId);
        }

        intent.setAction("com.kakao.talk.notification.READ_MESSAGE");

        AndroidUtils.startService(intent);
    }
}
