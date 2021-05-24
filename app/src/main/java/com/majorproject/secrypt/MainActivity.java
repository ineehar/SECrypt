package com.majorproject.secrypt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import android.util.Base64;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_PICK_CONTACT =1;
    EditText pNumber, message;
    ImageButton contacts;
    Button myMessages;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        pNumber = (EditText) findViewById(R.id.editTextPhone);
        message = (EditText) findViewById(R.id.editTextMessage);


        BottomNavigationView btnNav = findViewById(R.id.bottomnavigationview);
        btnNav.setOnNavigationItemSelectedListener(navListener);

        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout,new chatfragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.call:
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    startActivity(intent);
                    break;

                case R.id.chats:
                   // selectedFragment = new chatfragment();
                    Intent intent1 = new Intent(MainActivity.this, ReceiverActivity.class);
                    startActivity(intent1);
                    break;
                case R.id.contacts:
                    Intent in = new Intent (Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult (in, RESULT_PICK_CONTACT);
//                    selectedFragment=new contactsfragments();
//                    break;

            }
            //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout,selectedFragment).commit();
            return true;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Toast.makeText(this, "Failed To pick contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;

        try {
            String phoneNo = null;
            Uri uri = data.getData ();
            cursor = getContentResolver ().query (uri, null, null,null,null);
            cursor.moveToFirst ();
            int phoneIndex = cursor.getColumnIndex (ContactsContract.CommonDataKinds.Phone.NUMBER);

            phoneNo = cursor.getString (phoneIndex);

            pNumber.setText (phoneNo);


        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    public void send(View view) throws InvalidKeySpecException, NoSuchAlgorithmException {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED)
            MyMessage();
        else
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SEND_SMS}, 0);
    }

    public static PublicKey loadPublicKey(String publicKeyStr)
            throws Exception {
        try {// w w w  . j a  v  a2  s.co m
            byte[] buffer = Base64.decode(publicKeyStr, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("");
        } catch (InvalidKeySpecException e) {
            throw new Exception("");
        } catch (NullPointerException e) {
            throw new Exception("");
        }
    }

    public void MyMessage() throws InvalidKeySpecException, NoSuchAlgorithmException {
        String phoneNumber = pNumber.getText().toString().trim();
        String sendMessage = message.getText().toString().trim();
        KeyPair key = null;
        try {
            key = Asymmetric.generateRSAKkeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final KeyPair finalKey = key;
        String publicKeySend = Asymmetric.PublicToString(finalKey.getPublic());
        byte[] cipherText = new byte[100000];

        try {
            cipherText = Asymmetric.do_RSAEncryption(sendMessage, finalKey.getPrivate());

           // pNumber.setText(publicKeySend);
        } catch (Exception e) {
            e.printStackTrace();
        }


        String plainText = "";
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(phoneNumber.equals("") || sendMessage.equals(""))
            Toast.makeText(this,"Enter valid credentials", Toast.LENGTH_LONG).show();
        else{
            SmsManager smsManager = SmsManager.getDefault();
            String cp = Base64.encodeToString(cipherText,0);
            sendMessage = cp +" PK: " + publicKeySend;
            ArrayList<String> parts = new ArrayList<>();
            int i=0;
            while(i < sendMessage.length())
            {
                if( i+140 > sendMessage.length())
                    parts.add(sendMessage.substring(i));
                else
                    parts.add(sendMessage.substring(i, i + 140));

                i+=140;
            }
             smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
             //smsManager.sendTextMessage(phoneNumber, null, plainText, null, null);
            Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show();
            //pNumber.setText(cipherText.toString()+" "+publicKeySend);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case 0:
                if(grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        MyMessage();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
                else
                    Toast.makeText(this, "You don't have required permissions", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}