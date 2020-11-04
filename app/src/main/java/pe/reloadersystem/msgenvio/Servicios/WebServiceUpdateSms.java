package pe.reloadersystem.msgenvio.Servicios;

import android.content.Context;
import android.util.Log;

import okhttp3.ResponseBody;
import pe.reloadersystem.msgenvio.Servicios.Retrofit.ItemPostsms;
import pe.reloadersystem.msgenvio.Servicios.Retrofit.HelperWs;
import pe.reloadersystem.msgenvio.Servicios.Retrofit.MethodWs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebServiceUpdateSms {

    public static void updateSms(Context context, ItemPostsms loguinRequest){

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

                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
