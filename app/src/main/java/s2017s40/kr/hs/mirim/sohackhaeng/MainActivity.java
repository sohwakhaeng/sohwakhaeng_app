package s2017s40.kr.hs.mirim.sohackhaeng;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity{
    WebView mWebView;
    TextView errorVeiw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 123);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("notice");

        errorVeiw = (TextView) findViewById(R.id.net_error_view);
        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

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
        });



        mWebView.loadUrl("http://vvvv980.dothome.co.kr/sohackhaeng_last/");

        mWebView.setWebViewClient(new WebViewClient(){
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
            //alert 처리
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }//onJsAlert

            //confirm 처리
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
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
        });//setWebChromeClient

    }//onCreate

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

}


