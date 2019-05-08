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
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
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

import org.json.JSONObject;

import java.util.Arrays;

/**
 * facebook登录注意点：
 * 1、facebook后台如未提交上线，还是开发中状态，测试需要添加测试账号
 * 2、测试账号测试登录时可能会报key hash值不对，需要将此key添加到facebook后台配置中
 * 3、与firebase关联时，注意不要遗漏验证链接地址配置
 * 4、页面中调用时，主要onActivityResult回调不要遗漏
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnLogin;
    private Button btn_share_url;
    private Button btn_share_pic;
    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private TextView tv_login_result;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private GraphRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();


        btnLogin = findViewById(R.id.btn_login);
        btn_share_url = findViewById(R.id.btn_share_url);
        btn_share_pic = findViewById(R.id.btn_share_pic);
        tv_login_result = findViewById(R.id.tv_login_result);


        btnLogin.setOnClickListener(this);
        btn_share_url.setOnClickListener(this);
        btn_share_pic.setOnClickListener(this);
        // 初始化facebook登录回调
        callbackManager = CallbackManager.Factory.create();
        // 注册facebook登录回调结果
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                Log.e("lxf", "登录成功");
                commitTongJi("facebook登录成功");
                tv_login_result.setText("");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                commitTongJi("facebook登录取消");
                Log.e("lxf", "登录取消");
                Toast.makeText(MainActivity.this, "登录取消", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                commitTongJi("facebook登录失败");
                commitTongJi("facebook登录失败：" + error.toString());
                Log.e("lxf", "facebook登录失败");
                tv_login_result.setText("facebook登录失败：" + error.toString());
                Toast.makeText(MainActivity.this, "登录失败:" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        // 获取登录后的accesstoken，更具其值读取更多信息
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.e("lxf", "oldAccessToken==" + oldAccessToken);
                Log.e("lxf", "currentAccessToken==" + currentAccessToken);
                AccessToken currentAccessToken1 = AccessToken.getCurrentAccessToken();
                Log.e("lxf", "currentAccessToken1==" + currentAccessToken1);
                // 用accesstoken更具需求获取对应的信息（id，姓名，性别，邮箱，头像，地域信息）
                request = GraphRequest.newMeRequest(currentAccessToken1, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        if (object != null) {
                            //比如:1565455221565
                            String id = object.optString("id");
                            //比如：Zhang San
                            String name = object.optString("name");
                            //性别：比如 male （男）  female （女）
                            String gender = object.optString("gender");
                            //邮箱：比如：56236545@qq.com
                            String emali = object.optString("email");

                            //获取用户头像
                            JSONObject object_pic = object.optJSONObject("picture");
                            JSONObject object_data = object_pic.optJSONObject("data");
                            String photo = object_data.optString("url");

                            //获取地域信息
                            //zh_CN 代表中文简体
                            String locale = object.optString("locale");
                            Log.e("lxf", "22 gender==" + gender);
                            Log.e("lxf", "22 id==" + id);
                            Log.e("lxf", "22 emali==" + emali);
                            Log.e("lxf", "22 photo==" + photo.toString());
                            Log.e("lxf", "22 locale==" + locale);
                            Log.e("lxf", "22 name==" + name);
                        } else {
                            Log.e("lxf", "22 null null null ");
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,gender,birthday,email,picture,locale,updated_time,timezone,age_range,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();
            }
        };
        // 获取用户信息，名和id
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                String firstName = currentProfile.getFirstName();
                String id = currentProfile.getId();
                String lastName = currentProfile.getLastName();
                Uri linkUri = currentProfile.getLinkUri();
                String middleName = currentProfile.getMiddleName();
                String name = currentProfile.getName();
                Log.e("lxf", "firstName==" + firstName);
                Log.e("lxf", "id==" + id);
                Log.e("lxf", "lastName==" + lastName);
                Log.e("lxf", "linkUri==" + linkUri.toString());
                Log.e("lxf", "middleName==" + middleName);
                Log.e("lxf", "name==" + name);
            }
        };

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
            case R.id.btn_share_url:
                commitTongJi("点击分享链接");
                shareUrl();
                break;
            case R.id.btn_share_pic:
                commitTongJi("点击分享图片");
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
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }
}
