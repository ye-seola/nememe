package io.nememe.shell.models;

import org.json.JSONObject;

public class TalkMessage {
    public final long dbId;
    public final long logId;
    public final long chatId;
    public final long authorId;
    public final String authorName;
    public final int messageType;
    public final String message;
    public final String attachment;
    public final int sentAt;
    public final int scope;
    public final long threadId;

    public TalkMessage(long dbId, long logId, long chatId, long authorId,
                       String authorName, int messageType, String message,
                       String attachment, int sentAt, int scope, long threadId) {
        this.dbId = dbId;
        this.logId = logId;
        this.chatId = chatId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.messageType = messageType;
        this.message = message;
        this.attachment = attachment;
        this.sentAt = sentAt;
        this.scope = scope;
        this.threadId = threadId;
    }

    public JSONObject toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("dbId", dbId);
            obj.put("logId", logId);
            obj.put("chatId", chatId);
            obj.put("authorId", authorId);
            obj.put("authorName", authorName);
            obj.put("messageType", messageType);
            obj.put("message", message);
            obj.put("attachment", attachment);
            obj.put("sentAt", sentAt);
            obj.put("scope", scope);
            obj.put("threadId", threadId);
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
