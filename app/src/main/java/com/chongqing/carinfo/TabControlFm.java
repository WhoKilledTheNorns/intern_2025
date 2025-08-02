package com.chongqing.carinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import okhttp3.OkHttpClient;

public class TabControlFm extends Fragment
{

    private View viewControl; // 保存页面View
    private OkHttpClient client1; // 保存客户端
    private String AToken1;       // 保存Token

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewControl = inflater.inflate(R.layout.tab_cfm_control, container, false);
        return viewControl;
    }

    /**
     * 控制页面的统一更新函数
     * @param angle     角度
     * @param conc      浓度
     * @param client    OkHttpClient 对象
     * @param AToken    鉴权Token
     */
    public void controlAll(String angle, String conc, OkHttpClient client, String AToken)
    {

        // 保存网络对象和Token，方便后续调用接口
        client1 = client;
        AToken1 = AToken;

        // 绑定控件（你需要在 fragment_tab_cfm_control.xml 里添加这两个TextView）
        TextView tvAngle = viewControl.findViewById(R.id.tvAngle);
        TextView tvConc = viewControl.findViewById(R.id.tvConc);

        // 更新数据
        tvAngle.setText(angle);
        tvConc.setText(conc);
    }
}
