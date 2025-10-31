package io.nememe.shell.crypto;

public class TalkCryptoConstants {
    public static final String[] SALT_PREFIXES = {
            "", "", "12", "24", "18", "30", "36", "12", "48", "7", "35", "40", "17",
            "23", "29", "isabel", "kale", "sulli", "van", "merry", "kyle", "james",
            "maddux", "tony", "hayden", "paul", "elijah", "dorothy", "sally",
            "bran", "extr.ursra", "veil"
    };

    public static final byte[] KDF_PASSWORD = {
            0, 22, 0, 8, 0, 9, 0, 111, 0, 2, 0, 23, 0, 43, 0, 8, 0, 33, 0, 33, 0, 10,
            0, 16, 0, 3, 0, 3, 0, 7, 0, 6, 0, 0
    };

    public static final byte[] KAKAO_IV = {
            15, 8, 1, 0, 25, 71, 37, (byte) 220, 21, (byte) 245,
            23, (byte) 224, (byte) 225, 21, 12, 53
    };
}
