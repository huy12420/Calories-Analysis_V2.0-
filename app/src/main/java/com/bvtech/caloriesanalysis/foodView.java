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
import android.widget.TextView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.bvtech.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.ArrayList;

public class foodView extends AppCompatActivity {
    JSONObject myJson;
    File jsonfile;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_food_view);

        ImageButton buttonUserInfo = findViewById(R.id.userInfo);
        buttonUserInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                startActivity(new Intent(foodView.this,InfoActivity.class));
            }
        });

        TextView userName = findViewById(R.id.userNameID);
        myJson = ReadJsonInfo();
        SetHeaderContain(myJson,buttonUserInfo, userName);

        try {
            JSONObject listImage = myJson.getJSONObject("objectList");
            ArrayList<String> listFood = new ArrayList<>();

            for (Iterator<String> it = listImage.keys(); it.hasNext(); ) {
                String imageName = it.next();
                // Get the value associated with the key
                JSONObject listFoodInImage = listImage.getJSONObject(imageName);

                for (Iterator<String> it_food = listFoodInImage.keys(); it_food.hasNext(); ) {
                    String foodNameStr = it_food.next();
                    JSONObject foodInfo = listFoodInImage.getJSONObject(foodNameStr);

                    String calo = foodInfo.getString("calo");
                    String unit = foodInfo.getString("unit");
                    String numberUnit = foodInfo.getString("numberUnit");

                    String food = imageName + ";" +foodNameStr+ ";" +calo+ ";" +unit+ ";" +numberUnit;
                    listFood.add(food);

//                    Toast.makeText(foodView.this, food , Toast.LENGTH_SHORT).show();
                }
            }

//            String[] items = {"Item 1", "Item 2", "Item 3", "Item 4"};
            String[] arrayFood = listFood.toArray(new String[0]);

            listView = findViewById(R.id.foodListView);
            AdapterForFoodViewItem adapter = new AdapterForFoodViewItem(this, arrayFood);
            listView.setAdapter(adapter);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public void buttonInfoGoBack(View view) throws IOException {
        // Set view
        startActivity(new Intent(foodView.this,MainActivity.class));
    }

    public void buttonCalculator(View view) throws IOException {
        // Set view
        startActivity(new Intent(foodView.this, foodViewResult.class));
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
                startActivity(new Intent(foodView.this,InfoActivity.class));
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
                String user_name = "Hi " + jsonObject.getString("name");
                textView.setText(user_name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class AdapterForFoodViewItem extends ArrayAdapter<String> {
        private final Activity context;
        private final String[] items;

        public AdapterForFoodViewItem(Activity context, String[] items) {
            super(context, R.layout.food_view_item, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            View rowView = view;
            if (rowView == null) {
                // Inflate the layout for each item if it's not already available
                rowView = context.getLayoutInflater().inflate(R.layout.food_view_item, null, true);
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

            // Set number unit
            NumberPicker number = rowView.findViewById(R.id.numberPicker);
            number.setMinValue(0);
            number.setMaxValue(20);

            // Set a default value
            number.setValue(Integer.parseInt(foodInfo_array[4]));

            // Set a listener to respond to value changes
            number.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                    // Do something when the value changes
                    UpdateJsonUnitValue(position,newVal);
                }
            });

            return rowView;
        }

        private void UpdateJsonUnitValue(int position,int value){
            JSONObject sourceJson = ReadJsonInfo();
            int count = 0;
            boolean singleBreak = false;
            try {
                JSONObject listImage = sourceJson.getJSONObject("objectList");

                for (Iterator<String> it = listImage.keys(); it.hasNext(); ) {
                    String imageName = it.next();
                    // Get the value associated with the key
                    JSONObject listFoodInImage = listImage.getJSONObject(imageName);

                    for (Iterator<String> it_food = listFoodInImage.keys(); it_food.hasNext(); ) {
                        String foodNameStr = it_food.next();

                        if (count == position){
                            JSONObject foodInfo = listFoodInImage.getJSONObject(foodNameStr);
                            String calo = foodInfo.getString("calo");
                            String unit = foodInfo.getString("unit");
                            String numberUnit = foodInfo.getString("numberUnit");

                            JSONObject newFoodInfo = new JSONObject();
                            newFoodInfo.put("calo",calo);
                            newFoodInfo.put("unit",unit);
                            newFoodInfo.put("numberUnit",Integer.toString(value));

                            listFoodInImage.put(foodNameStr, newFoodInfo);
                            listImage.put(imageName,listFoodInImage);
                            sourceJson.put("objectList",listImage);

//                          Update json data
                            FileOutputStream fos = new FileOutputStream(jsonfile);
                            fos.write(sourceJson.toString().getBytes());
                            fos.close();
//                            Toast.makeText(foodView.this, numberUnit , Toast.LENGTH_SHORT).show();
                            singleBreak = true;
                            break;
                        }
                        count += 1;
                    }
                    if (singleBreak){break;}
                }
            } catch (JSONException | FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}