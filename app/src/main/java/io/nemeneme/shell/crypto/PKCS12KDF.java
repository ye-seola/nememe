package io.nemeneme.shell.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PKCS12KDF {
    public static byte[] deriveKey(byte[] passwordBytes, byte[] saltBytes, int iterations, int dkeySize) throws NoSuchAlgorithmException {
        MessageDigest hasher = MessageDigest.getInstance("SHA-1");
        int digestSize = hasher.getDigestLength();
        int blockSize = 64;

        byte[] D = new byte[blockSize];
        Arrays.fill(D, (byte) 1);

        byte[] S = new byte[blockSize * ((saltBytes.length + blockSize - 1) / blockSize)];
        for (int i = 0; i < S.length; i++) {
            S[i] = saltBytes[i % saltBytes.length];
        }

        byte[] P = new byte[blockSize * ((passwordBytes.length + blockSize - 1) / blockSize)];
        for (int i = 0; i < P.length; i++) {
            P[i] = passwordBytes[i % passwordBytes.length];
        }

        byte[] I = new byte[S.length + P.length];
        System.arraycopy(S, 0, I, 0, S.length);
        System.arraycopy(P, 0, I, S.length, P.length);

        byte[] B = new byte[blockSize];
        int c = (dkeySize + digestSize - 1) / digestSize;
        byte[] dKey = new byte[dkeySize];

        for (int i = 1; i <= c; i++) {
            hasher = MessageDigest.getInstance("SHA-1");
            hasher.update(D);
            hasher.update(I);
            byte[] A = hasher.digest();

            for (int j = 1; j < iterations; j++) {
                hasher = MessageDigest.getInstance("SHA-1");
                hasher.update(A);
                A = hasher.digest();
            }

            for (int j = 0; j < B.length; j++) {
                B[j] = A[j % A.length];
            }

            for (int j = 0; j < I.length / blockSize; j++) {
                pkcs16adjust(I, j * blockSize, B);
            }

            int start = (i - 1) * digestSize;
            if (i == c) {
                System.arraycopy(A, 0, dKey, start, dkeySize - start);
            } else {
                System.arraycopy(A, 0, dKey, start, A.length);
            }
        }

        return dKey;
    }

    private static void pkcs16adjust(byte[] a, int aOff, byte[] b) {
        int x = (b[b.length - 1] & 0xff) + (a[aOff + b.length - 1] & 0xff) + 1;
        a[aOff + b.length - 1] = (byte) (x % 256);
        x >>= 8;
        for (int i = b.length - 2; i >= 0; i--) {
            x += (b[i] & 0xff) + (a[aOff + i] & 0xff);
            a[aOff + i] = (byte) (x % 256);
            x >>= 8;
        }
    }
}