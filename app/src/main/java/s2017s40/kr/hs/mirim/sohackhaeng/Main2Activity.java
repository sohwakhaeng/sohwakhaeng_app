package s2017s40.kr.hs.mirim.sohackhaeng;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import javax.xml.transform.Result;

public class Main2Activity extends AppCompatActivity {

    Button okBtn;
    TextView txtD, txtS;
    public static int ResultSum = 0;
    public static String market[] = new String[3];
    String Number;
    int ResultAvg;
    int smsNum = 0;
    FirebaseDatabase database  = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getInstance().getReference();

    ArrayList<MarketDTO> marketList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        okBtn = findViewById(R.id.result_ok_btn);
        txtD = findViewById(R.id.result_audio_text);
        txtS = findViewById(R.id.result_sms_text);
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        Number = auto.getString("Number",null);
        Intent intent = getIntent();
        ResultSum = intent.getExtras().getInt("ResultSum"); /*String형*/

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.fromParts("package", "s2017s40.kr.hs.mirim.sohackhaeng", null);
                Intent delIntent = new Intent(Intent.ACTION_DELETE, uri);
                startActivity(delIntent);
            }
        });
        ResultAvg = ResultSum / 5;
        if(ResultAvg > 80){
           smsNum = 1;
        }else if(ResultAvg > 60){
            smsNum = 2;
        }else if(ResultAvg > 40){
            smsNum = 3;
        }else if(ResultAvg > 20){
           smsNum = 4;
        }else if(ResultAvg > 0){
            smsNum = 5;
        }else{
            smsNum = 0;
        }
        SMS(smsNum, ResultAvg);
       // ResultSMS();

    }
    public void SMS(int smsNum, int resultAvg){
        marketList.add(new MarketDTO("서울시 종로구 율곡로 226","26D5S6V","밀라네"));
        marketList.add(new MarketDTO("서울 종로구 이화장1나길 17 1층","69S5W5G","먹방스튜디오"));
        marketList.add(new MarketDTO("서울 종로구 이화장1나길13 1층","5W6XC6W","슬로스텝"));

        String smsText = "";
        switch (smsNum){
            case 0: case 1:
                smsText = "쿠폰을 받을 수 없습니다.";break;
            case 2:
                smsText = marketList.get(0).getName(); break;
            case 3:
                smsText = marketList.get(0).getName() +", " + marketList.get(1).getName();break;
            case 4:
                smsText = marketList.get(0).getName() +", " + marketList.get(1).getName();break;
            case 5:
                smsText = marketList.get(0).getName() +", " + marketList.get(1).getName()+", " +  marketList.get(2).getName();break;
        }
        txtD.setText(Number + "님의 10분 간 측정 된 평균 데시벨은" + resultAvg + "DB 입니다.");
        txtS.setText(smsText + "가게의 코드를 문자로 전송했습니다.");


        try {
            if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(Main2Activity.this, android.Manifest.permission.SEND_SMS )
                    != PackageManager.PERMISSION_GRANTED) {
                checkVerify();
            }
            SmsManager smsManager = SmsManager.getDefault();
            String newNumber = "0" + Number.substring(3);
            Log.e("newNumber","newNumber" + newNumber);
            smsManager.sendTextMessage(newNumber, null, smsText, null, null);

            Toast.makeText(getApplicationContext(), "전송 완료!" + smsText + "의 쿠폰을 문자 받을 수 있습니다.", Toast.LENGTH_LONG).show();
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

