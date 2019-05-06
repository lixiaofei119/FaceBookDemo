package com.lxf.facebookdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnLogin;
    private Button btnShare;
    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private TextView tv_login_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();


        btnLogin = findViewById(R.id.btn_login);
        btnShare = findViewById(R.id.btn_share);
        tv_login_result = findViewById(R.id.tv_login_result);


        btnLogin.setOnClickListener(this);
        btnShare.setOnClickListener(this);

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                commitTongJi("facebook登录成功");
                tv_login_result.setText("");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                commitTongJi("facebook登录取消");
                Toast.makeText(MainActivity.this, "登录取消", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                commitTongJi("facebook登录失败");
                commitTongJi("facebook登录失败：" + error.toString());
                tv_login_result.setText("facebook登录失败：" + error.toString());
                Toast.makeText(MainActivity.this, "登录失败:" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                commitTongJi("点击facebook登录");
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken == null || accessToken.isExpired()) {
                    LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
                }
                break;
            case R.id.btn_share:
                commitTongJi("点击分享");
                sharePic();
                break;
            default:
                break;
        }
    }

    private void commitTongJi(String content) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT, content);
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void shareUrl() {
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://www.baidu.com/"))
                .build();
        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
    }

    private void sharePic() {
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.mipmap.beautiful, null);
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
    }

    private void shareVideo() {
        Uri videoUrl = null;
        ShareVideo video = new ShareVideo.Builder()
                .setLocalUrl(videoUrl)
                .build();
        ShareVideoContent content = new ShareVideoContent.Builder()
                .setVideo(video)
                .build();
        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.show(content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("lxf", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("lxf", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("lxf", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }
}
