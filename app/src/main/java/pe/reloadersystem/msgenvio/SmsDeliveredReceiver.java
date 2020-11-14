package pe.reloadersystem.msgenvio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SmsDeliveredReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String sms_id = (intent.getStringExtra("sms_id")!=null)?intent.getStringExtra("sms_id"):"";
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Toast.makeText(context, "SMS delivered ID -> "+sms_id, Toast.LENGTH_SHORT).show();
                break;
            case Activity.RESULT_CANCELED:
                Toast.makeText(context, "SMS not delivered ID -> "+sms_id, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}