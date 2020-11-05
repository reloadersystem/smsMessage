package pe.reloadersystem.msgenvio;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.ResponseBody;
import pe.reloadersystem.msgenvio.Servicios.Retrofit.HelperWs;
import pe.reloadersystem.msgenvio.Servicios.Retrofit.MethodWs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    Button btnEnviar;
    String sms_destinatario;
    String sms_mensaje;

    private static final String SMS_SENT_ACTION = "com.mycompany.myapp.SMS_SENT";
    private static final String SMS_DELIVERED_ACTION = "com.mycompany.myapp.SMS_DELIVERED";
    private int requestCode = 33;

    private static final String EXTRA_NUMBER = "number";
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_ID = "sms_id";

    private BroadcastReceiver resultsReceiver;
    private IntentFilter intentFilter;

    int count = 0;
    String cadena_respuesta;

    Handler handler = new Handler();

    private final int TIEMPO = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnEnviar = findViewById(R.id.btnEnviarMSG);
        resultsReceiver = new SmsResultReceiver();
        intentFilter = new IntentFilter(SMS_SENT_ACTION);
        intentFilter.addAction(SMS_DELIVERED_ACTION);

        btnEnviar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {

            MethodWs methodWs = HelperWs.getConfiguration(this).create(MethodWs.class);
            Call<ResponseBody> responseBodyCall = methodWs.getDatosSMS();
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if (response.isSuccessful()) {
                        ResponseBody informacion = response.body();
                        try {
                            cadena_respuesta = informacion.string();

                            SendMessage(cadena_respuesta);


                        } catch (Exception e) {
                            Log.e("LogResponseError", e.toString());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }

    private void SendMessage(String cadena_respuesta) {

        try {
            JSONObject respuesta = new JSONObject(cadena_respuesta);
            JSONArray data = respuesta.getJSONArray("data");

            String datostosend = String.valueOf(data.length());
            Log.d("datostosend", datostosend);

            if (count < data.length()) {
                int code = (int) ((JSONObject) data.get(count)).get("sms_id");
                sms_destinatario = ((JSONObject) data.get(count)).getString("sms_destinatario");
                sms_mensaje = ((JSONObject) data.get(count)).getString("sms_mensaje");

                SmsManager sms = SmsManager.getDefault();

                Intent sentIntent = new Intent(SMS_SENT_ACTION);
                Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION);

                sentIntent.putExtra(EXTRA_NUMBER, sms_destinatario);
                sentIntent.putExtra(EXTRA_MESSAGE, sms_mensaje);
                sentIntent.putExtra(EXTRA_ID, code);

                deliveredIntent.putExtra(EXTRA_NUMBER, sms_destinatario);
                deliveredIntent.putExtra(EXTRA_MESSAGE, sms_mensaje);
                deliveredIntent.putExtra(EXTRA_ID, code);

                ArrayList<String> parts = sms.divideMessage(sms_mensaje);
                int numParts = parts.size();

                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();

                PendingIntent sentPI = PendingIntent.getBroadcast(this,
                        requestCode,
                        sentIntent,
                        PendingIntent.FLAG_ONE_SHOT);

                PendingIntent deliveredPI = PendingIntent.getBroadcast(this,
                        requestCode,
                        deliveredIntent,
                        PendingIntent.FLAG_ONE_SHOT);

                for (int i = 0; i < numParts; i++) {
                    sentIntents.add(sentPI);
                    deliveryIntents.add(deliveredPI);
                    sms.sendMultipartTextMessage(sms_destinatario, null, parts, sentIntents, deliveryIntents);
                }

            }
        } catch (Exception e) {
            Log.e("LogErrorJson", e.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (resultsReceiver != null) {
            unregisterReceiver(resultsReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (resultsReceiver != null) {
            registerReceiver(resultsReceiver, intentFilter);
        }
    }


    private class SmsResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String result = null;
            String action = intent.getAction();

            String number = intent.getStringExtra(EXTRA_NUMBER);
            String message = intent.getStringExtra(EXTRA_MESSAGE);
            String sms_id = intent.getStringExtra(EXTRA_ID);

            if (SMS_SENT_ACTION.equals(action)) {
                int resultCode = getResultCode();
                try {
                    JSONObject body = new JSONObject();
                    body.put(EXTRA_NUMBER, number);
                    body.put(EXTRA_MESSAGE, message);
                    body.put(EXTRA_ID, sms_id);
                    body.put("resultCode", resultCode);
                    result = translateSentResult(body);
                    Toast.makeText(context, number + " - " + message + result, Toast.LENGTH_SHORT).show();

                    ejecutarTarea();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(context, "Broadcast de envio", Toast.LENGTH_SHORT).show();
                }
            } else if (SMS_DELIVERED_ACTION.equals(action)) {
                SmsMessage sms = null;
                byte[] pdu = intent.getByteArrayExtra("pdu");
                String format = intent.getStringExtra("format");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && format != null) {
//                        sms = SmsMessage.createFromPdu(pdu, format);
                    sms = SmsMessage.createFromPdu(pdu);
                } else {
                    sms = SmsMessage.createFromPdu(pdu);
                }

                result = "Delivery result : " + translateDeliveryStatus(sms.getStatus());

                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private String translateSentResult(JSONObject params) {
        String mensaje = null;
        System.out.println(params);
        try {
            switch (params.getInt("resultCode")) {
                case Activity.RESULT_OK:
                    mensaje = "Activity.RESULT_OK";
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    mensaje = "SmsManager.RESULT_ERROR_GENERIC_FAILURE";
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    mensaje = "SmsManager.RESULT_ERROR_RADIO_OFF";
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    mensaje = "SmsManager.RESULT_ERROR_NULL_PDU";
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    mensaje = "SmsManager.RESULT_ERROR_NO_SERVICE";
                    break;
                default:
                    mensaje = "Unknown error code";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("SINCODIGO", ex.getMessage());
        }

        return mensaje;
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


    public void ejecutarTarea() {
        handler.postDelayed(new Runnable() {
            public void run() {

                count = count + 1;
                SendMessage(cadena_respuesta);

                handler.postDelayed(this, TIEMPO);
            }

        }, TIEMPO);

    }
}