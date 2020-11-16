package pe.reloadersystem.msgenvio;

import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import pe.reloadersystem.msgenvio.servicios.Retrofit.HelperWs;
import pe.reloadersystem.msgenvio.servicios.Retrofit.MethodWs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebServicesJobs extends JobService {

    private static final String TAG = "ExampleJOB";
    private boolean jobCancelled = false;
    private Context context = this;
    private final int TIEMPO = 20000;
    Handler handler = new Handler();
    int datostosend = 0;

    private SmsManager sms;
    public BroadcastReceiver resultsReceiver;
    public IntentFilter intentFilter;

    private static final String SMS_SENT_ACTION = "pe.reloadersystem.msgenvio.SMS_SENT";
    private static final String SMS_DELIVERED_ACTION = "pe.reloadersystem.msgenvio.SMS_DELIVERED";
    private static final String EXTRA_NUMBER = "number";
    private static final String EXTRA_MESSAGE = "message";

    int requestCode;
    int code;
    String resultEnvio;
    SmsResultReceiverData smsSentReceiver;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started");

        sms = SmsManager.getDefault();
        smsSentReceiver = new SmsResultReceiverData();
        doBackWork(jobParameters);

        return true;
    }

    private void doBackWork(final JobParameters params) {

        revisarPendientes();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");

        jobCancelled = true;
        return true;
    }


    private void revisarPendientes() {

        MethodWs methodWs = HelperWs.getConfiguration(this).create(MethodWs.class);
        Call<ResponseBody> responseBodyCall = methodWs.getDatosSMS();
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    ResponseBody informacion = response.body();
                    try {
                        String cadena_respuesta = informacion.string();
                        JSONObject verifydata = new JSONObject(cadena_respuesta);
                        Boolean estado = Boolean.valueOf(verifydata.getString("status"));
                        if (estado) {

                            JSONArray data = verifydata.getJSONArray("data");
                            datostosend = data.length();
                            Log.d("datostosend", String.valueOf(datostosend));

                            code = (int) ((JSONObject) data.get(0)).get("sms_id");
                            String sms_destinatario = ((JSONObject) data.get(0)).getString("sms_destinatario");
                            String sms_mensaje = ((JSONObject) data.get(0)).getString("sms_mensaje");

                            SendMessage(code, sms_destinatario, sms_mensaje);

                        } else {
                            // Toast.makeText(context, "No hay pendientes", Toast.LENGTH_SHORT).show();
                            handlerToastMessage(context, "No hay pendientes", 0);
                            buscaPendientes();

                        }
                    } catch (Exception e) {
                        Log.e("LogResponseError", e.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Error de Conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void buscaPendientes() {
        handler.postDelayed(new Runnable() {
            public void run() {
                revisarPendientes();
            }
        }, TIEMPO);
    }


    private void SendMessage(int code, String sms_numero, String sms_message) {

        try {

            requestCode += 1;

            Intent sendPIntent = new Intent(context, SmsResultReceiverData.class);
            sendPIntent.putExtra("sms_numero", sms_numero);
            sendPIntent.putExtra("sms_message", sms_message);
            sendPIntent.putExtra("sms_code", code);


            PendingIntent sentPI = PendingIntent.getBroadcast(context, requestCode,
                    sendPIntent, PendingIntent.FLAG_ONE_SHOT);


            Intent intentDPi = new Intent(context, SmsDeliveredReceiver.class);
            intentDPi.putExtra("sms_numero", sms_numero);
            intentDPi.putExtra("sms_message", sms_message);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(context, requestCode,
                    intentDPi, PendingIntent.FLAG_ONE_SHOT);

            sms.sendTextMessage(sms_numero, null, sms_message, sentPI, deliveredPI);


        } catch (Exception e) {
            Log.e("LogErrorJson", e.toString());
        }
    }

    private void handlerToastMessage(final Context context, final String mensaje, final int code) {

        final Handler handler = new Handler(context.getMainLooper());

        handler.post(new Runnable() {
            public void run() {

                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }
}