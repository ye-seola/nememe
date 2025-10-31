package io.nememe.shell;

import android.ddm.DdmHandleAppName;

import java.io.InputStream;

import io.nememe.shell.database.TalkMonitor;
import io.nememe.shell.utils.TalkUtils;

public class Main {
    public static void main(String[] args) {
        DdmHandleAppName.setAppName("nememe_shell", 0);
        System.out.println("앱 경로: " + TalkUtils.getAppPath());

        TalkMonitor monitor = null;
        try {
            monitor = new TalkMonitor(msg -> {
                System.out.println(msg.toJson().toString());
                return null;
            });
            monitor.start();

            // Wait
            try (InputStream in = System.in) {
                //noinspection StatementWithEmptyBody
                while (in.read() != -1) ;
            }

            System.out.println("종료하는 중입니다.");
        } catch (Exception e) {
            System.out.println("Exception!!");
            e.printStackTrace(System.out);
        } finally {
            if (monitor != null) monitor.stop();
        }
    }
}
