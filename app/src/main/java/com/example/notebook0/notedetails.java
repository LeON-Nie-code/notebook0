package com.example.notebook0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class notedetails extends AppCompatActivity {

    private TextView title_detail_note;
    private TextView content_detail_note;

    FloatingActionButton gotoEditNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notedetails);

        title_detail_note = findViewById(R.id.title_detail_note);
        content_detail_note = findViewById(R.id.content_detail_note);
        gotoEditNote = findViewById(R.id.gotoEditNote);



        Toolbar toolbar = findViewById(R.id.toolbar_detail_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent data = getIntent();

        gotoEditNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),editnoteactivity.class);
                intent.putExtra("title", data.getStringExtra("title"));
                intent.putExtra("content", data.getStringExtra("content"));
                intent.putExtra("noteId", data.getStringExtra("noteId"));
                v.getContext().startActivity(intent);
                //important
            }
        });

        content_detail_note.setText(data.getStringExtra("content"));
        title_detail_note.setText(data.getStringExtra("title"));

        String originalText = data.getStringExtra("content");
        TestAnalyzer testAnalyzer = new TestAnalyzer();
        testAnalyzer.setWidth(ScreenUtils.getScreenWidth(this));
        testAnalyzer.initContent(originalText);
        if (testAnalyzer.haveImage)
        {
            testAnalyzer.setContext(this);
            Log.d("YYPT", testAnalyzer.getSpannable().toString());
            Log.d("image_path", testAnalyzer.getImageTextList().get(0));
            content_detail_note.setText(testAnalyzer.getSpannable());
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}