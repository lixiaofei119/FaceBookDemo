package com.lxf.facebookdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnLogin;
    private Button btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btn_login);
        btnShare = findViewById(R.id.btn_share);

        btnLogin.setOnClickListener(this);
        btnShare.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:

                break;
            case R.id.btn_share:

                break;
            default:
                break;
        }
    }
}
