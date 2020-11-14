package pe.reloadersystem.msgenvio;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import pe.reloadersystem.msgenvio.servicios.Retrofit.HelperWs;
import pe.reloadersystem.msgenvio.servicios.Retrofit.ItemPostsms;
import pe.reloadersystem.msgenvio.servicios.Retrofit.MethodWs;
import pe.reloadersystem.msgenvio.utils.ShareDataRead;
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
    SmsSentReceiver smsSentReceiver;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started");

        sms = SmsManager.getDefault();

//        resultsReceiver = new SmsResultReceiver();
        intentFilter = new IntentFilter(SMS_SENT_ACTION);
        intentFilter.addAction(SMS_DELIVERED_ACTION);
        smsSentReceiver = new SmsSentReceiver();
        doBackWork(jobParameters);

        return true;
    }

    private void doBackWork(final JobParameters params) {

//        displayService();
        revisarPendientes();

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");

        jobCancelled = true;
        return true;
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

                            //SendMessage(code, sms_destinatario, sms_mensaje);
                            SmsResultReceiverData.SendMessage(context,code, sms_destinatario, sms_mensaje);
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

    private void updateService(final int code, final String resultEnvio) {
        ItemPostsms loguinRequest = new ItemPostsms(code, resultEnvio);
        MethodWs methodWs = HelperWs.getConfiguration(getApplicationContext()).create(MethodWs.class);
        Call<ResponseBody> responseBodyCall = methodWs.sendUpdateSMS(loguinRequest);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    ResponseBody info = response.body();
                    try {

                        String cadena_respuesta = info.string();
                        Log.e("LogResponse", cadena_respuesta);

                        SharedPreferences settings = getSharedPreferences("sms_wait", Context.MODE_PRIVATE);
                        settings.edit().clear().commit();

                        ejecutarTarea();

                        //{"message":"Se actualizó el estado del registro.","status":true}

                    } catch (Exception e) {

                        Toast.makeText(context, "Falto guardar sms enviados", Toast.LENGTH_SHORT).show();
                        handlerToastMessage(context, "Falto guardar sms enviados", code);

                        ShareDataRead.guardarValor(getApplicationContext(), "codigo", String.valueOf(code));
                        ShareDataRead.guardarValor(getApplicationContext(), "result", resultEnvio);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

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

    public void ejecutarTarea() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                revisarPendientes();
            }
        }, 3000);
    }

    private void SendMessage(int code, String sms_destinatario, String sms_mensaje) {

        try {

            Intent sentIntent = new Intent(SMS_SENT_ACTION);
            Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION);

            sentIntent.putExtra(EXTRA_NUMBER, sms_destinatario);
            sentIntent.putExtra(EXTRA_MESSAGE, sms_mensaje);


            deliveredIntent.putExtra(EXTRA_NUMBER, sms_destinatario);
            deliveredIntent.putExtra(EXTRA_MESSAGE, sms_mensaje);

            requestCode += 1;

            PendingIntent sentPI = PendingIntent.getBroadcast(this,
                    requestCode,
                    sentIntent,
                    PendingIntent.FLAG_ONE_SHOT);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(this,
                    requestCode,
                    deliveredIntent,
                    PendingIntent.FLAG_ONE_SHOT);

            sms.sendTextMessage(sms_destinatario, null, sms_mensaje, sentPI, deliveredPI);


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

    public class SmsResultReceiver extends BroadcastReceiver {
//
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
                    updateService(code, "");
                } else {
                    updateService(code, resultEnvio);
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
    }



}
