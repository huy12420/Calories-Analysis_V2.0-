package com.bvtech.caloriesanalysis;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.BufferedReader;
import java.io.FileReader;

import android.view.PixelCopy;
import android.graphics.Rect;
import android.os.Handler;

import com.bvtech.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    Integer caloList[] = {200,29,51,175,627,195,328,301,114,146,195,200,315,479,530,173,123,247,412,538,431,483,239,58,141,226,106,137,152,184,179,28,21,56,8,131,125,573,225,400,99,880,416,340,1030};
    String class_names[] = {"Cơm trắng","Bò xào đậu que","com chiên dương châu","Canh bí đao","Canh khoai mỡ","Canh khổ qua","Cơm tấm sườn bì","Chả trứng chưng","Đậu hủ dồn thịt","Gà kho gừng","Khổ qua xào trứng","Thịt heo quay","Thịt kho tiêu","Thịt kho rứng","Bún bò huế","Đùi gà chiên","Sườn nướng","Tôm lăng bột chiên","Cháo lòng","Bò kho","Phở gà","Phở bò","Bánh mì ổ","Trứng gà ta","Bia","Nước cam vắt","Nước mía","Sữa chua Vinamilk","Sữa hộp","Bơ","Xoài","Sầu riêng","Dưa hấu","Mãng cầu","Bưởi", "Khoai lan","Đu đủ","Đậu phộng","Thanh long","Hủ tiếu nam vang","Bánh bao","Bánh tét","Bánh xèo miền tây","Lạp xưởng","Cua biển"};
    String Unit[]= {"1 chén vừa","1 chén","1 chén","1 chén","1 đĩa cơm phần","1 lát","1 miếng lớn","1 đĩa","1 đĩa","1 đĩa","1 đĩa","1 đĩa","1 trứng + 2 miếng thịt","1 tô","1 đĩa","1 cái","1 miếng","1 đĩa","1 tô","1 tô","1 tô","1 tô","1 ổ trung bình","1 quả","1 ly","1 ly","1 ly","1 hủ nhỏ","1 hộp nhỏ","1 trái","1 trái","1 trái","1 miếng","1 trái","1 múi","1 củ","1 miếng","1 đĩa nhỏ","1 trái","1 tô","100g","2 khoanh","1 cái","100g","1kg"};
    public static final int REQUEST_CAMERA = 100;

    private final Yolov8Ncnn yolov8ncnn = new Yolov8Ncnn();
    private int facing = 0;
    private int current_cpugpu = 1;
    private SurfaceView cameraView;
    String savedImagePath = "";
    JSONObject myJson;
    File jsonfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraView = findViewById(R.id.cameraview);

        cameraView.getHolder().setFormat(PixelFormat.RGBA_8888);
        cameraView.getHolder().addCallback(this);

        ImageButton buttonUserInfo = findViewById(R.id.userInfo);
        buttonUserInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                yolov8ncnn.closeCamera();
                startActivity(new Intent(MainActivity.this,InfoActivity.class));
            }
        });

        TextView userName = findViewById(R.id.userNameID);
        myJson = ReadJsonInfo();
        SetHeaderContain(myJson,buttonUserInfo, userName);

        if (!checkAllPermissions()) {
            requestAllPermissions();
        }

        Button buttonSwitchCamera = findViewById(R.id.buttonSwitchCamera);
        buttonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                int new_facing = 1 - facing;

                yolov8ncnn.closeCamera();

                yolov8ncnn.openCamera(new_facing);

                facing = new_facing;
            }
        });

        Button btnShutter = findViewById(R.id.buttonShutterCamera);
        btnShutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SimpleDateFormat date = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                String imageFileName = date.format(new Date()).toString() + ".jpg";
                synchronized (this) {

                    PixelCopy.OnPixelCopyFinishedListener listener = new PixelCopy.OnPixelCopyFinishedListener() {

                        @Override
                        public void onPixelCopyFinished(int copyResult) {

                            if (copyResult == PixelCopy.SUCCESS) {
                                Toast.makeText(MainActivity.this, "Captured", Toast.LENGTH_SHORT).show();

                                String fileName = "classID.txt";
                                String fileContent = readCacheFile(fileName);
                                String[] ids = fileContent.split(" ");
                                // Add Object list child
                                try {
                                    JSONObject childObject = new JSONObject();

                                    for (String id : ids) {
                                        int int_id = Integer.parseInt(id);
                                        if (int_id >= 0 && int_id <= 44) {
                                            JSONObject infoChildObject = new JSONObject();
                                            infoChildObject.put("calo",caloList[int_id]);
                                            infoChildObject.put("unit",Unit[int_id]);
                                            infoChildObject.put("numberUnit",1);

                                            childObject.put(class_names[int_id], infoChildObject);
//                                Toast.makeText(MainActivity.this, class_names[int_id] + ": " + caloList[int_id]+ "/"+ Unit[int_id], Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    JSONObject objectList = myJson.getJSONObject("objectList");
                                    objectList.put(imageFileName, childObject);

//                        Update json data
                                    FileOutputStream fos = new FileOutputStream(jsonfile);
                                    fos.write(myJson.toString().getBytes());
                                    fos.close();
                                } catch (JSONException | IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Capture fail", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };

                    Rect rect = new Rect(0, 0, cameraView.getWidth(), cameraView.getHeight());

                    savedImagePath = Utils.getDCIMDirectory() + File.separator + imageFileName;
                    Bitmap b = Bitmap.createBitmap(cameraView.getWidth(), cameraView.getHeight(), Bitmap.Config.ARGB_8888);
                    File file = new File(savedImagePath);

                    try {
                        PixelCopy.request(cameraView, rect, b, listener, new Handler());
                        b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
//                        Toast.makeText(MainActivity.this, "Save snapshot to " + savedImagePath, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    startActivity(new Intent(MainActivity.this,foodView.class));
                }
            }
        });

        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                RenewFoodList();
            }
        });

        reload();
//        Toast.makeText(MainActivity.this, "Model loaded", Toast.LENGTH_SHORT).show();
    }

    public String readCacheFile(String fileName) {
        StringBuilder content = new StringBuilder();
        File file = new File(getApplicationContext().getCacheDir(), fileName);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line); // Append each line to content
            }
            reader.close(); // Close the reader
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString(); // Return the content of the file
    }

    private void RenewFoodList() {
        try {
            JSONObject childObject = new JSONObject();
            myJson.put("objectList", childObject);
            //Update json data
            FileOutputStream fos = new FileOutputStream(jsonfile);
            fos.write(myJson.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void reload()
    {
        boolean ret_init = yolov8ncnn.loadModel(getAssets(), current_cpugpu);
        if (!ret_init)
        {
            Log.e("MainActivity", "yolov8ncnn loadModel failed");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permission denied")
                    .setMessage("Click to force quit the app, then open Settings->Apps & notifications->Target " +
                            "App->Permissions to grant all of the permissions.")
                    .setCancelable(false)
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    }).show();
        }
    }

    private void requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES ,
                    Manifest.permission.CAMERA}, 100);
        }else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 100);
        }
    }

    private boolean checkAllPermissions() {
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private JSONObject ReadJsonInfo(){
        JSONObject jsonObject = null;
        try {
            String fileName = "info.json";

            // Tạo một File object
            jsonfile = new File(getApplicationContext().getFilesDir(), fileName);

            if (jsonfile.exists()) {
                FileInputStream fis = new FileInputStream(jsonfile);
                int size = fis.available();
                byte[] buffer = new byte[size];
                fis.read(buffer);
                fis.close();

                String json = new String(buffer, "UTF-8");
                jsonObject = new JSONObject(json);

            } else {
                startActivity(new Intent(MainActivity.this,InfoActivity.class));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void SetHeaderContain(JSONObject jsonObject,ImageButton button, TextView textView){
        try {
            if (jsonObject != null) {
                // Set bg
                String avatarPath_string = jsonObject.getString("avatarPath");
                File imgFile = new File(avatarPath_string);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    button.setImageBitmap(bitmap);
                }

                //Set name
                String user_name = "Hi " + jsonObject.getString("name");
                textView.setText(user_name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        yolov8ncnn.setOutputWindow(holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        yolov8ncnn.openCamera(facing);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        yolov8ncnn.closeCamera();
    }
}