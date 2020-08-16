package com.example.study;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.study.entity.User;

public class firstActivity extends AppCompatActivity  {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiyt_first);
        Bundle bundle= getIntent().getExtras();
        User user = (User)bundle.getSerializable("user");
        Log.i("first","获取的名字为："+user.getName()+"获取的id为："+user.getId());
        TextView tvshow = findViewById(R.id.tv_show);
        tvshow.setText("获取的名字为："+user.getName()+"获取的id为："+user.getId());


    }
}
