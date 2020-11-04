package pe.reloadersystem.msgenvio.Servicios.Retrofit;

public class ItemPostsms {
    int sms_id;
    String error;

    public ItemPostsms(int sms_id, String error) {
        this.sms_id = sms_id;
        this.error = error;
    }

    @Override
    public String toString() {
        return "Entity_AlumnoSms{" +
                "sms_id=" + sms_id +
                ", error='" + error + '\'' +
                '}';
    }
}
