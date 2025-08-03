
//
//isLightOn isFanOn 标志位

package com.chongqing.carinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import okhttp3.OkHttpClient;

public class TabControlFm extends Fragment {

    private View viewControl; // 保存页面View
    private OkHttpClient client1; // 保存客户端
    private String AToken1;       // 保存Token

    private boolean isLightOn = false;
    private boolean isFanOn = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewControl = inflater.inflate(R.layout.tab_cfm_control, container, false);

        // 绑定控件
        ImageButton btnAngle = viewControl.findViewById(R.id.btnAngle);
        ImageButton btnConc = viewControl.findViewById(R.id.btnConc);

        // 灯泡按钮点击事件
        btnAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLightOn = !isLightOn;
                if (isLightOn)
                {
                    ((MainActivity)getActivity()).controlLight("ON", client1, AToken1);
                    btnAngle.setBackgroundResource(R.drawable.light_on);
                }
                else
                {
                    ((MainActivity)getActivity()).controlLight("OFF", client1, AToken1);
                    btnAngle.setBackgroundResource(R.drawable.light_off);
                }
            }
        });

        // 风扇按钮点击事件
        btnConc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFanOn = !isFanOn;
                if (isFanOn)
                {
                    btnConc.setBackgroundResource(R.drawable.fan_on);
                    ((MainActivity)getActivity()).SetTheFanstatus("ON", client1, AToken1);
                }
                else
                {
                    ((MainActivity)getActivity()).SetTheFanstatus("OFF", client1, AToken1);
                    btnConc.setBackgroundResource(R.drawable.fan_off);
                }
            }
        });

        return viewControl;
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
}
