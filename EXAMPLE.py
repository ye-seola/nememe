from dataclasses import dataclass
import json
from websockets.sync.client import connect

from typing import TypedDict
import requests


@dataclass
class Media:
    path: str
    filename: str | None = None
    mime_type: str | None = None

    def to_tuple(self):
        ctype = self.mime_type or "application/octet-stream"
        name = self.filename or self.path.split("/")[-1]
        return ("file", (name, open(self.path, "rb"), ctype))


class TalkClient:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip("/")

    def send_media(self, chat_id: int, media: list[Media] | Media):
        forceMultiple = False

        if isinstance(media, list):
            files = [m.to_tuple() for m in media]
            forceMultiple = True
        else:
            files = [media.to_tuple()]

        try:
            requests.post(
                f"{self.base_url}/media",
                data={"chatId": str(chat_id), "forceMultiple": str(forceMultiple)},
                files=files,
            ).text
        finally:
            for _, (_, f, _) in files:
                f.close()

    def send_message(self, chat_id: int, message: str, thread_id: int | None = None):
        data = {
            "chatId": str(chat_id),
            "message": message,
        }
        if thread_id is not None and thread_id != -1:
            data["threadId"] = str(thread_id)
        requests.post(f"{self.base_url}/message", data=data)

    def react_message(self, chat_id: int, log_id: int, thread_id: int | None = None):
        data = {
            "chatId": str(chat_id),
            "logId": str(log_id),
        }
        if thread_id is not None and thread_id != -1:
            data["threadId"] = str(thread_id)
        requests.post(f"{self.base_url}/react", data=data)

    def read_message(self, chat_id: int, thread_id: int | None = None):
        data = {"chatId": str(chat_id)}
        if thread_id is not None and thread_id != -1:
            data["threadId"] = str(thread_id)
        requests.post(f"{self.base_url}/read", data=data)


class ChatRecord(TypedDict):
    dbId: int
    logId: int
    chatId: int
    authorId: int
    authorName: str
    messageType: int
    message: str
    attachment: str
    sentAt: int
    scope: int
    threadId: int


tc = TalkClient("http://localhost:7070")


def main():
    uri = "ws://localhost:7070/ws"
    with connect(uri) as ws:
        while True:
            chat = ChatRecord(json.loads(ws.recv()))
            print(chat)

            if chat["message"] == "chat":
                tc.send_message(
                    chat["chatId"],
                    "hello",
                    chat["threadId"],
                )

            if chat["message"] == "media":
                tc.send_media(
                    chat["chatId"],
                    [
                        Media(path="./audio1.ogg", mime_type="audio/ogg"),
                    ],
                )

            if chat["message"] == "react":
                tc.react_message(
                    chat["chatId"],
                    chat["logId"],
                    chat["threadId"],
                )


if __name__ == "__main__":
    main()

