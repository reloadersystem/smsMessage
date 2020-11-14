package pe.reloadersystem.msgenvio;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsResultReceiverData extends BroadcastReceiver {

    private static final String SMS_SENT_ACTION = "pe.reloadersystem.msgenvio.SMS_SENT";
    private static final String SMS_DELIVERED_ACTION = "pe.reloadersystem.msgenvio.SMS_DELIVERED";
    private static final String EXTRA_NUMBER = "number";
    private static final String EXTRA_MESSAGE = "message";
    String resultEnvio;


    @Override
    public void onReceive(Context context, Intent intent) {
        String result = null;

        String resultRecepcionado;

        String action = intent.getAction();

        String numero = intent.getStringExtra(EXTRA_NUMBER);
        String mensaje = intent.getStringExtra(EXTRA_MESSAGE);


        if (SMS_SENT_ACTION.equals(action)) {
            int resultCode = getResultCode();
            resultEnvio = translateSentResult(resultCode);
            Toast.makeText(context, numero + " - " + mensaje + resultEnvio, Toast.LENGTH_SHORT).show();
            if (resultEnvio.equals("Activity.RESULT_OK")) {
                //  updateService(code, "");
                Toast.makeText(context, "Enviado", Toast.LENGTH_SHORT).show();
            } else {
                // updateService(code, resultEnvio);
                Toast.makeText(context, resultEnvio, Toast.LENGTH_SHORT).show();
            }


        } else if (SMS_DELIVERED_ACTION.equals(action)) {
            SmsMessage sms = null;
            byte[] pdu = intent.getByteArrayExtra("pdu");
            String format = intent.getStringExtra("format");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && format != null) {
                sms = SmsMessage.createFromPdu(pdu, format);
            } else {
                sms = SmsMessage.createFromPdu(pdu);
            }

            resultRecepcionado = translateDeliveryStatus(sms.getStatus());

            Toast.makeText(context, resultRecepcionado, Toast.LENGTH_SHORT).show();
        }
    }

    private String translateSentResult(int resultcode) {

        switch (resultcode) {
            case Activity.RESULT_OK:
                return "Activity.RESULT_OK";
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return "SmsManager.RESULT_ERROR_GENERIC_FAILURE";
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return "SmsManager.RESULT_ERROR_RADIO_OFF";
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return "SmsManager.RESULT_ERROR_NULL_PDU";
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return "SmsManager.RESULT_ERROR_NO_SERVICE";
            default:
                return "Unknown error code";
        }
    }

    String translateDeliveryStatus(int status) {
        switch (status) {
            case Telephony.Sms.STATUS_COMPLETE:
                return "Sms.STATUS_COMPLETE";
            case Telephony.Sms.STATUS_FAILED:
                return "Sms.STATUS_FAILED";
            case Telephony.Sms.STATUS_PENDING:
                return "Sms.STATUS_PENDING";
            case Telephony.Sms.STATUS_NONE:
                return "Sms.STATUS_NONE";
            default:
                return "Unknown status code";
        }
    }

    public static void SendMessage(Context context, int code, String sms_destinatario, String sms_mensaje) {

        SmsManager sms;
        int requestCode = 0;

        try {

            sms = SmsManager.getDefault();

            Intent sentIntent = new Intent(SMS_SENT_ACTION);
            Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION);

            sentIntent.putExtra(EXTRA_NUMBER, sms_destinatario);
            sentIntent.putExtra(EXTRA_MESSAGE, sms_mensaje);


            deliveredIntent.putExtra(EXTRA_NUMBER, sms_destinatario);
            deliveredIntent.putExtra(EXTRA_MESSAGE, sms_mensaje);

            requestCode += 1;

            PendingIntent sentPI = PendingIntent.getBroadcast(context,
                    requestCode,
                    sentIntent,
                    PendingIntent.FLAG_ONE_SHOT);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(context,
                    requestCode,
                    deliveredIntent,
                    PendingIntent.FLAG_ONE_SHOT);

            sms.sendTextMessage(sms_destinatario, null, sms_mensaje, sentPI, deliveredPI);


        } catch (Exception e) {
            Log.e("LogErrorJson", e.toString());
        }
    }

}
