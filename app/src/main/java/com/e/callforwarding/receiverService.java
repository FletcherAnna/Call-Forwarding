package com.e.callforwarding;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.Map;

public class receiverService extends Service {
    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private BroadcastReceiver mReceiver;
    public static final String pdu_type = "pdus";


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "RECEIVER_SERVICE",
                    "Receiver Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }}
    @Override


    public int onStartCommand(Intent intent, int flags, int startId) {

        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        Notification notification = new NotificationCompat.Builder(this, "RECEIEVER_SERVICE")
                .setContentTitle("SMS Receiver")
                .build();
        SmsManager smsManager = SmsManager.getDefault();
        startForeground(1, notification);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Toast.makeText(getApplicationContext(),"Forward Command Received",Toast.LENGTH_SHORT).show();
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs;
                String address = "";
                String message = "";
                String format = bundle.getString("format");
                Object[] pdus = (Object[]) bundle.get(pdu_type);

                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
                Map<String, String> contacts = (Map<String, String>) sharedPref.getAll();

            if (pdus != null) {
                boolean isVersionM = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);

                msgs = new SmsMessage[pdus.length];

                for (int i = 0; i < msgs.length; i++) {
                    if (isVersionM) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    } else {
                        // If Android version L or older:
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }

                    //if text from untrusted number, return
                    //what if you want to forward from an untrusted number?
                    address += msgs[i].getOriginatingAddress();
                    //make sure the address is only ten digits long
                    address = PhoneNumberUtils.normalizeNumber(address.toString());
                    if(address.length() > 10){
                        address = address.substring(address.length() - 10);
                    }

                    int n = 0;
                    for(Map.Entry<String,String> entry: contacts.entrySet()){
                        if(entry.getValue().equals(address)){

                            n++;
                        }
                    }
                    if(n==0){
                        return;
                    }

                    //handle unforward request
                    message += msgs[i].getMessageBody();
                    message = message.toLowerCase();
                    if (message.length() > 8){
                        if (message.substring(0,9).equals("unforward")) {
                            telecomManager.placeCall(Uri.parse("tel:*73"), new Bundle());


                            //TODO:
                            smsManager.sendTextMessage(address.toString(), null, "Command Receieved. Unforwarding....", null, null);
                            return;
                        }

                        //parse message, check against known names
                        //what if you want to forward to an arbitrary number?
                        if (message.substring(0,8).equals("forward ")){
                            i = 8;

                            String sub = "";
                            while (i <  message.length() && (message.charAt(i) != (' '))){
                                sub += message.charAt(i);
                                i++;
                            }

                            //sharedPref.contains(String) would be better, but is case-sensitive
                            //TODO: encapsulate verification methods

                            for(Map.Entry<String,String> entry: contacts.entrySet()){
                                if(entry.getKey().toLowerCase().equals(sub)){
                                    telecomManager.placeCall(Uri.parse("tel:*72" + entry.getValue()), new Bundle());


                                    return;
                                }
                            }

                            if (sharedPref.contains(sub)){
                                String num = sharedPref.getString(sub, "");
                            }
                        }
                    }
                }
            }
            }


        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(SMS_RECEIVED);
        registerReceiver(mReceiver, filter);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}