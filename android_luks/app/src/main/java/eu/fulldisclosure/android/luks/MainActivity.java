package eu.fulldisclosure.android.luks;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import java.security.SecureRandom;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import android.app.PendingIntent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.concurrent.Executor;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class MainActivity extends AppCompatActivity implements Runnable
{
    private static final String ACTION_USB_PERMISSION = "com.examples.accessory.controller.action.USB_PERMISSION";
    private final String TAG = "FDEU";

    private Cipher mycipher;
    private FileHelpers fh;
    private CryptoHelpers ch;

    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;
    private boolean keyHasBeenSetup;

    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;

    private void buttonInit()
    {
        TextView authStatus = findViewById(R.id.textView);
        Button button = findViewById(R.id.button);

        authStatus.setText("Create a key by pressing Init");
        button.setText("Init");
    }

    private void buttonUnlock()
    {
        TextView authStatus = findViewById(R.id.textView);
        Button button = findViewById(R.id.button);

        authStatus.setText("Connect usb and press Unlock");
        button.setText("Unlock");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        fh = new FileHelpers(this.getFilesDir());
        ch = new CryptoHelpers(fh);

        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (mInputStream != null && mOutputStream != null)
            return;

        TextView authStatus = findViewById(R.id.textView);
        Button button = findViewById(R.id.button);

        if (keyHasBeenSetup = fh.hasKey())
            buttonUnlock();
        else
            buttonInit();

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null)
        {
            if (mUsbManager.hasPermission(accessory))
            {
                openAccessory(accessory);
            }
            else
            {
                synchronized (mUsbReceiver)
                {
                    if (!mPermissionRequestPending)
                    {
                        mUsbManager.requestPermission(accessory,mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        }
        else
        {
            Log.d(TAG, "mAccessory is null");
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        closeAccessory();
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

    private void openAccessory(UsbAccessory accessory)
    {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null)
        {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            Thread thread = new Thread(null, this, "AccessoryController");
            thread.start();
            Log.d(TAG, "accessory opened");
        }
        else
        {
            Log.d(TAG, "accessory open fail");
        }
    }

    private void closeAccessory()
    {
        try
        {
            if (mFileDescriptor != null)
                mFileDescriptor.close();
        }
        catch (IOException e)
        {
            Log.d(TAG, "Exception: " + e);
        }
        finally
        {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action))
            {
                synchronized (this)
                {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                        openAccessory(accessory);
                    else
                        Log.d(TAG, "permission denied for accessory "+ accessory);

                    mPermissionRequestPending = false;
                }
            }
            else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
            {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && accessory.equals(mAccessory))
                    closeAccessory();

            }
        }
    };

    public boolean isAuthenticated = false;

    public void run()
    {
        while (!isAuthenticated)
        {
            Log.d(TAG, "Not yet authenticated...");
            try
            {
                Thread.sleep(1000);
            }
            catch (Exception e)
            {
                Log.d(TAG, "Thread sleep exception: " + e);
                return;
            }
        }

        try
        {
            if(keyHasBeenSetup)
            {
                Log.d(TAG, "Reading magic header");

                byte[] buffer = new byte[8192];
                int ret = mInputStream.read(buffer);
                Log.d(TAG, "Buffer read res: " + ret);
                // TODO: check for magic header
                Log.d(TAG, new String(buffer));

                Log.d(TAG, "Decrypting the LUKS key and sending over usb...");
                buffer = fh.readEncryptedKey();
                byte[] cleartext = mycipher.doFinal(buffer);

                mOutputStream.write(cleartext);
                mOutputStream.flush();
                Log.d(TAG, "written");

                Log.d(TAG, "Cleanup");
                for(int i = 0; i < cleartext.length; i++)
                    cleartext[i] = '\0';
            }

            isAuthenticated = false;
        }
        catch (IOException e)
        {
            Log.d(TAG, "Io exception. File not found?");
        }
        catch (IllegalBlockSizeException | BadPaddingException e)
        {
            Log.d(TAG, "Exception: " + e);
            throw new RuntimeException(e);
        }

    }

    public void onActionClicked(View view) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, CertificateException, IOException, UnrecoverableKeyException, KeyStoreException
    {
        Executor executor;
        BiometricPrompt biometricPrompt;
        BiometricPrompt.PromptInfo promptInfo;

        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG /*| DEVICE_CREDENTIAL*/))
        {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.d(TAG, "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.d(TAG, "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                Log.d(TAG, "Prompts the user to create credentials that your app accepts");
                break;
        }

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback()
        {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString)
            {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result)
            {
                super.onAuthenticationSucceeded(result);

                try
                {
                    if (result.getCryptoObject() != null)
                    {
                        mycipher = result.getCryptoObject().getCipher();
                    }
                    else
                    {
                        Log.d(TAG, "Empty cipher");
                        return;
                    }

                    isAuthenticated = true;
                    Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                    TextView authStatus = findViewById(R.id.textView);
                    authStatus.setText("Authenticated");

                    if(!keyHasBeenSetup)
                    {
                        Log.d(TAG, "Encryting and saving the new LUKS key...");

                        SecureRandom random = new SecureRandom();
                        byte[] buffer = new byte[8192];
                        random.nextBytes(buffer);

                        byte[] ciphertext = mycipher.doFinal(buffer);
                        fh.writeEncryptedKey(ciphertext);
                        fh.writeIV(mycipher.getIV());
                        keyHasBeenSetup = true;

                        buttonUnlock();
                    }
                }
                catch (Exception e)
                {
                    Log.d(TAG, "Exception: " + e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onAuthenticationFailed()
            {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.CryptoObject cryptoObject;
        if (keyHasBeenSetup)
        {
            Log.d(TAG, "Biometrics for decryption");
            cryptoObject = new BiometricPrompt.CryptoObject(ch.getDecryptCipher());
        }
        else
        {
            Log.d(TAG, "Biometrics for encryption");
            cryptoObject = new BiometricPrompt.CryptoObject(ch.getEncryptCipher());
        }

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock LUKS encryption")
                .setSubtitle("This will transfer your decrypted key to the host")
                .setNegativeButtonText("Try to use LUKS password instead")
                .build();
        biometricPrompt.authenticate(promptInfo, cryptoObject);
        Log.d(TAG,"Authentication prompt created");

    }
}