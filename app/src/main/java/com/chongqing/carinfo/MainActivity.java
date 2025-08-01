package com.chongqing.carinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageButton;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity<usbIoManager> extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private OkHttpClient client;
    private String AToken;

    private TabAFm tabCFM_a;
    private TabBFm tabCFM_b;
    private Fragment tabCFM_vedio; // 新增视频监控的 fragment
    private Fragment currentFragment;

    private ImageButton btnTemp, btnVideo, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Vibrator vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);

        // 初始化 Fragment
        tabCFM_a = new TabAFm();
        tabCFM_b = new TabBFm();
        tabCFM_vedio = new TabVedioFm(); // 新增

        // 绑定按钮
        btnTemp = findViewById(R.id.btn_temp);
        btnVideo = findViewById(R.id.btn_video);
        btnExit = findViewById(R.id.btn_exit);

        // 默认显示温湿度页面
        switchFragment(tabCFM_vedio);

        // 按钮点击事件
        btnTemp.setOnClickListener(v -> switchFragment(tabCFM_b));
        btnVideo.setOnClickListener(v -> switchFragment(tabCFM_vedio));
        btnExit.setOnClickListener(v -> finish());

        // 初始化 OkHttp
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // 认证请求
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "{ ...你的 JSON 请求体... }");

        Request request = new Request.Builder()
                .url("https://4c19e048b3.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/9b0cd9d943cd4689b67d2563fdee15b7/products")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Token 获取失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                AToken = response.header("X-Subject-Token");

                if (AToken == null || AToken.isEmpty()) {
                    Log.e(TAG, "未获取到 X-Subject-Token，终止数据轮询");
                    return; // 防止崩溃
                }

                Log.d(TAG, "AToken: " + AToken);

                // 定时轮询
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        fetchDeviceData();
                    }
                }, 0, 1000);
            }
        });
    }

    private void switchFragment(Fragment fragment) {
        if (currentFragment != fragment) {
            currentFragment = fragment;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.tab_content, fragment)
                    .commit();
        }
    }

    private void fetchDeviceData() {
        if (AToken == null || AToken.isEmpty()) {
            Log.e(TAG, "AToken 为空，停止请求数据");
            return;
        }

        Request request = new Request.Builder()
                .url("https://4c19e048b3.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/9b0cd9d943cd4689b67d2563fdee15b7/products")
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Auth-Token", AToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "getLuminace Failure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String temperatue = "0";
                String humity = "0";
                String lumity = "0";
                String pressure = "0";
                String fan_state = "0";
                String amount = "0";
                String coco = "0";
                String light = "0";

                try {
                    if (response.body() != null) {
                        String bodyStr = response.body().string();
                        Log.d(TAG, "响应数据: " + bodyStr);

                        com.alibaba.fastjson.JSONObject object = JSON.parseObject(bodyStr);
                        if (object != null && object.containsKey("shadow")) {
                            com.alibaba.fastjson.JSONArray shadows = object.getJSONArray("shadow");

                            if (shadows != null) {
                                for (int i = 0; i < shadows.size(); i++) {
                                    com.alibaba.fastjson.JSONObject service = shadows.getJSONObject(i);
                                    if (service != null && service.containsKey("reported")) {
                                        com.alibaba.fastjson.JSONObject reportObj = service.getJSONObject("reported");

                                        if (reportObj != null && reportObj.containsKey("properties")) {
                                            com.alibaba.fastjson.JSONObject propertiesObj = reportObj.getJSONObject("properties");
                                //下面绿色的是华为云里的变量，好像是
                                            if (propertiesObj != null) {
                                                if (propertiesObj.get("Temp") != null) {
                                                    temperatue = propertiesObj.get("Temp").toString();
                                                }
                                                if (propertiesObj.get("Humi") != null) {
                                                    humity = propertiesObj.get("Humi").toString();
                                                }
                                                if (propertiesObj.get("Lumi") != null) {
                                                    lumity = propertiesObj.get("Lumi").toString();
                                                }
                                                if (propertiesObj.get("Pres") != null) {
                                                    pressure = propertiesObj.get("Pres").toString();
                                                }
                                                if (propertiesObj.get("Fan") != null) {
                                                    fan_state = propertiesObj.get("Fan").toString();
                                                }
                                                if (propertiesObj.get("Amount") != null) {
                                                    amount = propertiesObj.get("Amount").toString();
                                                }
                                                if (propertiesObj.get("Coco") != null) {
                                                    coco = propertiesObj.get("Coco").toString();
                                                }
                                                if (propertiesObj.get("Light") != null) {
                                                    light = propertiesObj.get("Light").toString();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析JSON异常: " + e.getMessage());
                } finally {
                    response.close();
                }

                final String finalTemperature = temperatue;
                final String finalHumidity = humity;
                final String finalLumity = lumity;
                final String finalPressure = pressure;
                final String finalFan_state = fan_state;
                final String finalAmount = amount;
                final String finalCoco = coco;
                final String finalLight = light;

                runOnUiThread(() -> {
                    if (currentFragment == tabCFM_b) {
                        tabCFM_a.SetTheTempandHumi(finalTemperature, finalHumidity, finalLumity,finalAmount,finalCoco,
                                finalPressure,finalFan_state,finalLight,client, AToken);

                    } else if (currentFragment == tabCFM_vedio) {
                        //灯和风扇控制函数
                        tabCFM_b.SetTheLightstatus(finalLight, client, AToken);
                        tabCFM_b.SetTheFanstatus(finalFan_state, client, AToken);
                    }
                });
            }
        });
    }
}
