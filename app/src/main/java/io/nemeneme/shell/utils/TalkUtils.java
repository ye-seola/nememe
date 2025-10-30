package io.nemeneme.shell.utils;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TalkUtils {
    @SuppressLint("SdCardPath")
    public static String getAppPath() {
        String defaultPath = "/data/data/com.kakao.talk/";
        String mirrorPath = "/data_mirror/data_ce/null/0/com.kakao.talk/";

        File mirrorFile = new File(mirrorPath);
        File defaultFile = new File(defaultPath);

        if (mirrorFile.canRead()) {
            return mirrorPath;
        } else if (defaultFile.canRead()) {
            return defaultPath;
        } else {
            throw new RuntimeException("읽기 가능한 카카오톡 앱 경로를 찾을 수 없습니다");
        }
    }

    public static String readNotificationReferer() {
        try {
            Path prefsFile = Paths.get(TalkUtils.getAppPath(), "shared_prefs/KakaoTalk.hw.perferences.xml");

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = Files.newBufferedReader(prefsFile, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            String data = sb.toString();

            Pattern pattern = Pattern.compile("<string name=\"NotificationReferer\">(.*?)</string>");
            Matcher matcher = pattern.matcher(data);
            if (!matcher.find()) {
                throw new Exception("referer를 찾지 못했습니다.");
            }

            String referer = matcher.group(1);
            if (referer == null) {
                throw new Exception("referer를 찾지 못했습니다.");
            }

            return referer;
        } catch (Exception e) {
            System.out.println("Referer를 찾을 수 없습니다. 카카오톡 로그인 후 1회 이상 알림을 받아야 합니다.");
            e.printStackTrace(System.out);

            throw new RuntimeException(e);
        }
    }
}
