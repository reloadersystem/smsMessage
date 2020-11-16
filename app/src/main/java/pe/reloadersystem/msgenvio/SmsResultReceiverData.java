package pe.reloadersystem.msgenvio;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import okhttp3.ResponseBody;
import pe.reloadersystem.msgenvio.servicios.Retrofit.HelperWs;
import pe.reloadersystem.msgenvio.servicios.Retrofit.ItemPostsms;
import pe.reloadersystem.msgenvio.servicios.Retrofit.MethodWs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class SmsResultReceiverData extends BroadcastReceiver {

    private static final String EXTRA_NUMBER = "sms_numero";
    private static final String EXTRA_MESSAGE = "sms_message";
    private static final String EXTRA_CODE = "sms_code";
    private static final int ID_SERVICIO = 99;
    private static final String TAG = "SmsResultClass";
    private static final int PERIOD_MS = 5000;

    public void onReceive(Context context, Intent intent) {

        String numero = intent.getStringExtra(EXTRA_NUMBER);
        String mensaje = intent.getStringExtra(EXTRA_MESSAGE);
        int code = intent.getIntExtra(EXTRA_CODE, 0);

        try {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context,
                            "Activity.RESULT_OK",
                            Toast.LENGTH_SHORT).show();
                    updateService(code, "", context);
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "SMS generic failure", Toast.LENGTH_SHORT)
                            .show();
                    updateService(code, "ERROR GENERIC FAILURE", context);

                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "SMS no service", Toast.LENGTH_SHORT)
                            .show();
                    updateService(code, "SMS no service", context);

                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "SMS null PDU", Toast.LENGTH_SHORT).show();
                    updateService(code, "SMS null PDU", context);
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "SMS radio off", Toast.LENGTH_SHORT).show();
                    updateService(code, "SMS radio off", context);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void updateService(final int code, final String resultEnvio, final Context context) {
        ItemPostsms loguinRequest = new ItemPostsms(code, resultEnvio);
        MethodWs methodWs = HelperWs.getConfiguration(context).create(MethodWs.class);
        Call<ResponseBody> responseBodyCall = methodWs.sendUpdateSMS(loguinRequest);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    ResponseBody info = response.body();
                    try {

                        String cadena_respuesta = info.string();
                        Log.e("LogResponse", cadena_respuesta);

                        //{"message":"Se actualizó el estado del registro.","status":true}

                        cancelJob(context);
                        reiniciarServicio(context);

                    } catch (Exception e) {
                        handlerToastMessage(context, "Falto guardar sms enviados", code);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("LogError", t.toString());
            }
        });
    }


    private void handlerToastMessage(final Context context, final String mensaje, final int code) {

        final Handler handler = new Handler(context.getMainLooper());

        handler.post(new Runnable() {
            public void run() {

                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cancelJob(Context context) {

        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(ID_SERVICIO);
        Log.d(TAG, "Job Cancelled");
    }

    public void reiniciarServicio(Context context) {
        ComponentName componentName = new ComponentName(context, WebServicesJobs.class);
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

        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");

        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }

}
