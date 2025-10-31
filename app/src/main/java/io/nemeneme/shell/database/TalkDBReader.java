package io.nemeneme.shell.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import io.nemeneme.shell.crypto.TalkDecrypt;
import io.nemeneme.shell.models.TalkMessage;
import io.nemeneme.shell.utils.TalkUtils;

import org.json.JSONObject;

import java.nio.file.Paths;
import java.util.*;

public class TalkDBReader {
    private final SQLiteDatabase connection;
    public final long myUserId;

    public TalkDBReader() {
        String dbPath = Paths.get(TalkUtils.getAppPath(), "databases").toString();

        try {
            connection = SQLiteDatabase.openDatabase(":memory:", null, SQLiteDatabase.OPEN_READONLY);

            connection.execSQL(
                    "ATTACH DATABASE '" + Paths.get(dbPath, "KakaoTalk.db") + "' AS db1;"
            );
            connection.execSQL(
                    "ATTACH DATABASE '" + Paths.get(dbPath, "KakaoTalk2.db") + "' AS db2;"
            );
        } catch (Exception e) {
            throw new RuntimeException("DB 열기 실패", e);
        }

        myUserId = getMyUserId();
    }

    public TalkMessage[] getMessagesAfter(long lastId) {
        List<Map<String, String>> rows = executeQuery(
                "SELECT * FROM chat_logs WHERE _id > ? ORDER BY _id ASC",
                new String[]{String.valueOf(lastId)}
        );

        List<TalkMessage> messages = new ArrayList<>();

        for (Map<String, String> m : rows) {
            try {
                long logId = Long.parseLong(Objects.requireNonNull(m.get("id")));
                long authorId = Long.parseLong(Objects.requireNonNull(m.get("user_id")));
                int encType = new JSONObject(m.get("v")).getInt("enc");

                TalkMessage msg = new TalkMessage(
                        Long.parseLong(Objects.requireNonNull(m.get("_id"))),
                        logId,
                        Long.parseLong(Objects.requireNonNull(m.get("chat_id"))),
                        authorId,
                        getNicknameFromLogId(logId),
                        Integer.parseInt(Objects.requireNonNull(m.get("type"))),
                        TalkDecrypt.decrypt(encType, m.get("user_id"), m.get("message")),
                        TalkDecrypt.decrypt(encType, m.get("user_id"), m.get("attachment")),
                        Integer.parseInt(Objects.requireNonNull(m.get("created_at"))),
                        Integer.parseInt(Objects.requireNonNullElse(m.get("scope"), "1")),
                        Long.parseLong(Objects.requireNonNullElse(m.get("thread_id"), "-1"))
                );
                messages.add(msg);
            } catch (Exception ignored) {
            }
        }

        return messages.toArray(new TalkMessage[0]);
    }

    private String getNicknameFromLogId(long logId) {
        try {
            Map<String, String> res = executeQuerySingle(
                    "SELECT " +
                            "myo.nickname as my_open_nickname," +
                            "l.user_id," +
                            "r.type," +
                            "CASE r.type" +
                            " WHEN 'OM' THEN o.nickname " +
                            " WHEN 'PlusChat' THEN f.name " +
                            " ELSE NULL END AS user_name, " +
                            "CASE r.type " +
                            " WHEN 'OM' THEN o.enc " +
                            " WHEN 'PlusChat' THEN f.enc " +
                            " ELSE NULL END AS enc " +
                            "FROM chat_logs l " +
                            "JOIN chat_rooms r ON l.chat_id = r.id " +
                            "LEFT JOIN open_chat_member o ON (r.type = 'OM' AND l.user_id = o.user_id) " +
                            "LEFT JOIN open_profile myo ON (r.type = 'OM' AND l.user_id = myo.user_id AND myo.link_id = (SELECT link_id FROM chat_rooms WHERE id = l.chat_id)) " +
                            "LEFT JOIN friends f ON (r.type = 'PlusChat' AND l.user_id = f.id) " +
                            "WHERE l.id = ?;",
                    new String[]{String.valueOf(logId)}
            );

            if (res == null) return "";

            String userId = Objects.requireNonNull(res.get("user_id"));
            if (userId.equals(String.valueOf(myUserId))) {
                return Objects.requireNonNullElse(res.get("my_open_nickname"), "");
            }

            String type = Objects.requireNonNull(res.get("type"));
            String userName = res.get("user_name");
            int encType = Integer.parseInt(Objects.requireNonNull(res.get("enc")));

            switch (type) {
                case "MultiChat":
                case "DirectChat":
                    return "";
                case "OM":
                case "PlusChat":
                    return TalkDecrypt.decrypt(encType, String.valueOf(myUserId), userName);
                default:
                    throw new Exception("알 수 없는 방 타입");
            }
        } catch (Exception ignored) {
            return "";
        }
    }

    private long getMyUserId() {
        Map<String, String> res = executeQuerySingle(
                "SELECT user_id FROM chat_logs WHERE v LIKE '%\"isMine\":true%' ORDER BY _id DESC LIMIT 1;",
                new String[]{}
        );

        if (res == null || res.get("user_id") == null) {
            throw new IllegalArgumentException("카카오톡 로그인 후 채팅을 1회 이상 받은 뒤 재시작 해주세요");
        }

        return Long.parseLong(Objects.requireNonNull(res.get("user_id")));
    }

    public long getMaxDBID() {
        Map<String, String> res = executeQuerySingle(
                "SELECT MAX(_id) as cnt from chat_logs",
                new String[]{}
        );
        if (res == null || res.get("cnt") == null) return 0L;
        return Long.parseLong(Objects.requireNonNull(res.get("cnt")));
    }

    private Map<String, String> executeQuerySingle(String sql, String[] args) {
        List<Map<String, String>> list = executeQuery(sql, args);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<Map<String, String>> executeQuery(String sql, String[] args) {
        List<Map<String, String>> result = new ArrayList<>();

        try (Cursor cursor = connection.rawQuery(sql, args)) {
            String[] columns = cursor.getColumnNames();
            while (cursor.moveToNext()) {
                Map<String, String> row = new HashMap<>();
                for (String col : columns) {
                    row.put(col, cursor.getString(cursor.getColumnIndexOrThrow(col)));
                }
                result.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
