package com.bvtech.caloriesanalysis;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bvtech.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class foodViewResult extends AppCompatActivity {

    JSONObject myJson;
    File jsonfile;
    ListView listViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_food_view_result);

        ImageButton buttonUserInfo = findViewById(R.id.userInfo);
        buttonUserInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                startActivity(new Intent(foodViewResult.this,InfoActivity.class));
            }
        });

        TextView userName = findViewById(R.id.userNameID);
        myJson = ReadJsonInfo();
        SetHeaderContain(myJson,buttonUserInfo, userName);

        try {
            double caloInDate = myJson.getDouble("TDEE");
            double totalCaloUsePercent = 0.0;
            double percentCalo;
            int sumCalo;
            JSONObject listImage = myJson.getJSONObject("objectList");
            ArrayList<String> listFood = new ArrayList<>();

            for (Iterator<String> it = listImage.keys(); it.hasNext(); ) {
                String imageName = it.next();
                // Get the value associated with the key
                JSONObject listFoodInImage = listImage.getJSONObject(imageName);

                for (Iterator<String> it_food = listFoodInImage.keys(); it_food.hasNext(); ) {
                    String foodNameStr = it_food.next();
                    JSONObject foodInfo = listFoodInImage.getJSONObject(foodNameStr);

                    int calo = foodInfo.getInt("calo");
                    String unit = foodInfo.getString("unit");
                    int numberUnit = foodInfo.getInt("numberUnit");

                    sumCalo = calo*numberUnit;
                    percentCalo = sumCalo*100/caloInDate;
                    totalCaloUsePercent += percentCalo;

                    String food = imageName + ";" + foodNameStr + ";" + calo + ";" + unit + ";" + sumCalo + ";" + decfor.format(percentCalo);
                    listFood.add(food);
                }
            }

            String[] arrayFood = listFood.toArray(new String[0]);

            listViewResult = findViewById(R.id.foodListViewResult);
            AdapterForFoodViewResultItem adapter = new AdapterForFoodViewResultItem(this, arrayFood);
            listViewResult.setAdapter(adapter);

            TextView percentCaloTotal = findViewById(R.id.percentCaloTotal);
            percentCaloTotal.setText("Tổng cộng:\n"+ decfor.format(totalCaloUsePercent)  + "%/100%");

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private static final DecimalFormat decfor = new DecimalFormat("0.00");

    public void buttonGoToHome(View view) throws IOException {
        // Set view
        startActivity(new Intent(foodViewResult.this,MainActivity.class));
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
                startActivity(new Intent(foodViewResult.this,InfoActivity.class));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    private void SetHeaderContain(JSONObject jsonObject, ImageButton button, TextView textView){
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
                String user_name = "Hi, " + jsonObject.getString("name");
                textView.setText(user_name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class AdapterForFoodViewResultItem extends ArrayAdapter<String> {
        private final Activity context;
        private final String[] items;

        public AdapterForFoodViewResultItem(Activity context, String[] items) {
            super(context, R.layout.food_view_item, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            View rowView = view;
            if (rowView == null) {
                // Inflate the layout for each item if it's not already available
                rowView = context.getLayoutInflater().inflate(R.layout.food_view_result_item, null, true);
            }

            String foodInfo_array[] = items[position].split(";");

            // Set food image
            File imgFile = new File(Utils.getDCIMDirectory() + File.separator+ foodInfo_array[0]);
            if (imgFile.exists()) {
                ImageView foodImage = rowView.findViewById(R.id.foodImage);
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                foodImage.setImageBitmap(bitmap);
            }

            // Set food name
            TextView foodName = rowView.findViewById(R.id.foodName);
            foodName.setText(foodInfo_array[1]); // Set text for each item

            // Set food calo
            TextView foodCalo = rowView.findViewById(R.id.foodCalo);
            foodCalo.setText(foodInfo_array[2] + " calo/" + foodInfo_array[3]); // Set text for each item

            // set result calo process
            TextView result = rowView.findViewById((R.id.foodCaloResult));
            result.setText("Kết quả: " + foodInfo_array[4] + " calo chiếm " + foodInfo_array[5]+ "% tổng số calo bạn cần nạp trong 1 ngày");


            return rowView;
        }
    }
}