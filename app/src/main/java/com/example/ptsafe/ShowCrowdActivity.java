package com.example.ptsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ptsafe.model.News;
import com.example.ptsafe.model.ObjectDetection;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShowCrowdActivity extends AppCompatActivity {

    private ImageView photoIv;
    private TextView totalCountTv;
    private TextView totalPercentageTv;
    private TextView suggestionTv;
    private Button backToMainBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_crowd);
        initView();
        try {
            getIntentData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        backToMainBtn.setOnClickListener(setBackToMainMenuBtn());
    }

    private View.OnClickListener setBackToMainMenuBtn() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowCrowdActivity.this, MainActivity.class);
                startActivity(intent);
            }
        };
    }

    private void getIntentData() throws IOException {
        Intent intent = getIntent();
        Uri photoUri = Uri.parse(intent.getStringExtra("imageUri"));
        Log.i("photouri", photoUri.getPath());
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(intent.getStringExtra("imageUri")));
        photoIv.setImageBitmap(bitmap);
        detectObject(photoUri);
    }

    //todo: create post multiform function
    private void detectObject(Uri uri) {
        String url = "https://ptsafe-object-detection-api.herokuapp.com/v1/predict";
        File file = null;
        String realPath = getRealPath(uri);
        try {
            file = new File(realPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", realPath, RequestBody.create(MediaType.parse("image/jpeg"), file))
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject resultObj = null;
                int numberOfPeople = 0;
                float percentageOfPeople = 0;
                int status = 0;
                try {
                    resultObj = new JSONObject(response.body().string());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    numberOfPeople += resultObj.getInt("number_people");
                    percentageOfPeople += resultObj.getDouble("percentage_people");
                    status += resultObj.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int finalNumberOfPeople = numberOfPeople;
                float finalPercentageOfPeople = percentageOfPeople;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        totalCountTv.setText("Number of passengers: " + finalNumberOfPeople);
                        totalPercentageTv.setText("Level of crowdedness: " + String.format("%.2f", finalPercentageOfPeople));
                        if (finalPercentageOfPeople > 0.5) {
                           suggestionTv.setText("Try another carriages!");
                           suggestionTv.setTextColor(Color.RED);
                        }
                        else {
                            suggestionTv.setText("You can stay here.");
                            suggestionTv.setTextColor(Color.GREEN);
                        }
                        totalCountTv.setVisibility(View.VISIBLE);
                        totalPercentageTv.setVisibility(View.VISIBLE);
                        suggestionTv.setVisibility(View.VISIBLE);
//                            Log.i("result", response.body().string());
                    }
                });
            }
        });

//        Log.i("realpath", getRealPath(uri));
    }

    //todo: get real path of a picture
    public String getRealPath(Uri uri) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri,filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    private void initView() {
        photoIv = findViewById(R.id.photo_iv);
        totalCountTv = findViewById(R.id.total_count_tv);
        totalPercentageTv = findViewById(R.id.total_percentage_tv);
        suggestionTv = findViewById(R.id.suggestions_tv);
        backToMainBtn = findViewById(R.id.back_to_menu_btn);
    }
}