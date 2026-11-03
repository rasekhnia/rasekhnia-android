package ir.rasekhnia.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DashboardActivity extends Activity {
    private static final String HOME="https://rasekhnia.ir/";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private JSONObject config;
    private FrameLayout shell;
    private LinearLayout drawer;
    private int activeTab=0;
    private int dp(float x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
    private int color(String key,int fallback){try{return Color.parseColor(config.getString(key));}catch(Exception e){return fallback;}}
    private String val(String key,String fallback){return config==null?fallback:config.optString(key,fallback);}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);t.setTextDirection(View.TEXT_DIRECTION_RTL);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private void open(String url){if(!url.startsWith("https://rasekhnia.ir/")&&!url.startsWith("https://www.rasekhnia.ir/"))url=HOME;Intent i=new Intent(this,MainActivity.class);i.putExtra("target_url",url);startActivity(i);}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
    private TextView action(String label,int size,int fg,int bg,int radius){TextView t=text(label,size,fg,true);t.setGravity(Gravity.CENTER);t.setBackground(shape(bg,radius));return t;}
    private String glyph(String key){switch(key){case "scale":return "⚖";case "users":return "♙";case "file-text":return "▤";case "book-open":return "▣";case "graduation-cap":return "✦";case "tv":return "▣";case "headphones":return "☎";case "calendar-days":return "▦";case "shopping-bag":return "▧";case "bell":return "♢";case "handshake":return "♧";default:return "◆";}}
    private JSONArray items(){JSONArray a=config==null?null:config.optJSONArray("tiles");if(a!=null)return a;JSONArray defaults=new JSONArray();String[][] data={{"قراردادساز","تنظیم قرارداد حرفه‌ای","file-text",HOME},{"دادخواست و شکوائیه","تنظیم اوراق قضایی","scale",HOME},{"مطالعه پرونده","بررسی تخصصی اسناد","book-open",HOME},{"چکاپ حقوقی","ارزیابی کسب‌وکار","users",HOME},{"مقالات حقوقی","آموزش و آگاهی","graduation-cap",HOME},{"حساب کاربری","مدیریت سفارش‌ها","users",HOME+"my-account/"}};for(String[] d:data){JSONObject o=new JSONObject();try{o.put("title",d[0]);o.put("subtitle",d[1]);o.put("icon",d[2]);o.put("url",d[3]);}catch(Exception ignored){}defaults.put(o);}return defaults;}
    private LinearLayout card(JSONObject o,int cardColor,int accent,int radius,boolean compact){LinearLayout c=vertical();c.setPadding(dp(12),dp(13),dp(12),dp(12));c.setBackground(shape(cardColor,radius));c.setGravity(Gravity.CENTER_VERTICAL);String title=o.optString("title","خدمات حقوقی"),desc=o.optString("subtitle","");boolean showIcons=config==null||config.optBoolean("show_icons",true),showDesc=config==null||config.optBoolean("show_descriptions",true);if(showIcons){TextView ico=text(glyph(o.optString("icon","grid")),compact?23:27,accent,true);c.addView(ico);}TextView name=text(title,compact?13:15,Color.WHITE,true);name.setMaxLines(2);c.addView(name);if(showDesc&&!desc.isEmpty()){TextView sub=text(desc,11,Color.rgb(184,211,225),false);sub.setMaxLines(2);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(5);c.addView(sub,p);}c.setOnClickListener(v->open(o.optString("url",HOME)));return c;}
    private void render(){
        int navy=color("background",0xFF101F34),cardColor=color("card",0xFF1E3951),accent=color("accent",0xFF69DBDA),radius=config==null?16:config.optInt("corner_radius",16);
        getWindow().setStatusBarColor(navy);getWindow().setNavigationBarColor(navy);
        shell=new FrameLayout(this);shell.setBackgroundColor(0xFFF4F8FA);
        LinearLayout page=vertical();page.setBackgroundColor(0xFFF4F8FA);shell.addView(page,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);header.setPadding(dp(18),dp(14),dp(18),dp(14));header.setBackgroundColor(navy);
        TextView menu=action("☰",25,Color.WHITE,0xFF1E3951,12);header.addView(menu,new LinearLayout.LayoutParams(dp(46),dp(46)));menu.setOnClickListener(v->showDrawer());
        LinearLayout brand=vertical();brand.setPadding(dp(14),0,dp(14),0);brand.addView(text(val("name","راسخ‌نیا"),21,Color.WHITE,true));brand.addView(text(val("subtitle","سامانه هوشمند خدمات حقوقی"),11,accent,false));header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView notification=action("♢",23,accent,0xFF1E3951,12);header.addView(notification,new LinearLayout.LayoutParams(dp(46),dp(46)));notification.setOnClickListener(v->open(HOME));page.addView(header);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);page.addView(sc,new LinearLayout.LayoutParams(-1,0,1));LinearLayout body=vertical();body.setPadding(dp(18),dp(18),dp(18),dp(22));sc.addView(body);
        LinearLayout hero=vertical();hero.setPadding(dp(18),dp(20),dp(18),dp(20));hero.setBackground(shape(navy,20));hero.addView(text("خوش آمدید به راسخ‌نیا",13,accent,true));TextView title=text(val("welcome","راهکارهای حقوقی، در دسترس شما"),22,Color.WHITE,true);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=dp(9);hero.addView(title,tp);TextView intro=text("از تنظیم قرارداد تا مدیریت پرونده، همه در یک مکان",12,0xFFD0E0E9,false);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,-2);ip.topMargin=dp(8);hero.addView(intro,ip);TextView go=action("مشاهده همه خدمات  ←",13,navy,accent,14);LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-2,dp(40));gp.topMargin=dp(16);hero.addView(go,gp);go.setPadding(dp(16),0,dp(16),0);go.setOnClickListener(v->open(HOME));body.addView(hero);
        TextView search=action("⌕   جست‌وجوی خدمات و مقالات",14,0xFF526D7D,Color.WHITE,14);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(52));sp.topMargin=dp(15);body.addView(search,sp);search.setOnClickListener(v->open(HOME+"?s="));
        TextView heading=text("خدمات پرکاربرد",19,0xFF183449,true);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=dp(22);hp.bottomMargin=dp(12);body.addView(heading,hp);
        String layout=val("layout","grid2");int cols=layout.equals("grid3")?3:layout.equals("list")?1:2;int gap=config==null?10:Math.max(4,Math.min(24,config.optInt("tile_gap",10)));
        GridLayout grid=new GridLayout(this);grid.setColumnCount(cols);grid.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);grid.setUseDefaultMargins(false);body.addView(grid,new LinearLayout.LayoutParams(-1,-2));
        JSONArray tiles=items();int shown=0;for(int i=0;i<Math.min(tiles.length(),60);i++){JSONObject o=tiles.optJSONObject(i);if(o==null||!o.optBoolean("enabled",true))continue;final int index=shown++;GridLayout.LayoutParams lp=new GridLayout.LayoutParams(GridLayout.spec(index/cols),GridLayout.spec(index%cols,1f));lp.width=0;lp.height=dp(cols==1?100:cols==3?116:132);lp.setGravity(Gravity.FILL);lp.bottomMargin=dp(gap);if(index%cols<cols-1)lp.leftMargin=dp(gap);grid.addView(card(o,cardColor,accent,radius,cols==3),lp);}
        TextView education=text("مجله و آموزش حقوقی",18,0xFF183449,true);LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2);ep.topMargin=dp(16);ep.bottomMargin=dp(10);body.addView(education,ep);
        TextView magazine=action("▣  مقالات، دوره‌ها و تلویزیون حقوقی     ←",13,0xFF17546A,0xFFE1F0F2,14);magazine.setPadding(dp(12),0,dp(12),0);body.addView(magazine,new LinearLayout.LayoutParams(-1,dp(65)));magazine.setOnClickListener(v->open(HOME));
        if(config!=null&&config.optBoolean("show_footer",true)){TextView footer=text(val("footer_text","© راسخ‌نیا"),11,0xFF78909C,false);footer.setGravity(Gravity.CENTER);LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2);fp.topMargin=dp(20);body.addView(footer,fp);}
        LinearLayout nav=new LinearLayout(this);nav.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);nav.setBackgroundColor(Color.WHITE);nav.setPadding(dp(4),dp(8),dp(4),dp(8));String[] tabs={"⌂\nخانه","▦\nخدمات","▣\nآموزش","♙\nحساب","☰\nبیشتر"};for(int i=0;i<tabs.length;i++){final int idx=i;TextView t=action(tabs[i],12,i==activeTab?0xFF087F91:0xFF607D8B,i==activeTab?0xFFE2F4F5:Color.WHITE,12);t.setLineSpacing(dp(3),1);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(0,dp(56),1);np.leftMargin=dp(2);np.rightMargin=dp(2);nav.addView(t,np);t.setOnClickListener(v->{activeTab=idx;if(idx==0){render();}else if(idx==1)open(HOME);else if(idx==2)open(HOME);else if(idx==3)open(HOME+"my-account/");else showDrawer();});}page.addView(nav);
        setContentView(shell);
    }
    private void showDrawer(){if(drawer!=null&&drawer.getParent()!=null){shell.removeView(drawer);drawer=null;return;}drawer=vertical();drawer.setBackgroundColor(Color.WHITE);drawer.setPadding(dp(18),dp(30),dp(18),dp(18));FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(dp(285),-1,Gravity.RIGHT);shell.addView(drawer,p);TextView close=action("✕    بستن فهرست",15,0xFF15394B,0xFFE7F1F4,12);drawer.addView(close,new LinearLayout.LayoutParams(-1,dp(50)));close.setOnClickListener(v->{shell.removeView(drawer);drawer=null;});TextView name=text("فهرست راسخ‌نیا",20,0xFF15394B,true);name.setPadding(0,dp(20),0,dp(12));drawer.addView(name);String[][] entries={{"خانه",HOME},{"خدمات حقوقی",HOME},{"قراردادها",HOME},{"مقالات و آموزش",HOME},{"حساب کاربری",HOME+"my-account/"},{"پشتیبانی",HOME+"my-account/"}};for(String[] e:entries){TextView t=text("‹    "+e[0],16,0xFF1A4054,false);t.setPadding(dp(12),dp(13),dp(12),dp(13));drawer.addView(t);t.setOnClickListener(v->open(e[1]));}JSONArray arr=items();for(int i=0;i<Math.min(arr.length(),30);i++){JSONObject o=arr.optJSONObject(i);if(o==null||!o.optBoolean("enabled",true))continue;TextView t=text("•    "+o.optString("title","خدمات"),14,0xFF316277,false);t.setPadding(dp(12),dp(10),dp(12),dp(10));drawer.addView(t);t.setOnClickListener(v->open(o.optString("url",HOME)));}}
    private void refresh(){new Thread(()->{HttpURLConnection conn=null;try{conn=(HttpURLConnection)new URL("https://rasekhnia.ir/wp-json/rki-android/v1/config").openConnection();conn.setConnectTimeout(8000);conn.setReadTimeout(8000);if(conn.getResponseCode()!=200)return;ByteArrayOutputStream b=new ByteArrayOutputStream();try(InputStream in=conn.getInputStream()){byte[] a=new byte[4096];int n;while((n=in.read(a))!=-1){if(b.size()+n>131072)return;b.write(a,0,n);}}String raw=new String(b.toByteArray(),StandardCharsets.UTF_8);JSONObject received=new JSONObject(raw);getSharedPreferences("dashboard",MODE_PRIVATE).edit().putString("config",raw).apply();handler.post(()->{if(!isFinishing()){config=received;render();}});}catch(Exception ignored){}finally{if(conn!=null)conn.disconnect();}}).start();}
    @Override public void onCreate(Bundle b){super.onCreate(b);try{config=new JSONObject(getSharedPreferences("dashboard",MODE_PRIVATE).getString("config","{}"));}catch(Exception ignored){}render();refresh();}
    @Override protected void onResume(){super.onResume();if(shell!=null)refresh();}
}
