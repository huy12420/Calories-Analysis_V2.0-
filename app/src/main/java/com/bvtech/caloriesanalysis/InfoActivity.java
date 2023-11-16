package com.bvtech.caloriesanalysis;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;

public class InfoActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 2;
    ImageButton buttonUserInfo;
    JSONObject infoJson;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        setContentView(R.layout.activity_info);

        buttonUserInfo = findViewById(R.id.setImage);

        EditText name = findViewById(R.id.name);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    infoJson.put("name", name.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        EditText gender = findViewById(R.id.gender);
        gender.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    infoJson.put("gender", gender.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        EditText height = findViewById(R.id.height);
        height.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    infoJson.put("height", height.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        EditText weight = findViewById(R.id.weight);
        weight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    infoJson.put("weight", weight.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Spinner expenditure = findViewById(R.id.activate_time);
        expenditure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    infoJson.put("expenditure", Integer.toString(position));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
//                String selectedItem = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        EditText age = findViewById(R.id.age);
        age.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    infoJson.put("age", age.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        infoJson = OnLoadJson();

        if (infoJson != null){
            try {
                // Set avatar
                String avatarPath_string = infoJson.getString("avatarPath");
                File imgFile = new File(avatarPath_string);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    buttonUserInfo.setImageBitmap(bitmap);
                }
                // Set Name
                String name_string = infoJson.getString("name");
                name.setText(name_string);
                // Set gender
                String gender_string = infoJson.getString("gender");
                gender.setText(gender_string);
                // Set height
                String height_string = infoJson.getString("height");
                height.setText(height_string);
                // Set weight
                String weight_string = infoJson.getString("weight");
                weight.setText(weight_string);
                // Set expenditure
                String expenditure_string = infoJson.getString("expenditure");
                int expenditure_index = Integer.parseInt(expenditure_string);
                expenditure.setSelection(expenditure_index);
                // Set age
                String age_string = infoJson.getString("age");
                age.setText(age_string);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        buttonUserInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
//                Toast.makeText(InfoActivity.this, "Hello", Toast.LENGTH_SHORT).show();
                openGallery();
            }
        });
    }
    public void buttonInfoGoBack(View view) throws IOException {
        // Add Object list child
        try {
            String gender = infoJson.getString("gender").toLowerCase();
            int weight = infoJson.getInt("weight");
            int height = infoJson.getInt("height");
            int age = infoJson.getInt("age");
            double BMR = 0;

            if (gender == "nam"){
                BMR = 66 + (13.7 * weight) + (5 * height) - (6.8 * age);
            }else{
                BMR = 655 + (9.6 * weight) + (1.8 * height) - (4.7 * age);
            }
            infoJson.put("BMR", Math.round(BMR * 100.0) / 100.0);

            int expenditure_index = infoJson.getInt("expenditure");
            double[] expenditure = {1.1, 1.2, 1.375, 1.55};
            double expenditure_double = expenditure[expenditure_index];
            double TDEE = 0.0;
            TDEE = expenditure_double * BMR;
            infoJson.put("TDEE", Math.round(TDEE * 100.0) / 100.0);

            double BMI = weight/((height/100.0)*(height/100.0));
            infoJson.put("BMI", Math.round(BMI * 100.0) / 100.0);

            JSONObject childObject = new JSONObject();
            infoJson.put("objectList", childObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //update json data
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(infoJson.toString().getBytes());
        fos.close();

        // Set view
        startActivity(new Intent(InfoActivity.this,MainActivity.class));
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE);
    }

    private JSONObject OnLoadJson(){
        JSONObject jsonObject = null;
        try {
            String fileName = "info.json";

            // Tạo một File object
            file = new File(getApplicationContext().getFilesDir(), fileName);

            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                int size = fis.available();
                byte[] buffer = new byte[size];
                fis.read(buffer);
                fis.close();

                String json = new String(buffer, "UTF-8");
                jsonObject = new JSONObject(json);

//                Toast.makeText(InfoActivity.this, "JsonOke", Toast.LENGTH_SHORT).show();
            } else {
                file.createNewFile();
                jsonObject = new JSONObject();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                if (bitmap != null) {
                    buttonUserInfo.setImageBitmap(bitmap);
                    String path = SaveBitMap(bitmap);
                    infoJson.put("avatarPath", path);
//                    Toast.makeText(InfoActivity.this, infoJson.getString("avatarPath"), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String SaveBitMap(Bitmap bitmap){
        String directory = getApplicationContext().getFilesDir().toString();
        File file = new File(directory, "avatar.png");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Toast.makeText(InfoActivity.this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        String path = file.getAbsolutePath();

        return path;
    }
}
