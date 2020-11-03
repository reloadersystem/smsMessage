package pe.reloadersystem.msgenvio;

import android.Manifest;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

        if (ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest
                        .permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {Manifest.permission.SEND_SMS,}, 1000);
        } else {

        }


        btnEnviar.setOnClickListener(this);

    }

    private void enviarMensaje(String numero, String mensaje) {
        try {
            SmsManager sms = SmsManager.getDefault();

            sms.sendTextMessage(numero, null, mensaje, sendPI, deliveredPI);

            // Toast.makeText(this, "Mensaje Enviado", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Mensaje no Enviado" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {

        String numero = "961162784";
        String mensaje = "Reloader System SMS Enviado";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);

//                if (numero.length() == 9) {
//
//                    enviarMensaje(numero, mensaje);
//                } else {
//                    Toast.makeText(this, "Ingrese un número válido", Toast.LENGTH_SHORT).show();
//                }


        } else {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(numero, null, mensaje, null, null);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        smsSendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
          

            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        registerReceiver(smsDeliveredReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
    }
}
