package com.vhallyun.videouplod;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.upload.VHUploadCallBack;
import com.vhall.upload.VhallUploadFile;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {


    private TextView upload;
    private TextView path;
    private SeekBar progress;
    private EditText name;
    private EditText token;
    private List<String> listFile = new ArrayList<>();
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        upload = findViewById(R.id.tv_upload);
        path = findViewById(R.id.tv_path);
        progress = findViewById(R.id.progress);
        name = findViewById(R.id.ed_name);
        token = findViewById(R.id.ed_token);
        getStoragePermission();
        findViewById(R.id.tv_choose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listFile == null || listFile.size() == 0) {
                    Toast.makeText(MainActivity.this, "请选择至少一个视频  ", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "开始上传  ", Toast.LENGTH_SHORT).show();
                count = 0;
                VhallUploadFile.getInstance().initAccessToken(token.getText().toString().trim());
                if (listFile.size() > 1) {
                    progress.setMax(listFile.size());
                    uploadFile(listFile.size());
                } else {
                    uploadFile(1);
                }
            }
        });
    }
    private static final int REQUEST_STORAGE = 1;
    private boolean getStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        return false;
    }
    private void uploadFile(final int allCount) {
        VhallUploadFile.getInstance().uploadFileSimple(MainActivity.this, listFile.get(count), name.getText().toString() + count, new VHUploadCallBack() {
            @Override
            public void onSuccess(String recordId) {
                if (allCount == 1) {
                    Toast.makeText(MainActivity.this, "上传成功  " + recordId, Toast.LENGTH_SHORT).show();
                    listFile.clear();
                    path.setText("请选择至少一个视频");
                    return;
                }
                count++;
                progress.setProgress(count);
                if (count == allCount) {
                    Toast.makeText(MainActivity.this, "上传成功  " + recordId, Toast.LENGTH_SHORT).show();
                    listFile.clear();
                    path.setText("请选择至少一个视频");
                    return;
                }
                uploadFile(allCount);
            }

            @Override
            public void onError(int code, String msg) {
                if (allCount == 1) {
                    Toast.makeText(MainActivity.this, "上传失败  " + msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, count + "上传失败  " + msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(long currentSize, long totalSize) {
                if (allCount == 1) {
                    if (progress.getMax() != 0) {
                        progress.setMax((int) totalSize);
                    }
                    progress.setProgress((int) currentSize);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 选取图片的返回值
        if (requestCode == 1) {
            //
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                assert uri != null;
                Cursor cursor = getContentResolver().query(uri, null, null,
                        null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        String v_path = cursor.getString(1);
                        String v_name = cursor.getString(2);
                        String v_size = cursor.getString(3);
                        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                        Log.e("MainActivity", "v_path= " + v_path);
                        Log.e("MainActivity", "v_size= " + v_size);
                        Log.e("MainActivity", "v_name= " + v_name);
                        Log.e("MainActivity", "v_time= " + duration);
                        cursor.close();
                        if (listFile.contains(v_path)) {
                            Toast.makeText(MainActivity.this, "这个视频已经选择过了  ", Toast.LENGTH_SHORT).show();
                        } else {
                            listFile.add(v_path);
                        }
                        path.setText(String.format("选择了%d个视频", listFile.size()));
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "选择适合的视频 ", Toast.LENGTH_SHORT).show();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
