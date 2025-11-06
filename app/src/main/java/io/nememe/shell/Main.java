package io.nememe.shell;

import android.annotation.SuppressLint;
import android.ddm.DdmHandleAppName;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.nememe.shell.android.AEnvironment;
import io.nememe.shell.database.TalkMonitor;
import io.nememe.shell.executor.DelayedSingleThreadExecutor;
import io.nememe.shell.utils.NetUtils;
import io.nememe.shell.utils.TalkActionUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Main {
    private static final Vertx vertx = Vertx.vertx();
    private static final DelayedSingleThreadExecutor messageExecutor = new DelayedSingleThreadExecutor(500);
    private static final DelayedSingleThreadExecutor reactExecutor = new DelayedSingleThreadExecutor(100);
    private static final DelayedSingleThreadExecutor readExecutor = new DelayedSingleThreadExecutor(100);
    private static final DelayedSingleThreadExecutor mediaExecutor = new DelayedSingleThreadExecutor(1000);

    public static void main(String[] args) throws IOException {
        Looper.prepare();

        DdmHandleAppName.setAppName("nememe_shell", 0);
        NetUtils.printAddresses();

        Thread.currentThread().setUncaughtExceptionHandler((thread, th) -> {
            System.out.println("Exception!!");
            th.printStackTrace(System.out);
        });

        TalkMonitor monitor = null;
        HttpServer server = null;

        Router router = Router.router(vertx);

        router.errorHandler(500, rc -> {
            Throwable failure = rc.failure();
            if (failure != null) {
                failure.printStackTrace(System.out);
            }
        });

        router.route("/ws").handler(rc -> {
            rc.request().toWebSocket().andThen((ws, th) -> {
                if (th != null) {
                    rc.fail(500, th);
                    return;
                }

                vertx.eventBus().consumer("NEW_CHAT", message -> {
                    ws.writeTextMessage((String) message.body());
                });
            });
        });

        var uploadPath = new File(AEnvironment.getAppContext().getCacheDir(), "nememe-upload-tmp");
        Handler uploadRemoveHandler = new Handler();
        router.post("/media")
                .handler(BodyHandler.create(uploadPath.toString()))
                .handler(rc -> {
                    var formData = rc.request().formAttributes();
                    Long chatId = parseLongOrNull(formData.get("chatId"));
                    boolean forceMultiple = Boolean.parseBoolean(formData.get("forceMultiple"));

                    if (chatId == null) {
                        rc.cancelAndCleanupFileUploads();
                        rc.fail(400);
                        return;
                    }

                    var uploads = rc.fileUploads();
                    ArrayList<TalkActionUtils.Media> mediaList = new ArrayList<>();
                    for (FileUpload upload : uploads) {
                        var media = new TalkActionUtils.Media(upload.uploadedFileName(), upload.fileName(), upload.contentType());
                        mediaList.add(media);
                    }

                    mediaExecutor.submit(() -> {
                        TalkActionUtils.sendMedia(mediaList, chatId, forceMultiple);
                        uploadRemoveHandler.postDelayed(rc::cancelAndCleanupFileUploads, 1000);
                    });

                    rc.end();
                });

        router.post("/message")
                .handler(BodyHandler.create(false))
                .handler(rc -> {
                    var formData = rc.request().formAttributes();
                    Long chatId = parseLongOrNull(formData.get("chatId"));
                    Long threadId = parseLongOrNull(formData.get("threadId"));
                    String message = formData.get("message");

                    if (chatId == null || message == null) {
                        rc.fail(400);
                        return;
                    }

                    messageExecutor.submit(() -> {
                        TalkActionUtils.sendMessage(chatId, threadId, message);
                    });

                    rc.end();
                });

        router.post("/react")
                .handler(BodyHandler.create(false))
                .handler(rc -> {
                    var formData = rc.request().formAttributes();
                    Long chatId = parseLongOrNull(formData.get("chatId"));
                    Long logId = parseLongOrNull(formData.get("logId"));
                    Long threadId = parseLongOrNull(formData.get("threadId"));

                    if (chatId == null || logId == null) {
                        rc.fail(400);
                        return;
                    }

                    reactExecutor.submit(() -> {
                        TalkActionUtils.reactMessage(chatId, logId, threadId);
                    });

                    rc.end();
                });

        router.post("/read")
                .handler(BodyHandler.create(false))
                .handler(rc -> {
                    var formData = rc.request().formAttributes();
                    Long chatId = parseLongOrNull(formData.get("chatId"));
                    Long threadId = parseLongOrNull(formData.get("threadId"));

                    if (chatId == null) {
                        rc.fail(400);
                        return;
                    }

                    readExecutor.submit(() -> {
                        TalkActionUtils.readMessage(chatId, threadId);
                    });

                    rc.end();
                });

        try {

            server = vertx.createHttpServer();
            server.requestHandler(router).listen(7070);

            System.out.println("웹서버가 실행되었습니다");

            monitor = new TalkMonitor(msg -> {
                System.out.println(msg.toJson().toString());
                vertx.eventBus().publish("NEW_CHAT", msg.toJson().toString());
                return null;
            });
            monitor.start();

            // Wait
            try (InputStream in = System.in) {
                //noinspection StatementWithEmptyBody
                while (in.read() != -1) ;
            }
        } finally {
            if (monitor != null) {
                System.out.println("DB 모니터를 종료합니다");
                monitor.stop();
            }
            if (server != null) {
                System.out.println("웹서버를 종료합니다");
                server.shutdown(5, TimeUnit.SECONDS).await();
            }

            System.out.println("종료되었습니다.");
            System.exit(0);
        }
    }

    private static Long parseLongOrNull(String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
