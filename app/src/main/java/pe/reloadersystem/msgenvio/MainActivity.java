package pe.reloadersystem.msgenvio;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import pe.reloadersystem.msgenvio.entidades.ESms;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;


    private static final String SMS_SENT_ACTION = "pe.reloadersystem.msgenvio.SMS_SENT";
    private static final String SMS_DELIVERED_ACTION = "pe.reloadersystem.msgenvio.SMS_DELIVERED";
    private static final String EXTRA_NUMBER = "number";
    private static final String EXTRA_MESSAGE = "message";
    private SmsManager sms;
    private BroadcastReceiver resultsReceiver;
    private IntentFilter intentFilter;
    int requestCode;
    int code;
    String resultEnvio;
    int smsCount = 0;
    int datostosend = 0;
    Handler handler = new Handler();
    private final int TIEMPO = 20000;

    private static final int ID_SERVICIO = 99;
    private static final int PERIOD_MS = 10000;
    private static final String TAG = "MainActivity";

    Button btn_schudlejob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btn_schudlejob = findViewById(R.id.btn_schudlejob);


//        sms = SmsManager.getDefault();
//        resultsReceiver = new SmsResultReceiver();
//
//        intentFilter = new IntentFilter(SMS_SENT_ACTION);
//        intentFilter.addAction(SMS_DELIVERED_ACTION);

        // btnEnviar.setOnClickListener(this);
        btn_schudlejob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 beginJob();

                //smsFirebase();
            }
        });
    }

    @Override
    public void onClick(View view) {

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
//                    MY_PERMISSIONS_REQUEST_SEND_SMS);
//        } else {

//            SharedPreferences sharpref = getSharedPreferences("sms_wait", MODE_PRIVATE);
//
//            if (sharpref.contains("codigo")) {
//                int codigo = Integer.parseInt(ShareDataRead.obtenerValor(getApplicationContext(), "codigo"));
//                String result = ShareDataRead.obtenerValor(getApplicationContext(), "result");
//                updateService(codigo, result);
//            } else {
//                revisarPendientes();
//            }
//        }

    }

//    private void revisarPendientes() {
//
//        MethodWs methodWs = HelperWs.getConfiguration(this).create(MethodWs.class);
//        Call<ResponseBody> responseBodyCall = methodWs.getDatosSMS();
//        responseBodyCall.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//
//                if (response.isSuccessful()) {
//                    ResponseBody informacion = response.body();
//                    try {
//                        String cadena_respuesta = informacion.string();
//                        JSONObject verifydata = new JSONObject(cadena_respuesta);
//                        Boolean estado = Boolean.valueOf(verifydata.getString("status"));
//                        if (estado) {
//
//                            JSONArray data = verifydata.getJSONArray("data");
//                            datostosend = data.length();
//                            Log.d("datostosend", String.valueOf(datostosend));
//
//                            code = (int) ((JSONObject) data.get(0)).get("sms_id");
//                            String sms_destinatario = ((JSONObject) data.get(0)).getString("sms_destinatario");
//                            String sms_mensaje = ((JSONObject) data.get(0)).getString("sms_mensaje");
//
//                            SendMessage(code, sms_destinatario, sms_mensaje);
//                        } else {
//                            Toast.makeText(MainActivity.this, "No hay pendientes", Toast.LENGTH_SHORT).show();
//                            buscaPendientes();
//
//                        }
//                    } catch (Exception e) {
//                        Log.e("LogResponseError", e.toString());
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Toast.makeText(MainActivity.this, "Error de Conexión", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void SendMessage(int code, String sms_destinatario, String sms_mensaje) {
//
//        try {
//
//            Intent sentIntent = new Intent(SMS_SENT_ACTION);
//            Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION);
//
//            sentIntent.putExtra(EXTRA_NUMBER, sms_destinatario);
//            sentIntent.putExtra(EXTRA_MESSAGE, sms_mensaje);
//
//
//            deliveredIntent.putExtra(EXTRA_NUMBER, sms_destinatario);
//            deliveredIntent.putExtra(EXTRA_MESSAGE, sms_mensaje);
//
//            requestCode += 1;
//
//            PendingIntent sentPI = PendingIntent.getBroadcast(this,
//                    requestCode,
//                    sentIntent,
//                    PendingIntent.FLAG_ONE_SHOT);
//
//            PendingIntent deliveredPI = PendingIntent.getBroadcast(this,
//                    requestCode,
//                    deliveredIntent,
//                    PendingIntent.FLAG_ONE_SHOT);
//
//            sms.sendTextMessage(sms_destinatario, null, sms_mensaje, sentPI, deliveredPI);
//
//
//        } catch (Exception e) {
//            Log.e("LogErrorJson", e.toString());
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (resultsReceiver != null) {
//            unregisterReceiver(resultsReceiver);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (resultsReceiver != null) {
//            registerReceiver(resultsReceiver, intentFilter);
//        }
    }


//    private class SmsResultReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String result = null;
//
//            String resultRecepcionado;
//
//            String action = intent.getAction();
//
//            String numero = intent.getStringExtra(EXTRA_NUMBER);
//            String mensaje = intent.getStringExtra(EXTRA_MESSAGE);
//
//
//            if (SMS_SENT_ACTION.equals(action)) {
//                int resultCode = getResultCode();
//                resultEnvio = translateSentResult(resultCode);
//                Toast.makeText(context, numero + " - " + mensaje + resultEnvio, Toast.LENGTH_SHORT).show();
//                if (resultEnvio.equals("Activity.RESULT_OK")) {
//                    updateService(code, "");
//                } else {
//                    updateService(code, resultEnvio);
//                }
//
//
//            } else if (SMS_DELIVERED_ACTION.equals(action)) {
//                SmsMessage sms = null;
//                byte[] pdu = intent.getByteArrayExtra("pdu");
//                String format = intent.getStringExtra("format");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && format != null) {
//                    sms = SmsMessage.createFromPdu(pdu, format);
//                } else {
//                    sms = SmsMessage.createFromPdu(pdu);
//                }
//
//                resultRecepcionado = translateDeliveryStatus(sms.getStatus());
//
//                Toast.makeText(context, resultRecepcionado, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void updateService(final int code, final String resultEnvio) {
//        ItemPostsms loguinRequest = new ItemPostsms(code, resultEnvio);
//        MethodWs methodWs = HelperWs.getConfiguration(getApplicationContext()).create(MethodWs.class);
//        Call<ResponseBody> responseBodyCall = methodWs.sendUpdateSMS(loguinRequest);
//        responseBodyCall.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//
//                if (response.isSuccessful()) {
//                    ResponseBody info = response.body();
//                    try {
//
//                        String cadena_respuesta = info.string();
//                        Log.e("LogResponse", cadena_respuesta);
//
//                        SharedPreferences settings = getSharedPreferences("sms_wait", Context.MODE_PRIVATE);
//                        settings.edit().clear().commit();
//
//                        ejecutarTarea();
//
//                        //{"message":"Se actualizó el estado del registro.","status":true}
//
//                    } catch (Exception e) {
//
//                        Toast.makeText(MainActivity.this, "Falto guardar sms enviados", Toast.LENGTH_SHORT).show();
//
//                        ShareDataRead.guardarValor(getApplicationContext(), "codigo", String.valueOf(code));
//                        ShareDataRead.guardarValor(getApplicationContext(), "result", resultEnvio);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//            }
//        });
//    }
//
//
//    private String translateSentResult(int resultcode) {
//
//        switch (resultcode) {
//            case Activity.RESULT_OK:
//                return "Activity.RESULT_OK";
//            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//                return "SmsManager.RESULT_ERROR_GENERIC_FAILURE";
//            case SmsManager.RESULT_ERROR_RADIO_OFF:
//                return "SmsManager.RESULT_ERROR_RADIO_OFF";
//            case SmsManager.RESULT_ERROR_NULL_PDU:
//                return "SmsManager.RESULT_ERROR_NULL_PDU";
//            case SmsManager.RESULT_ERROR_NO_SERVICE:
//                return "SmsManager.RESULT_ERROR_NO_SERVICE";
//            default:
//                return "Unknown error code";
//        }
//    }
//
//    String translateDeliveryStatus(int status) {
//        switch (status) {
//            case Telephony.Sms.STATUS_COMPLETE:
//                return "Sms.STATUS_COMPLETE";
//            case Telephony.Sms.STATUS_FAILED:
//                return "Sms.STATUS_FAILED";
//            case Telephony.Sms.STATUS_PENDING:
//                return "Sms.STATUS_PENDING";
//            case Telephony.Sms.STATUS_NONE:
//                return "Sms.STATUS_NONE";
//            default:
//                return "Unknown status code";
//        }
//    }
//
//
//    public void ejecutarTarea() {
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                revisarPendientes();
//            }
//        }, 3000);
//    }
//
//    public void buscaPendientes() {
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                revisarPendientes();
//            }
//        }, TIEMPO);
//    }

    public void beginJob() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {

            ComponentName componentName = new ComponentName(this, WebServicesJobs.class);
            JobInfo info;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                info = new JobInfo.Builder(ID_SERVICIO, componentName)
                        .setPersisted(true)
                        .setMinimumLatency(PERIOD_MS)
                        .build();
            } else {
                info = new JobInfo.Builder(ID_SERVICIO, componentName)
                        .setPersisted(true)
                        .setPeriodic(PERIOD_MS)
                        .build();
            }

            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "Job scheduled");

            } else {
                Log.d(TAG, "Job scheduling failed");
            }
        }
    }

    public void cancelJob(View v) {

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(ID_SERVICIO);
        Log.d(TAG, "Job Cancelled");
    }


}