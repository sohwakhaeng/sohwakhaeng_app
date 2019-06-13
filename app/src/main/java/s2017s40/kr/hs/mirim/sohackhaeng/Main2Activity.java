package s2017s40.kr.hs.mirim.sohackhaeng;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.xml.transform.Result;

public class Main2Activity extends AppCompatActivity {

    Button okBtn;
    TextView txtD, txtS;
    public static int ResultSum = 0;
    public static String market[] = new String[3];
    int count = 0;
    String Number;

    FirebaseDatabase database  = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getInstance().getReference().child("User");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        okBtn = findViewById(R.id.result_ok_btn);
        txtD = findViewById(R.id.result_audio_text);
        txtS = findViewById(R.id.result_sms_text);
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        Number = auto.getString("Number",null);

       // ResultSMS();

    }
    public void ResultSMS() {
        int ResultAvg = 0;
        myRef.child(Number).child("Noise").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot fileSnapshot : dataSnapshot.getChildren()) {
                    if(fileSnapshot != null) {
                        ResultSum += fileSnapshot.getValue(Integer.class);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        Log.e("sum",String.valueOf(ResultSum));


        ResultAvg = ResultSum / 6;
        int smsNum = 0;
        if(ResultAvg > 80){
            Toast.makeText(Main2Activity.this, "1", Toast.LENGTH_SHORT).show();
            smsNum = 1;
        }else if(ResultAvg > 60){
            Toast.makeText(Main2Activity.this, "2", Toast.LENGTH_SHORT).show();smsNum = 2;
        }else if(ResultAvg > 40){
            Toast.makeText(Main2Activity.this, "3", Toast.LENGTH_SHORT).show();smsNum = 3;
        }else if(ResultAvg > 20){
            Toast.makeText(Main2Activity.this, "4", Toast.LENGTH_SHORT).show();smsNum = 4;
        }else if(ResultAvg > 0){
            Toast.makeText(Main2Activity.this, "5", Toast.LENGTH_SHORT).show();smsNum = 5;
        }else{
            Toast.makeText(Main2Activity.this, "6", Toast.LENGTH_SHORT).show();smsNum = 0;
        }
        SMS(1);
    }
    public void SMS(int smsNum){

        myRef.child(Number).child("Market").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot fileSnapshot : dataSnapshot.getChildren()) {
                    if (fileSnapshot != null) {
                        //market[i++] = fileSnapshot.getValue(String.class);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        String smsText = "쿠폰을 받을 수 없습니다.";
        switch (smsNum){
            case 0: break;
            case 1: break;
        }

        try {
            if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(Main2Activity.this, android.Manifest.permission.SEND_SMS )
                    != PackageManager.PERMISSION_GRANTED)
            {
                checkVerify();
            }
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(Number, null, smsText, null, null);

            Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            Log.e("error", String.valueOf(e));
            e.printStackTrace();
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    public void checkVerify(){
        if (ActivityCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Main2Activity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.SEND_SMS},1);
        } else {
        }
    }
}

