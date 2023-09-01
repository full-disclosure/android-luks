package eu.fulldisclosure.android.luks;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHelpers
{

    public File filesDir;
    public static String keyFileNamePlain;
    public static String keyFileNameEnc;
    public static String ivFileName;

    FileHelpers(File fd)
    {
        filesDir = fd;
        keyFileNamePlain = filesDir + "/sample.key";
        keyFileNameEnc = keyFileNamePlain + ".encrypted";
        ivFileName = keyFileNamePlain + ".iv";

    }

    public void writeEncryptedKey(byte[] ciphertext) throws IOException
    {
        File keyFile = new File(keyFileNameEnc);
        FileOutputStream fos = new FileOutputStream(keyFile);
        fos.write(ciphertext);
        fos.close();
    }

    public void writeIV(byte[] iv) throws IOException
    {
        File ivFile = new File(ivFileName);
        FileOutputStream fos = new FileOutputStream(ivFile);
        fos.write(iv);
        fos.close();
    }


    public byte[] readEncryptedKey() throws IOException
    {
        byte[] buffer = new byte[8208];
        File keyFile = new File(keyFileNameEnc);
        FileInputStream ins = new FileInputStream(keyFile);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        int size;
        while((size = ins.read(buffer, 0, 1024)) >= 0)
            os.write(buffer, 0, size);

        ins.close();

        return os.toByteArray();
    }

    public byte[] readIV() throws IOException
    {
        byte[] iv = new byte[16];
        File fiv = new File(ivFileName);
        BufferedInputStream ivbuf = new BufferedInputStream(new FileInputStream(fiv));
        ivbuf.read(iv, 0, iv.length);
        ivbuf.close();

        return iv;
    }

    public boolean hasKey()
    {
        File file = new File(keyFileNameEnc);

        return file.exists();
    }
}
