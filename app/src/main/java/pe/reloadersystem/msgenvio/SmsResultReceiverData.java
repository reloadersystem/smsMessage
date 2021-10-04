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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import androidx.annotation.NonNull;
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
    private static final String EXTRA_CODEID = "sms_codeid";


    private static final int ID_SERVICIO = 99;
    private static final String TAG = "SmsResultClass";
    private static final int PERIOD_MS = 5000;
    FirebaseFirestore mFirestore;

    public void onReceive(Context context, Intent intent) {

        String numero = intent.getStringExtra(EXTRA_NUMBER);
        String mensaje = intent.getStringExtra(EXTRA_MESSAGE);
        String code = intent.getStringExtra(EXTRA_CODE);
        int sms_codeid = intent.getIntExtra(EXTRA_CODEID, 0);

        try {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context,
                            "Activity.RESULT_OK",
                            Toast.LENGTH_SHORT).show();
                    updateService(code, "", context, 2621, numero, mensaje, sms_codeid);
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "SMS generic failure", Toast.LENGTH_SHORT)
                            .show();
                    updateService(code, "ERROR GENERIC FAILURE", context, 2622, numero, mensaje, sms_codeid);

                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "SMS no service", Toast.LENGTH_SHORT)
                            .show();
                    updateService(code, "SMS no service", context, 2622, numero, mensaje, sms_codeid);

                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "SMS null PDU", Toast.LENGTH_SHORT).show();
                    updateService(code, "SMS null PDU", context, 2622, numero, mensaje, sms_codeid);
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "SMS radio off", Toast.LENGTH_SHORT).show();
                    updateService(code, "SMS radio off", context, 2622, numero, mensaje, sms_codeid);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateService(String code, String sms_radio_off, final Context context, int estado_id, String destinatario, String mensaje, int smd_id) {
        mFirestore = FirebaseFirestore.getInstance();

        Log.v("codigo", code);

        Date date = new Date();
        Timestamp fecha_hoy = new Timestamp(date);
        Log.v("hora", fecha_hoy.toString());

        String enviofecha = "1633105898";
        Long t = Long.valueOf(enviofecha);
        Timestamp ts = new Timestamp(t, 0);

        String fecharegistro = "1632934428";
        Long t2 = Long.valueOf(fecharegistro);
        Timestamp ts2 = new Timestamp(t2, 0);

        mFirestore.collection("data").document(code)
                .set(new smsUpdate(
                        estado_id, destinatario, sms_radio_off, true,
                        fecha_hoy, ts2, smd_id,
                        mensaje
                )).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Firebase", "Se guardo el sms correctamente");
                cancelJob(context);
                reiniciarServicio(context);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Log.d("Firebase", e.toString());
            }
        });
    }


    private void updateServices(final int code, final String resultEnvio, final Context context) {
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

    private class smsUpdate {
        int estado_id;
        String sms_destinatario;
        String sms_envio_error;
        Boolean sms_envio_estado;
        Timestamp sms_envio_fecha;
        Timestamp sms_fecha_registro;
        int sms_id;
        String sms_mensaje;

        public smsUpdate(int estado_id, String sms_destinatario, String sms_envio_error, Boolean sms_envio_estado, Timestamp sms_envio_fecha, Timestamp sms_fecha_registro, int sms_id, String sms_mensaje) {
            this.estado_id = estado_id;
            this.sms_destinatario = sms_destinatario;
            this.sms_envio_error = sms_envio_error;
            this.sms_envio_estado = sms_envio_estado;
            this.sms_envio_fecha = sms_envio_fecha;
            this.sms_fecha_registro = sms_fecha_registro;
            this.sms_id = sms_id;
            this.sms_mensaje = sms_mensaje;
        }

        public int getEstado_id() {
            return estado_id;
        }

        public void setEstado_id(int estado_id) {
            this.estado_id = estado_id;
        }

        public String getSms_destinatario() {
            return sms_destinatario;
        }

        public void setSms_destinatario(String sms_destinatario) {
            this.sms_destinatario = sms_destinatario;
        }

        public String getSms_envio_error() {
            return sms_envio_error;
        }

        public void setSms_envio_error(String sms_envio_error) {
            this.sms_envio_error = sms_envio_error;
        }

        public Boolean getSms_envio_estado() {
            return sms_envio_estado;
        }

        public void setSms_envio_estado(Boolean sms_envio_estado) {
            this.sms_envio_estado = sms_envio_estado;
        }

        public Timestamp getSms_envio_fecha() {
            return sms_envio_fecha;
        }

        public void setSms_envio_fecha(Timestamp sms_envio_fecha) {
            this.sms_envio_fecha = sms_envio_fecha;
        }

        public Timestamp getSms_fecha_registro() {
            return sms_fecha_registro;
        }

        public void setSms_fecha_registro(Timestamp sms_fecha_registro) {
            this.sms_fecha_registro = sms_fecha_registro;
        }

        public int getSms_id() {
            return sms_id;
        }

        public void setSms_id(int sms_id) {
            this.sms_id = sms_id;
        }

        public String getSms_mensaje() {
            return sms_mensaje;
        }

        public void setSms_mensaje(String sms_mensaje) {
            this.sms_mensaje = sms_mensaje;
        }
    }

}
