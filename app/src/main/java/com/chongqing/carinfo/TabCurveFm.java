package com.chongqing.carinfo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;


public class TabCurveFm extends Fragment {

    public View viewcurve; // 保存页面View
    private OkHttpClient client1; // 保存客户端
    private String AToken1;       // 保存Token
    private List<Entry> tempEntries, humiEntries, pressEntries, lightEntries, co2Entries;
    private LineChart chartTemperature, chartHumidity, chartPressure, chartLight, chartCO2;
    private int index = 0;

    // 缓存最近一次数据
    private String[] cachedData = null;
    private boolean chartsReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewcurve = inflater.inflate(R.layout.tab_curve, container, false);
        return viewcurve;
    }


    private void trimToMaxSize(List<Entry> list, int maxSize) {
        while (list.size() > maxSize) {
            list.remove(0); // 移除最早的数据
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化数据列表
        tempEntries = new ArrayList<>();
        humiEntries = new ArrayList<>();
        pressEntries = new ArrayList<>();
        lightEntries = new ArrayList<>();
        co2Entries = new ArrayList<>();

        // 绑定五个 LineChart
        chartTemperature = view.findViewById(R.id.chart_temperature);
        chartHumidity = view.findViewById(R.id.chart_humidity);
        chartPressure = view.findViewById(R.id.chart_pressure);
        chartLight = view.findViewById(R.id.chart_light);
        chartCO2 = view.findViewById(R.id.chart_CO2);

        chartsReady = true;

        // 如果有缓存数据，初始化完成后立刻绘制
        if (cachedData != null) {
            updateAllCurves(cachedData[0], cachedData[1], cachedData[2], cachedData[3], cachedData[4]);
            cachedData = null; // 用完清空
        }
    }

    // 一次更新五个曲线
    public void updateAllCurves(String temp, String humi, String press, String light, String co2) {
        if (!chartsReady) {
            Log.e("TabCurveFm", "Charts not ready, caching data.");
            cachedData = new String[]{temp, humi, press, light, co2};
            return;
        }


        try {
            float t = Float.parseFloat(temp);
            float h = Float.parseFloat(humi);
            float p = Float.parseFloat(press);
            float l = Float.parseFloat(light);
            float c = Float.parseFloat(co2);

            tempEntries.add(new Entry(index, t));
            humiEntries.add(new Entry(index, h));
            pressEntries.add(new Entry(index, p));
            lightEntries.add(new Entry(index, l));
            co2Entries.add(new Entry(index, c));
            index++;

            chartTemperature.setData(new LineData(new LineDataSet(tempEntries, "温度 (°C)")));
            chartHumidity.setData(new LineData(new LineDataSet(humiEntries, "湿度 (%)")));
            chartPressure.setData(new LineData(new LineDataSet(pressEntries, "气压 (hPa)")));
            chartLight.setData(new LineData(new LineDataSet(lightEntries, "光照 (lx)")));
            chartCO2.setData(new LineData(new LineDataSet(co2Entries, "CO₂ (ppm)")));
            // 只保留最近 5 个数据
            trimToMaxSize(tempEntries, 5);
            trimToMaxSize(humiEntries, 5);
            trimToMaxSize(pressEntries, 5);
            trimToMaxSize(lightEntries, 5);
            trimToMaxSize(co2Entries, 5);
            chartTemperature.invalidate();
            chartHumidity.invalidate();
            chartPressure.invalidate();
            chartLight.invalidate();
            chartCO2.invalidate();

        } catch (NumberFormatException e) {
            Log.e("TabCurveFm", "数据格式错误: " + e.getMessage());
        }
    }




    public void SetcurveData(String goodamount, String badamount, OkHttpClient client, String AToken) {

        client1=client;
        AToken1=AToken;

        TextView tvGoodAmount = viewcurve.findViewById(R.id.tvGoodAmount);
        TextView tvBadAmount = viewcurve.findViewById(R.id.tvBadAmount);

        tvGoodAmount.setText(goodamount);
        tvBadAmount.setText(badamount);
    }
}
