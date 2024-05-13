package com.example.notebook0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
// change import android.widget.Toolbar; to import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.base.MoreObjects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class editnoteactivity extends AppCompatActivity {


    private static final int IMAGE_CODE = 1;
    Intent data;
    EditText editText_title, editText_content;
    FloatingActionButton saveEditNote;

    String noteId;

    TestAnalyzer testAnalyzer;


    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;


    MediaRecorder mediaRecorder;

    MediaPlayer mediaPlayer;

    private static final String TAG = "EditNoteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editnoteactivity);
        editText_title = findViewById(R.id.edit_title_note);
        editText_content = findViewById(R.id.edit_note_content);
        saveEditNote = findViewById(R.id.saveEditNote);

        data = getIntent();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();


        Toolbar toolbar = findViewById(R.id.toolbar_edit_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        KeyBar keyBar = findViewById(R.id.keybar_edit);

        mediaRecorder = new MediaRecorder();

        resWrite();

// 监听软键盘的可见性
        View rootView = findViewById(R.id.root_edit);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                Log.d(TAG, "Screen Height: "+ Integer.toString(screenHeight));
                int keypadHeight = screenHeight - r.bottom;
                Log.d(TAG, "Keypad Height: "+ Integer.toString(keypadHeight));
                // 计算布局可见高度与屏幕高度的差值
                int heightDifference = screenHeight - (r.bottom - r.top);

                Log.d(TAG, "Height Difference: "+ Integer.toString(heightDifference));

                int originalY = 10;

                // 如果差值大于某个阈值，表示软键盘可见
                if (keypadHeight > screenHeight * 0.1) { // 输入法可见
                    // 调整KeyBar的位置（例如，向上移动）
                    keyBar.setY(keyBar.getY() - keypadHeight);
                } else { // 输入法隐藏
                    // 恢复KeyBar的位置（例如，向下移动）
                    keyBar.setY(originalY); // 这里的originalY是KeyBar的初始Y坐标

                }
            }
        });

        keyBar.insertImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理插入图片按钮点击事件
                Toast.makeText(getApplicationContext(), "Insert Image Button Clicked", Toast.LENGTH_SHORT).show();
                callGallery();
            }
        });

        keyBar.insertAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理插入音频按钮点击事件
                Toast.makeText(getApplicationContext(), "Insert Audio Button Clicked", Toast.LENGTH_SHORT).show();
                if(isMicrophonePresent())
                {
                    getMicrophonePermission();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Microphone is not present", Toast.LENGTH_SHORT).show();
                }
            }
        });

        keyBar.insertAudioButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    //开始录音
                    Toast.makeText(getApplicationContext(), "Start Recording", Toast.LENGTH_SHORT).show();

                    try {
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mediaRecorder.setOutputFile(getAudioRecordPath());
                        Log.d("Audio path", getAudioRecordPath());
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }


                }
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    //停止录音
                    Toast.makeText(getApplicationContext(), "Stop Recording", Toast.LENGTH_SHORT).show();
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    insertImg(getAudioPlayerPngPath());

                    SpannableString ss = new SpannableString(editText_content.getText());
                    Log.d("span", ss.toString());
                    ClickableSpan audioSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            Toast.makeText(getApplicationContext(),"audio Span is clicked", Toast.LENGTH_SHORT).show();
                        }
                    };


                }
                return false;
            }
        });

        keyBar.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mediaPlayer = new MediaPlayer();
                    Log.d("Audio path", getAudioRecordPath());

                    mediaPlayer.setDataSource(getAudioRecordPath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });





        saveEditNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "save button clicked", Toast.LENGTH_SHORT).show();
                //trim()???????
                String newTitle = editText_title.getText().toString();
                String newContent = editText_content.getText().toString();

                if(newTitle.isEmpty()||newContent.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Something is Empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    DocumentReference documentReference = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("mynotes").document(data.getStringExtra("noteId"));
                    Map<String,Object> note = new HashMap<>();
                    note.put("title", newTitle);
                    note.put("content", newContent);
                    documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(), "Note is updated", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(editnoteactivity.this,notesActivity.class));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed to update", Toast.LENGTH_SHORT).show();

                        }
                    });

                    //添加图片
                    //添加文件夹
                    String folderPath = noteId;

                    StorageReference storageReference = firebaseStorage.getReference();
                    StorageReference imgFolderRef = storageReference.child(noteId).child("images");
                    ArrayList<String> imgPaths = (ArrayList<String>) testAnalyzer.getImageTextList();
                    Log.d("imgpaths", Integer.toString(imgPaths.size()));
                    for(int i = 0; i < imgPaths.size(); i++)
                    {
                        Uri imgUri = Uri.fromFile(new File(imgPaths.get(i)));
                        StorageReference imgRef = imgFolderRef.child("image"+i);
                        imgRef.putFile(imgUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    // 图片上传成功
                                    Log.d(TAG, "Image uploaded successfully");

                                    // 获取上传后的图片的下载 URL
                                    imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        String imageUrl = uri.toString();
                                        // imageUrl 是上传后的图片在 Storage 中的下载 URL
                                        Log.d(TAG, "Image download URL: " + imageUrl);
                                    });
                                })
                                .addOnFailureListener(exception -> {
                                    // 图片上传失败
                                    Log.e(TAG, "Image upload failed", exception);
                                });
                    }
                }
            }
        });

        String title_note = data.getStringExtra("title");
        String content_note = data.getStringExtra("content");
        noteId = data.getStringExtra("noteId");
        editText_title.setText(title_note);
        editText_content.setText(content_note);

        String originalText = data.getStringExtra("content");
        testAnalyzer = new TestAnalyzer();
        testAnalyzer.setWidth(ScreenUtils.getScreenWidth(this));
        testAnalyzer.initContent(originalText);
        if (testAnalyzer.haveImage)
        {

            testAnalyzer.setContext(this);
            Log.d("YYPT", testAnalyzer.getSpannable().toString());
            Log.d("image_path", testAnalyzer.getImageTextList().get(0));
            editText_content.setText(testAnalyzer.getSpannable());
        }


        XXPermissions.with(this)
                // 申请单个权限
                //.permission(Permission.RECORD_AUDIO)
                // 申请多个权限
                //.permission(Permission.Group.CALENDAR)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                //.unchecked()
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            Toast.makeText(getApplicationContext(), "获取部分权限成功，但部分权限未正常授予", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(getApplicationContext(), "获取录音和日历权限成功", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            Toast.makeText(getApplicationContext(), "被永久拒绝授权，请手动授予录音和日历权限", Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(editnoteactivity.this, permissions);
                        } else {
                            Toast.makeText(getApplicationContext(), "获取录音和日历权限失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void callGallery(){
        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        getAlbum.setType("image/*");
        startActivityForResult(getAlbum,IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm = null;
        // 外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
        ContentResolver resolver = getContentResolver();
        if (requestCode == IMAGE_CODE) {
            try {
                // 获得图片的uri
                Uri originalUri = data.getData();
                Log.d("URI", "originalUri: "+originalUri);
                Log.d("URIpath", "originalUri.getPath(): "+originalUri.getPath());

                String path_Test = getRealPathFromUri(editnoteactivity.this, originalUri);

                Log.d("URIpath", "path_Test: "+path_Test);


                String imagePath= null;
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor_ = getContentResolver().query(originalUri, filePathColumn, null, null, null);
                if(cursor_ != null){
                    cursor_.moveToFirst();
                    int columnIndex = cursor_.getColumnIndex(filePathColumn[0]);
                    imagePath = cursor_.getString(columnIndex);
                    cursor_.close();
                }
                Log.d("ImageURIpath", "imagePath: "+imagePath);

                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                String[] proj = {MediaStore.Images.Media.DATA};
                // 好像是android多媒体数据库的封装接口，具体的看Android文档
                Cursor cursor = managedQuery(originalUri,proj,null,null,null);
                // 按我个人理解 这个是获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();
                // 最后根据索引值获取图片路径
                String path = cursor.getString(column_index);
                insertImg(path);


                //String path = getRealPathFromUri(originalUri);
                //insertImg(path);
                //Toast.makeText(AddFlagActivity.this,path,Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(editnoteactivity.this, "图片插入失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getRealPathFromUri(Uri uri) {
        String realPath = "";
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            if (cursor.moveToFirst()) {
                realPath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return realPath;
    }

    private void insertImg(String path) {
        String tagPath = "<img src=\""+path+"\"/>";//为图片路径加上<img>标签
        Log.d("path", "path: "+path);//path = null?
        Log.d("path", "tagPath: "+tagPath);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap != null){
            SpannableString ss = getBitmapMime(path,tagPath);
            Log.d("YYPT", ss.toString());
            insertPhotoToEditText(ss);
            editText_content.append("\n");
            Log.d("YYPT", editText_content.getText().toString());
        }

    }

    //region 将图片插入到EditText中
    private void insertPhotoToEditText(SpannableString ss){
        Editable et = editText_content.getText();
        int start = editText_content.getSelectionStart();
        et.insert(start,ss);
        et.insert(start+ss.length(),"\n");

        editText_content.setText(et);
        editText_content.setSelection(start+ss.length() + 1);
        editText_content.setFocusableInTouchMode(true);
        editText_content.setFocusable(true);
    }
    //endregion

    private SpannableString getBitmapMime(String path,String tagPath) {
        SpannableString ss = new SpannableString(tagPath);//这里使用加了<img>标签的图片路径
        Log.d("YYPT", ss.toString());

        int width = ScreenUtils.getScreenWidth(editnoteactivity.this);
        int height = ScreenUtils.getScreenHeight(editnoteactivity.this);

        Bitmap bitmap = ImageUtils.getSmallBitmap(path,width,480);
        ImageSpan imageSpan = new ImageSpan(this, bitmap);
        ss.setSpan(imageSpan, 0, tagPath.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static String getRealPathFromUri(Context context, Uri uri) {
        String filePath = "";
        String scheme = uri.getScheme();
        if (scheme == null)
            filePath = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            filePath = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
            if (TextUtils.isEmpty(filePath)) {
                filePath = getFilePathForNonMediaUri(context, uri);
            }
        }
        return filePath;
    }

    //非媒体文件中查找
    private static String getFilePathForNonMediaUri(Context context, Uri uri) {
        String filePath = "";
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow("_data");
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return filePath;
    }


    //Audio Part

    public boolean isMicrophonePresent()
    {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    private void getMicrophonePermission()
    {
        XXPermissions.with(this)
                .permission(Permission.RECORD_AUDIO)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean all) {
                        if (all) {
                            Toast.makeText(getApplicationContext(), "获取录音权限成功", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean never) {
                        if (never) {
                            Toast.makeText(getApplicationContext(), "被永久拒绝授权，请手动授予录音权限", Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(editnoteactivity.this, permissions);
                        } else {
                            Toast.makeText(getApplicationContext(), "获取录音权限失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String getAudioRecordPath()
    {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File audioDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File audioFile = new File(audioDir, "audio_record.mp3");
        return audioFile.getPath();
    }

    private String getAudioPlayerPngPath()
    {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File downloadDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File filePath = new File(downloadDir, "audio_player.png");
        return filePath.toString();
    }

    private void resWrite()
    {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.audio_player);
        // 获取 Bitmap 对象
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.audio_player);

        // 将 Bitmap 保存为文件
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File downloadDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File filePath = new File(downloadDir, "audio_player.png");
        Log.d("filepath", filePath.toString());

        try {
            FileOutputStream out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // 将 Bitmap 压缩成 PNG 格式保存
            out.flush();
            out.close();
            Log.d("write", filePath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}