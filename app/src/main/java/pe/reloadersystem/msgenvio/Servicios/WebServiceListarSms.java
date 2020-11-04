package pe.reloadersystem.msgenvio.Servicios;

import android.content.Context;
import android.util.Log;

import okhttp3.ResponseBody;
import pe.reloadersystem.msgenvio.Servicios.Retrofit.HelperWs;
import pe.reloadersystem.msgenvio.Servicios.Retrofit.MethodWs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebServiceListarSms {

    public static void listarSms(Context context) {
        MethodWs methodWs = HelperWs.getConfiguration(context).create(MethodWs.class);
        Call<ResponseBody> responseBodyCall = methodWs.getDatosSMS();
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    ResponseBody informacion = response.body();
                    try {
                        String cadena_respuesta = informacion.string();
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
