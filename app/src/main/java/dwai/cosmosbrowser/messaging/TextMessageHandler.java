package dwai.cosmosbrowser.messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import dwai.cosmosbrowser.FullTextMessage;

import androidx.appcompat.app.AlertDialog;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import dwai.cosmosbrowser.MainBrowserScreen;

public class TextMessageHandler {

    private final String TAG = "TextMessageHandler";
    public static final String PHONE_NUMBER = "0018443341241";




    /**
     *
     * @param body Body of the message a person is trying to send.
     * @param to Who the person is sending the text message to. Must be 10 digits.
     */
    public void sendTextMessage(String body, String to){
        //TODO: Make it so that this works when it's not 10 digits.

        if(body == null || to == null){
            Log.e(TAG, "***** ERROR EITHER BODY OR TO IS NULL!");
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        if(body.length() > 160) {
            //Because the body of the message can be larger than tha 140 bit limit presented, the message must be split up.
            ArrayList<String> parts = smsManager.divideMessage(body);
            smsManager.sendMultipartTextMessage(to, null, parts, null, null);
        }
        else{
            smsManager.sendTextMessage(to,null,body,null,null);
        }

    }

    public static class SMSReceiver extends BroadcastReceiver {

        private final String TAG = "SMSReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle extras = intent.getExtras();
                Object[] pdus = (Object[]) extras.get("pdus");
                for (Object pdu : pdus) {
                    SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu);
                    String origin = msg.getOriginatingAddress();
                    String body = msg.getMessageBody();
                    if(origin.contains(PHONE_NUMBER)) {

                        MainBrowserScreen.webView.loadDataWithBaseURL(null, origin + " , " + body, "text/html", "utf-8", null);
                    }
                    }
            }
        }
    }


    //old merged methods:
    static FullTextMessage fullTextMessage;

    private void textToTwilio(String whatToSend) throws Exception{
        fullTextMessage.texts = new ArrayList<String>();
        String phone_Num = PHONE_NUMBER;
        String send_msg = whatToSend;
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phone_Num, null, send_msg, null, null);

    }
    private void sendStringToTwilio(String whatToSend){
        String send_msg = whatToSend;
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(PHONE_NUMBER, null, send_msg, null, null);
    }
    private void generateAlertDialog(String message, Context context){
        new AlertDialog.Builder(context)
                .setTitle("Error!")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void saveFile(String name, String content, Context ctx) {
        String filename = name;
        String string = content;
        FileOutputStream outputStream;
        try {
            outputStream = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
