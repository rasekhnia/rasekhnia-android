package ir.rasekhnia.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/** Verify the actual WordPress authentication cookie on the server. Never trust a local flag. */
public final class SessionGate {
    private static final String ENDPOINT="https://rasekhnia.ir/wp-admin/admin-ajax.php?action=rki_app_session_status";
    public interface Callback { void result(boolean authenticated, boolean reachable); }
    private SessionGate() {}
    public static void check(Activity activity, Callback callback) {
        CookieManager.getInstance().flush();
        String rootCookies=CookieManager.getInstance().getCookie("https://rasekhnia.ir/");
        String wwwCookies=CookieManager.getInstance().getCookie("https://www.rasekhnia.ir/");
        final String cookies=(rootCookies!=null && !rootCookies.isEmpty())?rootCookies:wwwCookies;
        new Thread(() -> {
            boolean authenticated=false, reachable=false;
            HttpURLConnection conn=null;
            try {
                conn=(HttpURLConnection)new URL(ENDPOINT).openConnection();
                conn.setConnectTimeout(9000); conn.setReadTimeout(9000);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("Accept","application/json");
                if(cookies!=null) conn.setRequestProperty("Cookie",cookies);
                if(conn.getResponseCode()==200){
                    try(InputStream in=conn.getInputStream()) {
                        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); byte[] buffer=new byte[1024]; int n;
                        while((n=in.read(buffer))!=-1){if(bytes.size()+n>8192)throw new java.io.IOException("Response too large");bytes.write(buffer,0,n);}
                        JSONObject response=new JSONObject(new String(bytes.toByteArray(),StandardCharsets.UTF_8));
                        reachable=response.optBoolean("ready",false);
                        authenticated=reachable && response.optBoolean("authenticated",false);
                    }
                }
            } catch(Exception ignored) {} finally { if(conn!=null)conn.disconnect(); }
            final boolean auth=authenticated, online=reachable;
            new Handler(Looper.getMainLooper()).post(()->{if(!activity.isFinishing())callback.result(auth,online);});
        }).start();
    }
    public static void openLogin(Activity activity) {
        Intent intent=new Intent(activity,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent); activity.finish();
    }
}
