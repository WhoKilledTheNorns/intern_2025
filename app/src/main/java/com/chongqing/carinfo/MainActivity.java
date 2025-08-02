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



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;


public class MainActivity<usbIoManager> extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private OkHttpClient client;
    private String AToken;
    private TabCurveFm tabCFM_curve; // 从Fragment改到了方法

    private TabBFm tabCFM_b;
    private TabControlFm tab_cfm_control; // 改成具体的 Fragment 类型
    private Fragment tabCFM_vedio; // 新增视频监控的 fragment
    private Fragment currentFragment;


    private ImageButton btnTemp, btnVideo, btnExit, btnCurve, btnControl;


    private ImageView imageView;
    private Thread clientThread;

    // 直接写死 IP 和端口
    private static final String ip = "192.68.43.148";
    private static final String port = "8080";

    private volatile boolean running = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Vibrator vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);

        // 初始化 Fragment

        tabCFM_b = new TabBFm();
        tabCFM_vedio = new TabVedioFm(); // 新增
        tabCFM_curve = new TabCurveFm(); // 新增
        tab_cfm_control = new TabControlFm();

        // 绑定按钮
        btnTemp = findViewById(R.id.btn_temp);
        btnVideo = findViewById(R.id.btn_video);
        btnExit = findViewById(R.id.btn_exit);
        btnCurve = findViewById(R.id.btn_curve);
        btnControl = findViewById(R.id.btn_control);

        // 默认显示温湿度页面
        switchFragment(tabCFM_b);

        // 按钮点击事件
        btnTemp.setOnClickListener(v -> switchFragment(tabCFM_b));
        btnVideo.setOnClickListener(v -> switchFragment(tabCFM_vedio));
        btnCurve.setOnClickListener(v -> switchFragment(tabCFM_curve)); // 新增
        btnControl.setOnClickListener(v -> switchFragment(tab_cfm_control)); // 新增
        btnExit.setOnClickListener(v -> finish());

        // 初始化 OkHttp
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();



        // 认证请求
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "{ \n" +
                "    \"auth\": { \n" +
                "        \"identity\": { \n" +
                "            \"methods\": [ \n" +
                "                \"password\" \n" +
                "            ], \n" +
                "            \"password\": { \n" +
                "                \"user\": { \n" +
                "                    \"name\": \"cekong2022\", \n" +
                "                    \"password\": \"qq3416702178\", \n" +
                "                    \"domain\": { \n" +
                "                        \"name\": \"yangjl_neu\" \n" +
                "                    } \n" +
                "                } \n" +
                "            } \n" +
                "        }, \n" +
                "        \"scope\": { \n" +
                "            \"project\": { \n" +
                "                \"name\": \"cn-north-4\" \n" +
                "            } \n" +
                "        } \n" +
                "    } \n" +
                "}");

        Request request = new Request.Builder()
                .url("https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens")
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
                    Log.d(TAG, "响应状态码: " + response.code());
                    Log.d(TAG, "响应头部内容:\n" + response.headers().toString());
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
                .url("https://d75ff9379a.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/ba12250a69f543479841481557c1e554/devices/6886e669d582f200183fdcc5_smartcontrol/shadow")
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
                String gamount = "0";
                String bamount = "0";
                String coco = "0";
                String light = "0";
                String angle = "0";
                String conc = "0";

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
                                                if (propertiesObj.get("GAm") != null) {
                                                    gamount = propertiesObj.get("GAm").toString();
                                                }
                                                if (propertiesObj.get("BAm") != null) {
                                                    bamount = propertiesObj.get("BAm").toString();
                                                }
                                                if (propertiesObj.get("Coco") != null) {
                                                    coco = propertiesObj.get("Coco").toString();
                                                }
                                                if (propertiesObj.get("Deng") != null) {
                                                    light = propertiesObj.get("Deng").toString();
                                                }
                                                if (propertiesObj.get("Angle") != null) {
                                                    angle = propertiesObj.get("Angle").toString();
                                                }
                                                if (propertiesObj.get("Conc") != null) {
                                                    conc = propertiesObj.get("Conc").toString();
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
                final String finalgoodAmount = gamount;
                final String finalbadAmount = bamount;
                final String finalCoco = coco;
                final String finalLight = light;
                final String finalangle = angle;
                final String finalconc = conc;
                final String finalggoodAmount = gamount;
                final String finalbbadAmount = bamount;

                runOnUiThread(() -> {
                    if (currentFragment == tabCFM_b)
                    {
                        tabCFM_b.SetTheData(finalTemperature, finalHumidity, finalLumity,finalgoodAmount,finalbadAmount,finalCoco,
                                finalPressure,finalFan_state,finalLight,client, AToken);

                    }
                    else if (currentFragment == tabCFM_curve)
                            {
                                tabCFM_curve.updateAllCurves(finalTemperature, finalHumidity, finalPressure, finalLumity, finalCoco);
                               tabCFM_curve.SetcurveData(finalggoodAmount,finalbbadAmount,client, AToken);
                            }


                    else if (currentFragment == tab_cfm_control)
                    {
                        //灯和风扇控制函数
                        tab_cfm_control.controlAll(finalangle, finalconc, client, AToken);
/*                        tabCFM_b.SetTheLightstatus(finalLight, client, AToken);
                        tabCFM_control.SetTheFanstatus(finalFan_state, client, AToken);*/
                    }
                });
            }
        });
    }
}
