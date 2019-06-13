package s2017s40.kr.hs.mirim.sohackhaeng;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    WebView mWebView;
    TextView errorVeiw;
    private AudioReader audioReader;
    private int sampleRate = 5000;
    private int inputBlockSize = 256;
    private int sampleDecimate = 1;
    public static int ResultSum = 0;
    public static String market[] = new String[3];
    int count = 0;
    String Number;
    FirebaseDatabase database  = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getInstance().getReference().child("User");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

        Number = auto.getString("Number",null);
        Toast.makeText(MainActivity.this, Number + "님 어서오세요", Toast.LENGTH_LONG).show();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 123);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("notice");

        errorVeiw = (TextView) findViewById(R.id.net_error_view);
        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        mWebView.addJavascriptInterface(new JavaScriptInterface(this),"Android");
        mWebView.loadUrl("file:///android_asset/index.html");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            //네트워크연결에러
            @Override
            public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
                switch(errorCode) {
                    case ERROR_AUTHENTICATION: Log.e("ERROR_AUTHENTICATION","1");break;                  // 서버에서 사용자 인증 실패
                    case ERROR_BAD_URL:Log.e("ERROR_BAD_URL","2"); break;                                   // 잘못된 URL
                    case ERROR_CONNECT: Log.e("ERROR_CONNECT","3");break;                                   // 서버로 연결 실패
                    case ERROR_FAILED_SSL_HANDSHAKE: Log.e("ERROR_FAILED_SSL_","4");break;              // SSL handshake 수행 실패
                    case ERROR_FILE: Log.e("ERROR_FILE","5");break;                                         // 일반 파일 오류
                    case ERROR_FILE_NOT_FOUND: Log.e("ERROR_FILE_NOT_FOUND","6");break;                   // 파일을 찾을 수 없습니다
                    case ERROR_HOST_LOOKUP: Log.e("ERROR_HOST_LOOKUP","7");break;                          // 서버 또는 프록시 호스트 이름 조회 실패
                    case ERROR_IO: Log.e("ERROR_IO","8");break;                                              // 서버에서 읽거나 서버로 쓰기 실패
                    case ERROR_PROXY_AUTHENTICATION: Log.e("ERROR_PROXY_","9");break;                    // 프록시에서 사용자 인증 실패
                    case ERROR_REDIRECT_LOOP: Log.e("ERROR_REDIRECT_LOOP","10");break;                   // 너무 많은 리디렉션
                    case ERROR_TIMEOUT:Log.e("ERROR_TIMEOUT","11"); break;                                 // 연결 시간 초과
                    case ERROR_TOO_MANY_REQUESTS: Log.e("ERROR_TOO_MANY","12");break;                    // 페이지 로드중 너무 많은 요청 발생
                    case ERROR_UNKNOWN:Log.e("ERROR_UNKNOWN","13"); break;                                 // 일반 오류
                    case ERROR_UNSUPPORTED_AUTH_SCHEME: Log.e("ERROR_UNSUPPORTED","14");break;          // 지원되지 않는 인증 체계
                    case ERROR_UNSUPPORTED_SCHEME:          // URI가 지원되지 않는 방식
                        view.loadUrl("about:blank"); // 빈페이지 출력
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //확인 버튼 이벤트
                            }});
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
                mWebView.setVisibility(View.GONE);
                errorVeiw.setVisibility(View.VISIBLE);
            }
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error){
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("이 사이트의 보안 인증서는 신뢰할 수 없습니다.");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //확인 버튼 이벤트
                    }
                });
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("권한 설정")
                        .setMessage(message)
                        .setPositiveButton("Yes",
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("No",
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }//onJsconfirm
            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                WebView newWebView = new WebView(view.getContext());

                WebView.WebViewTransport transport = (WebView.WebViewTransport)resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();

                return true;
            }//onCreateWindow
        });//setWebChromeClient


        audioReader = new AudioReader(Number);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

    }//onCreat
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void doStart()
    {
        audioReader.startReader(sampleRate, inputBlockSize * sampleDecimate, new AudioReader.Listener()
        {
            @Override
            public final void onReadComplete(int dB) {
                receiveDecibel(dB);

            }
            @Override
            public void onReadError(int error) {

            }
        });
    }
    private void receiveDecibel(final int dB) {
       if(count > 1) {
           myRef.child(Number).child("Noise").child(String.valueOf(count)).setValue(Math.abs(dB));
       }
        Log.e("###", Math.abs(dB)+" dB" + count++);
        if(count == 7){
            count = 0;
            doStop();
            return ;
        }
    }
    public void doStop() {
        audioReader.stopReader();
        return;
    }
    class JavaScriptInterface {
        Context mContext;
        JavaScriptInterface(Context c) {
            mContext = c;
        }
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
        @JavascriptInterface
        public void permission(boolean per){
            Log.e("permission","들어옴");
            if(per){
                doStart();
            }
        }
    }
   public void ResultSMS() {
/*        int ResultAvg = 0;
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
            Toast.makeText(MainActivity.this, "1", Toast.LENGTH_SHORT).show();
            smsNum = 1;
        }else if(ResultAvg > 60){
            Toast.makeText(MainActivity.this, "2", Toast.LENGTH_SHORT).show();smsNum = 2;
        }else if(ResultAvg > 40){
            Toast.makeText(MainActivity.this, "3", Toast.LENGTH_SHORT).show();smsNum = 3;
        }else if(ResultAvg > 20){
            Toast.makeText(MainActivity.this, "4", Toast.LENGTH_SHORT).show();smsNum = 4;
        }else if(ResultAvg > 0){
            Toast.makeText(MainActivity.this, "5", Toast.LENGTH_SHORT).show();smsNum = 5;
        }else{
            Toast.makeText(MainActivity.this, "6", Toast.LENGTH_SHORT).show();smsNum = 0;
        }*/
        SMS(1);
   }
    public void SMS(int smsNum){

       /* myRef.child(Number).child("Market").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot fileSnapshot : dataSnapshot.getChildren()) {
                    if (fileSnapshot != null) {
                        market[i++] = fileSnapshot.getValue(String.class);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });*/
        String smsText = "쿠폰을 받을 수 없습니다.";
        switch (smsNum){
            case 0: break;
            case 1: break;
        }

        try {
            if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS )
                    != PackageManager.PERMISSION_GRANTED)
            {
                checkVerify();
            }
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("01063320658", null, smsText, null, null);

            Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            Log.e("error", String.valueOf(e));
            e.printStackTrace();
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    public void checkVerify()
    {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.SEND_SMS},1);
        } else {
        }
    }
}


