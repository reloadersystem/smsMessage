package pe.reloadersystem.msgenvio.utils;

import android.content.Context;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import pe.reloadersystem.msgenvio.WebServicesJobs;
import pe.reloadersystem.msgenvio.entidades.ESms;

public class SendMessageLimit {

    SendMessageTo sendMessageTo;
    WebServicesJobs webServicesJobs;
    int contador = 0;


    public void sendMessageTo25(final ArrayList<ESms> listmessage, final Context context) {

        sendMessageTo = new SendMessageTo();
        if (listmessage.size() > 0) {
            int datasms_id = listmessage.get(contador).getSms_id();
            final String code_auto = listmessage.get(contador).getCode();
            int totalSms = listmessage.size();
            if (contador < totalSms) {
                int estado = listmessage.get(contador).getEstado_id();
                String envio_error_sms = listmessage.get(contador).getSms_envio_error();
                Boolean envio_estado_sms = listmessage.get(contador).getSms_envio_estado();
                Timestamp envio_fecha_sms = listmessage.get(contador).getSms_envio_fecha();
                int id_sms = listmessage.get(contador).getSms_id();
                String mensaje_sms = listmessage.get(contador).getSms_mensaje();
                String destinatario_sms = listmessage.get(contador).getSms_destinatario();
                sendMessageTo.SendMessage(code_auto, destinatario_sms, mensaje_sms, id_sms, context);
                contador = contador + 1;
            }
        } else {
            //handlerToastMessage(context, "No hay pendientes", 0);
            // buscaPendientes();
            webServicesJobs.revisarPendientes();
        }
    }
}
