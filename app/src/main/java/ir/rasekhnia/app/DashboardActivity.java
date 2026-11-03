package ir.rasekhnia.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class DashboardActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private JSONObject config;
    private int generation=0;
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
    private int color(String key, int fallback){try{return Color.parseColor(config.getString(key));}catch(Exception e){return fallback;}}
    private String value(String key,String fallback){return config==null?fallback:config.optString(key,fallback);}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView label(String content,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(content);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);t.setTextDirection(View.TEXT_DIRECTION_RTL);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private void open(String url){Intent i=new Intent(this,MainActivity.class);i.putExtra("target_url",url);startActivity(i);}
    private void tile(LinearLayout root,String title,String description,String url,int tileColor,int radius,int scale){
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setGravity(Gravity.CENTER);panel.setPadding(dp(12),dp(16),dp(12),dp(16));panel.setBackground(bg(tileColor,radius));
        panel.addView(label(title,Math.max(14,19*scale/100),Color.WHITE,true));
        TextView sub=label(description,Math.max(10,13*scale/100),Color.rgb(190,211,223),false);LinearLayout.LayoutParams subParams=new LinearLayout.LayoutParams(-1,-2);subParams.topMargin=dp(5);panel.addView(sub,subParams);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,dp(94));params.bottomMargin=dp(10);root.addView(panel,params);panel.setOnClickListener(v->open(url));
    }
    private void render(){
        final int navy=color("background",Color.rgb(16,31,52)),tileColor=color("card",Color.rgb(30,57,81)),cyan=color("accent",Color.rgb(105,219,218));
        int scale=config==null?100:config.optInt("font_scale",100),radius=config==null?16:config.optInt("corner_radius",16);
        getWindow().setStatusBarColor(navy);getWindow().setNavigationBarColor(navy);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(navy);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(20),dp(30),dp(20),dp(20));
        String logo=value("logo_url","");
        if(logo.startsWith("https://rasekhnia.ir/")||logo.startsWith("https://www.rasekhnia.ir/"))loadLogo(logo,root,++generation);
        root.addView(label("⚖  "+value("name","راسخ‌نیا"),29*scale/100,Color.WHITE,true));
        if(config==null||config.optBoolean("show_subtitle",true)){
            TextView subtitle=label(value("subtitle","سامانه هوشمند خدمات حقوقی"),15*scale/100,cyan,false);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.bottomMargin=dp(22);root.addView(subtitle,sp);
        }
        if(config==null||config.optBoolean("show_welcome",true)){
            TextView welcome=label(value("welcome","خدمات حقوقی، آموزش و مدیریت پرونده‌ها"),16*scale/100,Color.WHITE,false);welcome.setPadding(dp(12),dp(20),dp(12),dp(20));welcome.setBackground(bg(tileColor,radius));LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(-1,-2);wp.bottomMargin=dp(18);root.addView(welcome,wp);
        }
        String[][] defaults={{"مراجعین و مشتریان","سفارش‌ها، قراردادها و پرونده‌ها","https://rasekhnia.ir/my-account/"},{"وکلا","ورود به حساب و خدمات وکالت","https://rasekhnia.ir/my-account/"},{"بازاریابان","ورود به حساب همکاری و فروش","https://rasekhnia.ir/my-account/"},{"خدمات حقوقی","قراردادها، دادخواست‌ها و خدمات","https://rasekhnia.ir/"},{"مقالات حقوقی","مطالعه و آموزش","https://rasekhnia.ir/"},{"سایر امکانات","تلویزیون حقوقی، کلاس‌ها و سایت","https://rasekhnia.ir/"}};
        JSONArray tiles=config==null?null:config.optJSONArray("tiles");
        for(int i=0;i<defaults.length;i++){
            JSONObject t=tiles==null?null:tiles.optJSONObject(i);if(t!=null&&!t.optBoolean("enabled",true))continue;
            String title=t==null?defaults[i][0]:t.optString("title",defaults[i][0]);
            String subtitle=t==null?defaults[i][1]:t.optString("subtitle",defaults[i][1]);
            String url=t==null?defaults[i][2]:t.optString("url",defaults[i][2]);
            if(!(url.startsWith("https://rasekhnia.ir/")||url.startsWith("https://www.rasekhnia.ir/")))url="https://rasekhnia.ir/";
            tile(root,title,subtitle,url,tileColor,radius,scale);
        }
        if(config!=null&&config.optBoolean("show_footer",true))root.addView(label(value("footer_text","© راسخ‌نیا"),12*scale/100,Color.LTGRAY,false));
        scroll.addView(root);setContentView(scroll);
    }
    private void loadLogo(String address,LinearLayout parent,int id){
        new Thread(()->{HttpURLConnection conn=null;try{conn=(HttpURLConnection)new URL(address).openConnection();conn.setConnectTimeout(7000);conn.setReadTimeout(7000);Bitmap bitmap;try(InputStream in=conn.getInputStream()){bitmap=BitmapFactory.decodeStream(in);}if(bitmap!=null)handler.post(()->{if(id!=generation||isFinishing())return;ImageView img=new ImageView(this);img.setImageBitmap(bitmap);img.setScaleType(ImageView.ScaleType.FIT_CENTER);parent.addView(img,0,new LinearLayout.LayoutParams(-1,dp(90)));});}catch(Exception ignored){}finally{if(conn!=null)conn.disconnect();}}).start();
    }
    private void refresh(){
        new Thread(()->{HttpURLConnection conn=null;try{
            conn=(HttpURLConnection)new URL("https://rasekhnia.ir/wp-json/rki-android/v1/config").openConnection();conn.setConnectTimeout(8000);conn.setReadTimeout(8000);
            if(conn.getResponseCode()!=200)return;
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(InputStream in=conn.getInputStream()){byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1){if(bytes.size()+n>131072)return;bytes.write(buf,0,n);}}
            JSONObject received=new JSONObject(new String(bytes.toByteArray(),StandardCharsets.UTF_8));handler.post(()->{if(!isFinishing()){config=received;render();}});
        }catch(Exception ignored){}finally{if(conn!=null)conn.disconnect();}}).start();
    }
    @Override public void onCreate(Bundle state){super.onCreate(state);render();refresh();}
    @Override protected void onResume(){super.onResume();if(config!=null)refresh();}
}
