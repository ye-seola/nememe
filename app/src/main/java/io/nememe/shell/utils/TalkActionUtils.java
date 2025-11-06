package io.nememe.shell.utils;

import android.app.RemoteInput;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TalkActionUtils {
    public static void sendMessage(long chatId, @Nullable Long threadId, String msg) {
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

    public static void reactMessage(long chatId, long logId, @Nullable Long threadId) {
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

    public static class Media {
        public final String path;
        public final String fileName;
        public final String mimeType;

        public Media(String path, String fileName, String mimeType) {
            this.path = path;
            this.fileName = fileName;
            this.mimeType = Objects.requireNonNullElse(mimeType, "application/octet-stream");
        }

        @Override
        public String toString() {
            return "Media{" +
                    "path='" + path + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", mimeType='" + mimeType + '\'' +
                    '}';
        }
    }

    public static void sendMedia(List<Media> mediaList, long chatId, boolean forceMultiple) {
        if (mediaList.isEmpty()) return;

        Intent intent = new Intent();
        intent.setPackage("com.kakao.talk");

        var uris = mediaList.stream()
                .map(TalkActionUtils::mediaToProviderUri)
                .collect(Collectors.toCollection(ArrayList::new));

        if (forceMultiple || mediaList.size() > 1) {
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            intent.setType(mediaList.get(0).mimeType);
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            System.out.println(uris.get(0).toString());
        }

        intent.putExtra("key_id", chatId);
        intent.putExtra("key_type", 1);
        intent.putExtra("key_from_direct_share", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        System.out.println(intent);
        AndroidUtils.startActivity(intent);
    }

    private static Uri mediaToProviderUri(Media media) {
        return new Uri.Builder()
                .scheme("content")
                .authority("io.nememe.provider")
                .appendPath(media.path)
                .appendQueryParameter("name", media.fileName)
                .appendQueryParameter("mimeType", media.mimeType)
                .build();
    }
}
