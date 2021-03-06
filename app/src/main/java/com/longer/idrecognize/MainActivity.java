package com.longer.idrecognize;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("OpenCV");
    }

    private TessBaseAPI tessBaseApi;
    private String language = "ck";
    private AsyncTask<Void, Void, Boolean> asyncTask;
    private ProgressDialog progressDialog;////////////////
    private ImageView idCard;
    private ImageView img_step_1, img_step_2, img_step_3, img_step_4, img_step_5;
    private TextView tesstext;
    private int index = 0;
    private int[] ids = {
            R.drawable.id_card0,
            R.drawable.id_card1,
            R.drawable.id_card2,
            R.drawable.id_card3,
            R.drawable.id_card4,
            R.drawable.id_card5,
            R.drawable.id_card6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        TextView tv  = findViewById(R.id.tv_idcard);
//        tv.setText(stringFromJNI());

        idCard = findViewById(R.id.idcard);
        tesstext = findViewById(R.id.tv_idcard);
        img_step_1 = findViewById(R.id.img_step_1);
        img_step_2 = findViewById(R.id.img_step_2);
        img_step_3 = findViewById(R.id.img_step_3);
        img_step_4 = findViewById(R.id.img_step_4);
        img_step_5 = findViewById(R.id.img_step_5);
        idCard.setImageResource(R.drawable.id_card0);
        //15
        tessBaseApi = new TessBaseAPI();/////////////
        methodRequiresTwoPermission();
    }

    private native String stringFromJNI();


    @SuppressLint("StaticFieldLeak")
    private void initTess() {
        //??????????????????????????? ?????????????????????
        this.asyncTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                //??????+????????? ???????????????tessdata??????
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    is = getAssets().open(language + ".traineddata");
                    File file = new File("/sdcard/tess/tessdata/" + language + ".traineddata");
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();

                        file.createNewFile();

                        fos = new FileOutputStream(file);
                        byte[] buffer = new byte[2048];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                    is.close();
                    return tessBaseApi.init("/sdcard/tess", language);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != is)
                            is.close();
                        if (null != fos)
                            fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            protected void onPreExecute() {
                showProgress();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                dismissProgress();
                if (aBoolean) {
                    Toast.makeText(MainActivity.this, "?????????OCR??????", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        };
        asyncTask.execute();
    }

    public static final int RC_CAMERA_AND_LOCATION = 0x0001;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private void methodRequiresTwoPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(MainActivity.this, "?????????", Toast.LENGTH_SHORT).show();

            long startTime = System.currentTimeMillis(); //????????????
//            doSomething();

            initTess();

            long endTime = System.currentTimeMillis(); //????????????
            long runTime = endTime - startTime;
            Log.i("test", String.format("?????????ocr ???????????? %d ms", runTime));


        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "????????????",
                    RC_CAMERA_AND_LOCATION, perms);
        }
    }

    private void showProgress() {
        if (null != progressDialog) {
            progressDialog.show();
        } else {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("?????????...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    private void dismissProgress() {
        if (null != progressDialog) {
            progressDialog.dismiss();
        }
    }

    public void previous(View view) {
        tesstext.setText(null);
        index--;
        if (index < 0) {
            index = ids.length - 1;
        }
        idCard.setImageResource(ids[index]);
    }

    public void next(View view) {
        tesstext.setText(null);
        index++;
        if (index >= ids.length) {
            index = 0;
        }
        idCard.setImageResource(ids[index]);
    }

    public void rt(View view) {
        //??????????????????????????????
        //?????????Bitmap???????????????????????????Bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ids[index]);

        long startTime = System.currentTimeMillis(); //????????????

            Bitmap bitmap1 = removeColor(bitmap, Bitmap.Config.ARGB_8888);
            img_step_1.setImageBitmap(bitmap1);
            // ?????????

        long endTime = System.currentTimeMillis(); //????????????
        long runTime = endTime - startTime;
        Log.i("test", String.format("????????????????????? %d ms", runTime));


        long startTime1 = System.currentTimeMillis(); //????????????
        //doSomething();

            // ?????????
            Bitmap bitmap2 = twoColor(bitmap1, Bitmap.Config.ARGB_8888);
            img_step_2.setImageBitmap(bitmap2);

//        long endTime1 = System.currentTimeMillis(); //????????????
//        long runTime1 = endTime1 - startTime1;
//        Log.i("test", String.format("????????????????????? %d ms", runTime1));


//        long startTime2 = System.currentTimeMillis(); //????????????
        //doSomething();

            // ????????????
            Bitmap bitmap3 = swellImg(bitmap2, Bitmap.Config.ARGB_8888);
            img_step_3.setImageBitmap(bitmap3);

//        long endTime2 = System.currentTimeMillis(); //????????????
//        long runTime2 = endTime2 - startTime2;
//        Log.i("test", String.format("???????????????????????? %d ms", runTime2));


//        long startTime3 = System.currentTimeMillis(); //????????????
        //doSomething();

            // ????????????
            Bitmap bitmap4 = outSideImage(bitmap3, Bitmap.Config.ARGB_8888);
            img_step_4.setImageBitmap(bitmap4);

//        long endTime3 = System.currentTimeMillis(); //????????????
//        long runTime3 = endTime3 - startTime3;
//        Log.i("test", String.format("???????????????????????? %d ms", runTime3));


//        long startTime4 = System.currentTimeMillis(); //????????????
        //doSomething();

            // ????????????
            Bitmap bitmap5 = cropImage(bitmap, Bitmap.Config.ARGB_8888);
            if (bitmap5 == null) {
                Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                return;
            }
            img_step_5.setImageBitmap(bitmap5);

        long endTime1 = System.currentTimeMillis(); //????????????
        long runTime1 = endTime1 - startTime1;
        Log.i("test", String.format("???????????????????????? %d ms", runTime1));


        long startTime5 = System.currentTimeMillis(); //????????????
        //doSomething();

            //OCR????????????
            //14 ????????????????????????
            //15 ????????????
            tessBaseApi.setImage(bitmap5);
            tesstext.setText("??????????????????" + tessBaseApi.getUTF8Text());

        long endTime5 = System.currentTimeMillis(); //????????????
        long runTime5 = endTime - startTime;
        Log.i("test", String.format("?????????????????? %d ms", runTime5));




<<<<<<< HEAD
=======
        //OCR????????????
        //14 ????????????????????????
        //15 ????????????
        tessBaseApi.setImage(bitmap5);
        tesstext.setText("??????????????????" + tessBaseApi.getUTF8Text());
>>>>>>> 2a89e9ac6e28470771baa10656a82ccd748b6de4
    }

    private native Bitmap cropImage(Bitmap bitmap4, Bitmap.Config argb8888);

    private native Bitmap outSideImage(Bitmap bitmap3, Bitmap.Config argb8888);

    private native Bitmap swellImg(Bitmap bitmap2, Bitmap.Config argb8888);

    private native Bitmap twoColor(Bitmap bitmap1, Bitmap.Config argb8888);

    private native Bitmap removeColor(Bitmap bitmap, Bitmap.Config argb8888);

}