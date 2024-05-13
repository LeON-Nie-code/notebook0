package com.example.notebook0;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestAnalyzer {
    private String original_text;
    private String pure_text;
    private ArrayList<String> image_text_list;

    private ArrayList<String> audio_text_list;

    private Context context;

    SpannableString spannable;

    boolean haveImage = false;

    FirebaseStorage firebaseStorage;

    int width = 1000;
    int height = 1000;

    public void initContent(String input) {
        //input是获取将被解析的字符串
        original_text = input;
        //将图片那一串字符串解析出来,即<img src=="xxx" />
        Pattern p = Pattern.compile("\\<img src=\".*?\"\\/>");
        Matcher m = p.matcher(input);

        image_text_list = new ArrayList<String>();

        //使用SpannableString了，这个不会可以看这里哦：http://blog.sina.com.cn/s/blog_766aa3810100u8tx.html#cmt_523FF91E-7F000001-B8CB053C-7FA-8A0
        spannable = new SpannableString(input);
        while(m.find()){
            haveImage = true;
            //Log.d("YYPT_RGX", m.group());
            //这里s保存的是整个式子，即<img src="xxx"/>，start和end保存的是下标
            String s = m.group();
            int start = m.start();
            int end = m.end();
            //path是去掉<img src=""/>的中间的图片路径
            String path = s.replaceAll("\\<img src=\"|\"\\/>","").trim();
            //Log.d("YYPT_AFTER", path);
            //将图片路径保存到image_text_list中
            image_text_list.add(path);

            Bitmap bitmap = ImageUtils.getSmallBitmap(path,width,height);
            if (bitmap != null) {
                ImageSpan imageSpan = new ImageSpan(context, bitmap);
                spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

    }

    public Spannable getSpannable() {
        return spannable;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    public String getOriginalText() {
        return original_text;
    }

    public String getPureText() {
        return pure_text;
    }

    public List<String> getImageTextList() {
        return image_text_list;
    }


    public void setContext(Context context) {
        this.context = context;
    }

    public static void main(String[] args) {
        String input = " <img src=\"/storage/emulated/0/Pictures/Screenshots/Screenshot_20240402-010454.png/\"/> dgasg asgd sadf g dcg sdg ";
        TestAnalyzer analyzer = new TestAnalyzer();
        analyzer.initContent(input);

        System.out.println("Original Text: " + analyzer.getOriginalText());
        System.out.println("Pure Text: " + analyzer.getPureText());
        System.out.println("Image URLs:");
        for (String imageUrl : analyzer.getImageTextList()) {
            System.out.println(imageUrl);
        }
    }
}
