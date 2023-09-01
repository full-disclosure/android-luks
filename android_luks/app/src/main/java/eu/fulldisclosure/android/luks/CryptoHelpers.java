package eu.fulldisclosure.android.luks;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

//import androidx.biometric.BiometricPrompt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class CryptoHelpers
{
    private static String TAG = "FDEU";
    private FileHelpers fh;

    CryptoHelpers(FileHelpers fh)
    {
        this.fh = fh;
    }
    public Cipher getEncryptCipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, UnrecoverableKeyException, CertificateException, IOException, KeyStoreException, NoSuchProviderException
    {
        Key key = getMyKey();

        String algorithm = KeyProperties.KEY_ALGORITHM_AES;
        String blockMode = KeyProperties.BLOCK_MODE_CBC;
        String padding = KeyProperties.ENCRYPTION_PADDING_PKCS7;
        Cipher cipher = Cipher.getInstance(algorithm+"/"+blockMode+"/"+padding);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher;
    }

    public Cipher getDecryptCipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, InvalidAlgorithmParameterException, UnrecoverableKeyException, CertificateException, KeyStoreException, NoSuchProviderException
    {
        Key key = getMyKey();

        String algorithm = KeyProperties.KEY_ALGORITHM_AES;
        String blockMode = KeyProperties.BLOCK_MODE_CBC;
        String padding = KeyProperties.ENCRYPTION_PADDING_PKCS7;
        Cipher cipher = Cipher.getInstance(algorithm+"/"+blockMode+"/"+padding);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(fh.readIV()));

        return cipher;
    }

    public SecretKey getMyKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, CertificateException, IOException, KeyStoreException, UnrecoverableKeyException
    {
        String provider = "AndroidKeyStore";
        String keyName = "LUKS2";

        KeyStore keyStore = KeyStore.getInstance(provider);
        keyStore.load(null);
        Key theSecretKey = keyStore.getKey(keyName, null);
        if (theSecretKey != null)
        {
            Log.e(TAG, "Got existing key");
            return (SecretKey)theSecretKey;
        }

        Log.d(TAG, "No key found, generating new");
        String algorithm = KeyProperties.KEY_ALGORITHM_AES;
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm, provider);
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .build();

        keyGenerator.init(keyGenParameterSpec);

        return keyGenerator.generateKey();
    }


}
