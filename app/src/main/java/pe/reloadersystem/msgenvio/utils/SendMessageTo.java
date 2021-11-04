package pe.reloadersystem.msgenvio.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;

import pe.reloadersystem.msgenvio.SmsDeliveredReceiver;
import pe.reloadersystem.msgenvio.SmsResultReceiverData;

public class SendMessageTo {

    int requestCode;
    private SmsManager sms;
    SmsResultReceiverData smsSentReceiver;

    public void SendMessage(String code, String sms_numero, String sms_message, int id_sms, Context context) {

        sms = SmsManager.getDefault();
        smsSentReceiver = new SmsResultReceiverData();

        try {

            requestCode += 1;

            ArrayList<String> parts = sms.divideMessage(sms_message);
            int numParts = parts.size();

            Intent sendPIntent = new Intent(context, SmsResultReceiverData.class);
            sendPIntent.putExtra("sms_numero", sms_numero);
            sendPIntent.putExtra("sms_message", sms_message);
            sendPIntent.putExtra("sms_code", code);
            sendPIntent.putExtra("sms_codeid", id_sms);


            PendingIntent sentPI = PendingIntent.getBroadcast(context, requestCode,
                    sendPIntent, PendingIntent.FLAG_ONE_SHOT);


            Intent intentDPi = new Intent(context, SmsDeliveredReceiver.class);
            intentDPi.putExtra("sms_numero", sms_numero);
            intentDPi.putExtra("sms_message", sms_message);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(context, requestCode,
                    intentDPi, PendingIntent.FLAG_ONE_SHOT);


            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();

            for (int i = 0; i < numParts; i++) {
                sentIntents.add(sentPI);
                deliveryIntents.add(deliveredPI);
            }


//            sms.sendTextMessage(sms_numero, null, sms_message, sentPI, deliveredPI);

            sms.sendMultipartTextMessage(sms_numero, null, parts, sentIntents, deliveryIntents);


        } catch (Exception e) {
            Log.e("LogErrorJson", e.toString());
        }
    }
}
