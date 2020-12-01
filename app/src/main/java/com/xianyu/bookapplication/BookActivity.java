package com.xianyu.bookapplication;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xianyu.bookapplication.view.BookPageBezierHelper;
import com.xianyu.bookapplication.view.BookPageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BookActivity extends AppCompatActivity {

    public static final String FILE_PATH = "file_path";
    public static final String IMOOC_PREFERENCE_BOOK_PROGRESS_NAME = "imooc_preference_book_progress";
    public static final String BOOKMARK = "bookmark";
    private BookPageView mBookPageView;
    private TextView mProgressTextView;
    private View mSettingView;
    private RecyclerView mRecyclerView;
    private static final String TAG = "BookActivity";
    private int mCurrentLength;
    private BookPageBezierHelper mHelper;
    private Bitmap mCurrentPageBitmap;
    private Bitmap mNextPageBitmap;
    private String mFilePath;
    private int mWidth;
    private int mHeight;
    private int mLastLength;
    private TextToSpeech mTTS;
    private SeekBar mSeekBar;
    private int mTotalLength;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // fullscreen
        getWindow().setStatusBarColor(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setNavigationBarColor(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        setContentView(R.layout.activity_book);

        if (getIntent() != null) {
            mFilePath = getIntent().getStringExtra(FILE_PATH);
            if (!TextUtils.isEmpty(mFilePath)) {
                mTotalLength = (int) new File(mFilePath).length();
            }
        } else {
            // todo  can not find the book path
        }
        // init view
        mBookPageView = (BookPageView) findViewById(R.id.book_page_view);
        mProgressTextView = (TextView) findViewById(R.id.progress_text_view);
        mSettingView = findViewById(R.id.setting_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.settingRecyclerView);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        // get Size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mWidth = displayMetrics.widthPixels;
        mHeight = displayMetrics.heightPixels;
        openBookByProgress(R.drawable.book_bg, 0);
        //set progress

        mHelper.setOnProgressChangedListener(new BookPageBezierHelper.OnProgressChangedListener() {
            @Override
            public void setProgress(int currentLength, int totalLength) {
                mCurrentLength = currentLength;
                float progress = mCurrentLength * 100 / totalLength;
                mProgressTextView.setText(String.format("%s%%", progress));
            }
        });

        // set user setting view listener.

        mBookPageView.setOnUserNeedSettingListener(new BookPageView.OnUserNeedSettingListener() {
            @Override
            public void onUserNeedSetting() {
                mSettingView.setVisibility(mSettingView.getVisibility() == View.VISIBLE
                        ? View.GONE : View.VISIBLE);
            }
        });

        // set recyclerView data.

        List<String> settingList = new ArrayList<>();
        settingList.add("添加书签");
        settingList.add("读取书签");
        settingList.add("设置背景");
        settingList.add("语音朗读");
        settingList.add("跳转进度");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new HorizontalAdapter(this, settingList));

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                openBookByProgress(R.drawable.book_bg, seekBar.getProgress() * mTotalLength / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                openBookByProgress(R.drawable.book_bg, seekBar.getProgress() * mTotalLength / 100);
            }
        });
    }

    private void openBookByProgress(int backgroundResourceID, int progress) {
        // set book helper
        mHelper = new BookPageBezierHelper(mWidth, mHeight, progress);
        mBookPageView.setBookPageBezierHelper(mHelper);

        // current page , next page
        mCurrentPageBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mNextPageBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mBookPageView.setBitmaps(mCurrentPageBitmap, mNextPageBitmap);

        // set background
        mHelper.setBackground(this, backgroundResourceID);
        // open book
        if (!TextUtils.isEmpty(mFilePath)) {
            try {
                mHelper.openBook(mFilePath);
                mHelper.draw(new Canvas(mCurrentPageBitmap));
                mBookPageView.invalidate();
            } catch (IOException e) {
                e.printStackTrace();
                // todo  can not find the book path
            }
        } else {
            // todo  can not find the book path
        }
    }

    public static void start(Context context, String filePath) {
        Intent intent = new Intent(context, BookActivity.class);
        intent.putExtra(FILE_PATH, filePath);
        context.startActivity(intent);

    }

    private class HorizontalAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private List<String> mData = new ArrayList<>();

        public HorizontalAdapter(Context context, List<String> list) {
            mContext = context;
            mData = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView itemView = new TextView(mContext);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            TextView textView = (TextView) holder.itemView;
            textView.setWidth(240);
            textView.setHeight(180);
            textView.setTextSize(16);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setText(mData.get(position));
            final SharedPreferences sharedPreferences =
                    mContext.getSharedPreferences("imooc_book_preference", MODE_PRIVATE);

            final SharedPreferences.Editor editor = sharedPreferences.edit();

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (position) {
                        case 0:
                            // add bookmark
                            // get progress
                            // save progress to preference.
                            editor.putInt(BOOKMARK, mCurrentLength);
                            editor.apply();
                            break;
                        case 1:
                            // get bookmark from preference.
                            // reload book to the progress.
                            mLastLength = sharedPreferences.getInt(BOOKMARK, 0);
                            openBookByProgress(R.drawable.book_bg, mLastLength);
                            break;
                        case 2:
                            openBookByProgress(R.drawable.book_bg2, mLastLength);
                            break;
                        case 3:
                            if (mTTS == null) {
                                mTTS = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                                    @Override
                                    public void onInit(int status) {
                                        if (status == TextToSpeech.SUCCESS) {
                                            int result = mTTS.setLanguage(Locale.CHINA);
                                            if (result == TextToSpeech.LANG_MISSING_DATA
                                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                                Log.e(TAG, "onInit: language is not available.");
                                                Uri uri = Uri.parse("http://acj2.pc6.com/pc6_soure/2017-6/com.iflytek.vflynote_208.apk");
                                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                startActivity(intent);
                                            } else {
                                                Log.i(TAG, "onInit: init success.");
                                                mTTS.speak(mHelper.getCurrentPageContent(), TextToSpeech.QUEUE_FLUSH, null);

                                            }
                                        } else {
                                            Log.e(TAG, "onInit: error");
                                        }
                                    }
                                });
                            } else {
                                if (mTTS.isSpeaking()) {
                                    mTTS.stop();
                                } else {
                                    mTTS.speak(mHelper.getCurrentPageContent(), TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                            break;
                        case 4:
                            mSeekBar.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mTextView;

            public ViewHolder(TextView itemView) {
                super(itemView);
                mTextView = itemView;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTTS != null) {
            mTTS.shutdown();
        }
    }
}




