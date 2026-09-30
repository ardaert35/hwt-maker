package com.arda.hwtmaker;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.graphics.drawable.*;import android.net.Uri;import android.provider.OpenableColumns;import android.view.*;import android.widget.*;import java.io.*;import java.util.*;import java.util.zip.*;

public class MainActivity extends Activity {
    static final int PICK_HWT=10, PICK_IMG=11, CREATE_HWT=12;
    LinearLayout root, previewBox; TextView status; Bitmap selected; Uri templateUri; String templateName="";
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    TextView label(String s){ TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(14);t.setPadding(dp(4),dp(8),dp(4),dp(8));return t; }
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b;}
    @Override public void onCreate(Bundle b){super.onCreate(b); build();}
    void build(){
      root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(16),dp(16),dp(16));root.setBackgroundColor(Color.rgb(12,12,14));
      TextView title=label("HWT Maker GT6");title.setTextSize(24);title.setTypeface(null,Typeface.BOLD);root.addView(title,new LinearLayout.LayoutParams(-1,dp(50)));
      TextView info=label("Huawei Watch GT 6 Pro • 466 × 466 px");info.setTextColor(Color.LTGRAY);root.addView(info);
      previewBox=new LinearLayout(this);previewBox.setGravity(Gravity.CENTER);previewBox.setPadding(0,dp(10),0,dp(10));
      ImageView pv=new ImageView(this);pv.setTag("preview");pv.setBackgroundColor(Color.BLACK);pv.setScaleType(ImageView.ScaleType.CENTER_CROP);previewBox.addView(pv,new LinearLayout.LayoutParams(dp(280),dp(280)));root.addView(previewBox,new LinearLayout.LayoutParams(-1,dp(310)));
      Button choose=btn("1 • HWT şablonu seç");choose.setOnClickListener(v->pickHwt());root.addView(choose);
      Button image=btn("2 • Kadran görselini seç");image.setOnClickListener(v->pickImage());root.addView(image);
      Button export=btn("3 • HWT oluştur");export.setOnClickListener(v->export());root.addView(export);
      status=label("Önce çalışan bir GT6 HWT şablonu seç.");status.setTextColor(Color.LTGRAY);root.addView(status);
      TextView note=label("Not: Uygulama mevcut HWT şablonunun paket yapısını korur; seçtiğin 466×466 görseli şablondaki görsel kaynaklarından birine yerleştirir. Böylece sıfırdan tahmini bir HWT formatı üretmek yerine gerçek çalışan şablon kullanılır.");note.setTextColor(Color.GRAY);root.addView(note);
      setContentView(root);
    }
    void pickHwt(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/octet-stream");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_HWT);}
    void pickImage(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_IMG);}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(c!=RESULT_OK||d==null)return;try{
      if(r==CREATE_HWT){actuallyExport(d.getData());return;}
      if(r==PICK_HWT){templateUri=d.getData();templateName=getName(templateUri);status.setText("Şablon: "+templateName);getContentResolver().takePersistableUriPermission(templateUri,d.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION);}
      else if(r==PICK_IMG){InputStream in=getContentResolver().openInputStream(d.getData());selected=BitmapFactory.decodeStream(in);in.close();selected=scaleCrop(selected,466,466);((ImageView)previewBox.findViewWithTag("preview")).setImageBitmap(selected);status.setText("Görsel hazır • 466×466");}
    }catch(Exception e){status.setText("Hata: "+e.getMessage());}}
    String getName(Uri u){String n="template.hwt";Cursor c=getContentResolver().query(u,null,null,null,null);if(c!=null){int x=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(c.moveToFirst()&&x>=0)n=c.getString(x);c.close();}return n;}
    Bitmap scaleCrop(Bitmap b,int w,int h){if(b==null)return null;float s=Math.max(w/(float)b.getWidth(),h/(float)b.getHeight());int nw=Math.round(b.getWidth()*s),nh=Math.round(b.getHeight()*s);Bitmap z=Bitmap.createScaledBitmap(b,nw,nh,true);int x=(nw-w)/2,y=(nh-h)/2;return Bitmap.createBitmap(z,x,y,w,h);}
    void export(){if(templateUri==null||selected==null){Toast.makeText(this,"HWT şablonu ve görsel seçmelisin.",Toast.LENGTH_LONG).show();return;}Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/octet-stream");i.putExtra(Intent.EXTRA_TITLE,"GT6_Custom.hwt");startActivityForResult(i,CREATE_HWT);}
    void actuallyExport(Uri out){
      try(InputStream raw=getContentResolver().openInputStream(templateUri);ZipInputStream zin=new ZipInputStream(new BufferedInputStream(raw));OutputStream os=getContentResolver().openOutputStream(out);ZipOutputStream zout=new ZipOutputStream(new BufferedOutputStream(os))){
        ByteArrayOutputStream png=new ByteArrayOutputStream();selected.compress(Bitmap.CompressFormat.PNG,100,png);byte[] img=png.toByteArray();ZipEntry e;boolean replaced=false;
        while((e=zin.getNextEntry())!=null){String n=e.getName();if(e.isDirectory()){zout.putNextEntry(new ZipEntry(n));zout.closeEntry();continue;}byte[] data=readAll(zin);String low=n.toLowerCase(Locale.US);if(!replaced && low.endsWith(".png") && (low.contains("a100")||low.contains("preview")||low.contains("cover"))){data=img;replaced=true;}ZipEntry ne=new ZipEntry(n);zout.putNextEntry(ne);zout.write(data);zout.closeEntry();}
        if(!replaced){Toast.makeText(this,"Şablonda otomatik değiştirilecek PNG bulunamadı. Paket korunarak dışa aktarıldı; doğru kaynak seçimi için gerçek GT6 şablonu gerekir.",Toast.LENGTH_LONG).show();}else Toast.makeText(this,"HWT oluşturuldu.",Toast.LENGTH_LONG).show();status.setText(replaced?"HWT hazır: "+getName(out):"HWT dışa aktarıldı");
      }catch(Exception ex){Toast.makeText(this,"HWT oluşturulamadı: "+ex.getMessage(),Toast.LENGTH_LONG).show();}
    }
    byte[] readAll(InputStream in)throws IOException{ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] x=new byte[8192];int n;while((n=in.read(x))!=-1)b.write(x,0,n);return b.toByteArray();}
    @Override protected void onResume(){super.onResume();}
    @Override protected void onActivityResultExtra(int a,int b,Intent c){}
}
