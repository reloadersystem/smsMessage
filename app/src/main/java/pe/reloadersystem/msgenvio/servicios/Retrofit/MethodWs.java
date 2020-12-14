package pe.reloadersystem.msgenvio.servicios.Retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Reloader - Resembrink Correa.
 */

public interface MethodWs {

    @GET("listarSmsPendiente")
    Call<ResponseBody> getDatosSMS();

    @POST("actualizarSmsEnviado")
    Call<ResponseBody> sendUpdateSMS(@Body ItemPostsms entity_alumnoSms);
}
