package pe.reloadersystem.msgenvio;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import pe.reloadersystem.msgenvio.Servicios.Retrofit.ItemPostsms;
import pe.reloadersystem.msgenvio.Servicios.WebServiceListarSms;
import pe.reloadersystem.msgenvio.Servicios.WebServiceUpdateSms;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    Button btnEnviar;

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELVERED";
    PendingIntent sendPI, deliveredPI;
    BroadcastReceiver smsSendReceiver, smsDeliveredReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnEnviar = findViewById(R.id.btnEnviarMSG);

        sendPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        WebServiceListarSms.listarSms(this);


        btnEnviar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

//        String numero = "961162784";
        String numero = "997248787";
        String mensaje = "Reloader System SMS Enviado en apagado";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            SmsManager sms = SmsManager.getDefault();



            ArrayList<String> parts  = sms.divideMessage("El dispositivo móvil basado en GSM se encarga de la segmentación en la que se rompen los mensajes a varias partes para enviar en ejecución y también el ensamblaje de los mensajes de varias partes en un mensaje en el recibo.");
//            sms.sendTextMessage(numero, null, parts, sendPI, deliveredPI);
            int numParts = parts.size();

            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();

            for (int i = 0; i < numParts; i++) {
                sentIntents.add(sendPI);
                deliveryIntents.add(deliveredPI);
            }

            sms.sendMultipartTextMessage(numero, null, parts , sentIntents, deliveryIntents);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(smsDeliveredReceiver);
        unregisterReceiver(smsSendReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        smsSendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode()) {
                    case Activity
                            .RESULT_OK:
                        // Toast.makeText(context, "SMS send", Toast.LENGTH_SHORT).show();

                        ItemPostsms loguinRequest = new ItemPostsms(214, "sms_send");

                        WebServiceUpdateSms.updateSms(context, loguinRequest);


                        break;

                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure!", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "sin Servicio", Toast.LENGTH_SHORT).show();
                        break;

                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio Off!", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered!", Toast.LENGTH_SHORT).show();
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(smsSendReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
    }


}
