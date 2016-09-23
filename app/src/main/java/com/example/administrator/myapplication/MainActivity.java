package com.example.administrator.myapplication;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;

/**
 * 安卓自带下载功能测试
 */
public class MainActivity extends AppCompatActivity {

    private Button button; // 点击开始下载文件
    private DownloadManager dm;
    private long mTaskId; // 下载任务ID号
    private TextView mTextViewShowPopWindow; // 点击显示PopMenu
    private PopupWindow mPopupWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linerar_layout);

        mTextViewShowPopWindow = (TextView) findViewById(R.id.tv_show_popmenu);
        mTextViewShowPopWindow.setOnClickListener(new textClick());

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("http://www.baidu.com");
        webView.getSettings().setJavaScriptEnabled(true);


        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);
                //返回值是true时为webview打开
                return true;
            }
        });

        //下载文件
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = "http://sw.bos.baidu.com/sw-search-sp/software/6c7303b41fffa/BaiduPlayer5_5.3.1.44_Setup_2.exe";
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setAllowedOverRoaming(false);// 设置漫游网络是否下载
//                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);// 设置只在WIFI下才下载
                String filename = url.substring(url.lastIndexOf("/") + 1);
                // 设置保存在当前程序下的DOWNLOAD目录下
                request.setDestinationInExternalFilesDir(getApplicationContext(), Environment.DIRECTORY_DOWNLOADS, filename);

                // 得到下载管理器
                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                // 将下载任务加入队列,得到ID,可通过ID控制下载任务
                mTaskId = dm.enqueue(request);

                //注册广播接收者，监听下载状态
//                registerReceiver(receiver, DownloadManager.ACTION_DOWNLOAD_COMPLETE);


            }
        });


        // 设置弹出窗口
        mPopupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setContentView(getLayoutInflater().inflate(R.layout.activity_pop, null));
        mPopupWindow.setFocusable(true);//取得焦点
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPopupWindow.setAnimationStyle(R.style.pop_menu_anim); // 设置动画




    }

    // 这些内容最好在service中进行操作
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkDownloadStatus();//检查下载状态
        }
    };

    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//筛选下载任务，传入任务ID，可变参数
        Cursor c = dm.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    //下载暂停
                case DownloadManager.STATUS_PENDING:
                    //下载延迟
                case DownloadManager.STATUS_RUNNING:
                    //正在下载
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    //下载完成安装APK
//                    downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + versionName;
//                    installAPK(new File(downloadPath));
                    break;
                case DownloadManager.STATUS_FAILED:
                    //下载失败
                    break;
            }
        }
    }


    //下载到本地后执行安装
    protected void installAPK(File file) {
        if (!file.exists()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://" + file.toString());
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //在服务中开启activity必须设置flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // TextView点击事件
    private class textClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            //显示PopupWindow
            mPopupWindow.showAtLocation(v, Gravity.BOTTOM,0,0);

        }
    }
}
