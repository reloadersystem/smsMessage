package pe.reloadersystem.msgenvio.entidades;

import com.google.firebase.Timestamp;

public class ESms {

    String code;
    int estado_id;
    String sms_destinatario;
    String sms_envio_error;
    Boolean sms_envio_estado;
    Timestamp sms_envio_fecha;
    Timestamp sms_fecha_registro;
    int sms_id;
    String sms_mensaje;


    public ESms(String code, int estado_id, String sms_destinatario, String sms_envio_error, Boolean sms_envio_estado, Timestamp sms_envio_fecha, Timestamp sms_fecha_registro, int sms_id, String sms_mensaje) {
        this.code = code;
        this.estado_id = estado_id;
        this.sms_destinatario = sms_destinatario;
        this.sms_envio_error = sms_envio_error;
        this.sms_envio_estado = sms_envio_estado;
        this.sms_envio_fecha = sms_envio_fecha;
        this.sms_fecha_registro = sms_fecha_registro;
        this.sms_id = sms_id;
        this.sms_mensaje = sms_mensaje;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
