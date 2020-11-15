package pe.reloadersystem.msgenvio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SmsDeliveredReceiver extends BroadcastReceiver {

    private static final String EXTRA_NUMBER = "sms_numero";
    private static final String EXTRA_MESSAGE = "sms_message";

    @Override
    public void onReceive(Context context, Intent intent) {

        String numero = intent.getStringExtra(EXTRA_NUMBER);
        String mensaje = intent.getStringExtra(EXTRA_MESSAGE);


        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Toast.makeText(context, "SMS delivered ID -> "+numero + mensaje, Toast.LENGTH_SHORT).show();
                break;
            case Activity.RESULT_CANCELED:
                Toast.makeText(context, "SMS not delivered ID -> "+numero, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}