package com.bas.liverpoolicons;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
public class MainActivity extends Activity {
  @Override public void onCreate(Bundle b) {
    super.onCreate(b);
    LinearLayout box=new LinearLayout(this);
    box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
    box.setPadding(48,48,48,48); box.setBackgroundColor(Color.rgb(200,16,46));
    TextView t=new TextView(this); t.setText("BAS Liverpool Icons\n10-app test pack");
    t.setTextColor(Color.WHITE); t.setTextSize(28); t.setGravity(Gravity.CENTER); box.addView(t);
    TextView i=new TextView(this); i.setText("Apply from Lawnchair → Home settings → Icons / Icon Pack");
    i.setTextColor(Color.WHITE); i.setTextSize(16); i.setGravity(Gravity.CENTER); i.setPadding(0,32,0,0); box.addView(i);
    setContentView(box);
  }
}
