package pe.reloadersystem.msgenvio.Servicios;

import android.app.PendingIntent;
import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import pe.reloadersystem.msgenvio.Servicios.Retrofit.HelperWs;
import pe.reloadersystem.msgenvio.Servicios.Retrofit.MethodWs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebServiceListarSms {


    public static void listarSms(Context context) {

        PendingIntent sendPI = null;
        PendingIntent deliveredPI = null;

        MethodWs methodWs = HelperWs.getConfiguration(context).create(MethodWs.class);
        Call<ResponseBody> responseBodyCall = methodWs.getDatosSMS();
        final PendingIntent finalSendPI = sendPI;
        final PendingIntent finalDeliveredPI = deliveredPI;
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    ResponseBody informacion = response.body();
                    try {
                        String cadena_respuesta = informacion.string();
                        JSONObject respuesta = new JSONObject(cadena_respuesta);
                        JSONArray data = respuesta.getJSONArray("data");
                        for (int a = 0; a < data.length(); a++) {
                            JSONObject obj = (JSONObject) data.get(a);
                            int code = (int) obj.get("sms_id");
                            String sms_destinatario = obj.getString("sms_destinatario");
                            String sms_mensaje = obj.getString("sms_mensaje");

                            SmsManager sms = SmsManager.getDefault();

                            ArrayList<String> parts = sms.divideMessage(sms_mensaje);
                            int numParts = parts.size();

                            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                            ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();

                            for (int i = 0; i < numParts; i++) {
                                sentIntents.add(finalSendPI);
                                deliveryIntents.add(finalDeliveredPI);
                            }
                            sms.sendMultipartTextMessage(sms_destinatario, null, parts, sentIntents, deliveryIntents);
                        }


                        Log.e("LogResponse", cadena_respuesta);

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
