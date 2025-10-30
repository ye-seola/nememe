package io.nemeneme.shell.crypto;

import android.util.LruCache;

public class TalkDecrypt {
    private static final LruCache<String, byte[]> keyCache = new LruCache<>(1000);

    public static String decrypt(int encType, String userId, String b64) {
        String salt = getSalt(encType, userId);
        byte[] key = getKey(salt);

        try {
            return AESUtils.decrypt(key, TalkCryptoConstants.KAKAO_IV, b64);
        } catch (Exception e) {
            return b64;
        }
    }

    private static String getSalt(int encType, String userId) {
        if (encType < 0 || encType >= TalkCryptoConstants.SALT_PREFIXES.length) {
            throw new IllegalArgumentException("지원하지 않는 enc " + encType);
        }

        String prefix = TalkCryptoConstants.SALT_PREFIXES[encType];
        String combined = prefix + userId;

        StringBuilder sb = new StringBuilder(combined);
        while (sb.length() < 16) {
            sb.append('\u0000');
        }
        return sb.substring(0, 16);
    }

    private static byte[] getKey(String salt) {
        byte[] cachedKey = keyCache.get(salt);
        if (cachedKey != null) {
            return cachedKey;
        }

        try {
            byte[] key = PKCS12KDF.deriveKey(
                    TalkCryptoConstants.KDF_PASSWORD,
                    salt.getBytes(),
                    2,
                    32
            );
            keyCache.put(salt, key);
            return key;
        } catch (Exception e) {
            throw new RuntimeException("키 파생 중 오류 발생", e);
        }
    }
}
