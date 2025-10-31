package io.nememe.shell.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
    public static String decrypt(byte[] key, byte[] iv, String b64) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] ct = Base64.getDecoder().decode(b64);
        byte[] decrypted = cipher.doFinal(ct);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
