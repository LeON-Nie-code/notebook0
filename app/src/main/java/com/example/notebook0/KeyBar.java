package com.example.notebook0;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class KeyBar extends LinearLayout {

    ImageView editButton;
    ImageView insertImageButton;
    ImageView insertAudioButton;
    ImageView backButton;
    public KeyBar(Context context) {
        super(context);
        init();
    }

    public KeyBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeyBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.keybar_layout, this, true);

        // 获取图标按钮的引用
        editButton = findViewById(R.id.edit_button);
        insertImageButton = findViewById(R.id.insert_image_button);
        insertAudioButton = findViewById(R.id.insert_audio_button);
        backButton = findViewById(R.id.back_button);

        // 设置按钮点击事件的监听器
        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        insertImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理插入图片按钮点击事件
            }
        });

        insertAudioButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理插入音频按钮点击事件
            }
        });

        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理返回按钮点击事件
            }
        });
    }
}