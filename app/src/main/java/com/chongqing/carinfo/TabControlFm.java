
//
//isLightOn isFanOn 标志位

package com.chongqing.carinfo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TabControlFm extends Fragment {

    private View viewControl; // 保存页面View
    private OkHttpClient client1; // 保存客户端
    private String AToken1;       // 保存Token

    private boolean isLightOn = false;
    private boolean isFanOn = false;
    private boolean autoMannual = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        viewControl = inflater.inflate(R.layout.tab_cfm_control, container, false);

        // 绑定控件
        ToggleButton control_d = viewControl.findViewById(R.id.control_d);
        ToggleButton control_f = viewControl.findViewById(R.id.control_f);
        ImageButton btnBigImage = viewControl.findViewById(R.id.btnBigImage);

        // 灯泡按钮点击事件
        control_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!autoMannual) {
                    isLightOn = !isLightOn;
                    if (isLightOn) {
                        ((MainActivity)getActivity()).controlLight("ON", client1, AToken1);
                        control_d.setBackgroundResource(R.drawable.light_on);
                        control_d.setChecked(true);
                    } else {
                        ((MainActivity)getActivity()).controlLight("OFF", client1, AToken1);
                        control_d.setBackgroundResource(R.drawable.light_off);
                        control_d.setChecked(false);
                    }
                } else {
                    // 阻止切换状态：恢复按钮状态为当前 isLightOn 状态
                    control_d.setChecked(isLightOn);  // true or false
//                    control_d.setBackgroundResource(android.R.color.transparent); // 不显示图片

                }
            }
        });


        // 风扇按钮点击事件
        control_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!autoMannual) {
                    isFanOn = !isFanOn;
                    if (isFanOn) {
                        ((MainActivity)getActivity()).SetTheFanstatus("ON", client1, AToken1);
                        control_f.setBackgroundResource(R.drawable.fan_on);
                        control_f.setChecked(true);
                    } else {
                        ((MainActivity)getActivity()).SetTheFanstatus("OFF", client1, AToken1);
                        control_f.setBackgroundResource(R.drawable.fan_off);
                        control_f.setChecked(false);
                    }
                }else {
                    // 阻止切换状态：恢复按钮状态为当前 isLightOn 状态
                    control_f.setChecked(isFanOn);  // true or false
//                    control_f.setBackgroundResource(android.R.color.transparent); // 不显示图片
                }
            }
        });


        // 自动/手动档位切换
        btnBigImage.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v) {
                autoMannual = !autoMannual;  // 切换状态
                if (autoMannual) {
                    btnBigImage.setImageResource(R.drawable.mode_auto);

                    // 自动模式下，不允许手动控制 isFanOn 和 isLightOn
                    // 这里可以选择是否复位状态
                    Log.d("状态", "已进入自动模式，禁止控制 isFanOn 和 isLightOn");
                } else {
                    btnBigImage.setImageResource(R.drawable.mode_man);

                    // 在手动模式下允许控制
                    Log.d("状态", "已进入手动模式，可以控制 isFanOn 和 isLightOn");
                }
            }
        });

        return viewControl;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) getActivity();
            isLightOn = main.getLightStatus();
            isFanOn = main.getFanStatus();

            ToggleButton control_d = viewControl.findViewById(R.id.control_d);
            ToggleButton control_f = viewControl.findViewById(R.id.control_f);

            control_d.setBackgroundResource(isLightOn ? R.drawable.light_on : R.drawable.light_off);
            control_f.setBackgroundResource(isFanOn ? R.drawable.fan_on : R.drawable.fan_off);
        }
    }

    /**
     * 控制页面的统一更新函数
     */
    public void controlAll(String angle, String conc, OkHttpClient client, String AToken) {
        client1 = client;
        AToken1 = AToken;

        TextView tvAngle = viewControl.findViewById(R.id.tvAngle);
        TextView tvConc = viewControl.findViewById(R.id.tvConc);

        tvAngle.setText(angle);
        tvConc.setText(conc);
    }
    /*灯状态控制*/
    ToggleButton control;
    RequestBody body;
    MediaType mediaType = MediaType.parse("application/json");//修改成这句话，在标准版里面 cgh2024/3/19
    private static final String TAG = "MainActivity";

    private int msg_presh = 0;
    public void SetTheLightstatus(String lightSt, OkHttpClient client, String AToken) {

//        ImageView lightold = viewb.findViewById(R.id.light);//句柄，图片

        control = viewControl.findViewById(R.id.control_d);
        control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean a) {


                if (buttonView.isPressed()) {
                    if (!buttonView.isChecked()) {
                        //下发开灯
                        body = RequestBody.create(mediaType, "{\n" +
                                " \"service_id\": \"smartcontrol\",\n" +
                                " \"command_name\": \"Light\",\n" +
                                " \"paras\": {\n" +
                                "  \"Light\": \"ON\"\n" +
                                " }\n" +
                                "}");
                        msg_presh = 1;
                        sendMessage(client,AToken,body);

                    } else {
                        //下发关灯

                        body = RequestBody.create(mediaType, "{\n" +
                                " \"service_id\": \"smartcontrol\",\n" +
                                " \"command_name\": \"Light\",\n" +
                                " \"paras\": {\n" +
                                "  \"Light\": \"OFF\"\n" +
                                " }\n" +
                                "}");
                        msg_presh = 1;
                        sendMessage(client,AToken,body);
                    }

                }
            }
        });



        if (msg_presh != 1) {//msg_presh 是防止文字乱变 图片更换
            if (lightSt.compareTo("OFF") == 0) {
//                lightold.setImageResource(R.drawable.light_off);
                control.setChecked(true);
            } else {
//                lightold.setImageResource(R.drawable.light_on);
                control.setChecked(false);
            }
        } else
            msg_presh = 0;


    }

    /*风扇状态控制*/
    public void SetTheFanstatus(String FanSt, OkHttpClient client, String AToken) {

//        ImageView fantold = viewb.findViewById(R.id.fan);//句柄，图片

        control = viewControl.findViewById(R.id.control_f);
        control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean a) {


                if (buttonView.isPressed()) {
                    if (!buttonView.isChecked()) {
                        //下发开风扇
                        body = RequestBody.create(mediaType, "{\n" +
                                " \"service_id\": \"smartcontrol\",\n" +
                                " \"command_name\": \"Light\",\n" +
                                " \"paras\": {\n" +
                                "  \"Fan\": \"ON\"\n" +
                                " }\n" +
                                "}");
                        msg_presh = 1;
                        sendMessage(client,AToken,body);

                    } else {
                        //下发关风扇

                        body = RequestBody.create(mediaType, "{\n" +
                                " \"service_id\": \"smartcontrol\",\n" +
                                " \"command_name\": \"Light\",\n" +
                                " \"paras\": {\n" +
                                "  \"Fan\": \"OFF\"\n" +
                                " }\n" +
                                "}");
                        msg_presh = 1;
                        sendMessage(client,AToken,body);
                    }

                }
            }
        });

        if (msg_presh != 1) {//msg_presh 是防止文字乱变 图片更换
            if (FanSt.compareTo("OFF") == 0) {

                control.setChecked(true);
            } else {

                control.setChecked(false);
            }
        } else
            msg_presh = 0;


    }

    public void sendMessage(OkHttpClient client, String AToken, RequestBody body) {
        Request request = new Request.Builder()
                .url("https://d75ff9379a.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/ba12250a69f543479841481557c1e554/devices/6886e669d582f200183fdcc5_smartcontrol/commands")
                .method("POST", body)
                .addHeader("X-Auth-Token", AToken)
                .addHeader("Content-Type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, response.body().string());
            }
        });


    }
}
