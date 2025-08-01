package com.chongqing.carinfo;
import java.net.InetSocketAddress;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.ArrayList;



import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.data.Entry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class TabVedioFm extends Fragment {
    private TextView tvFps;
    private TextView tvCurrentTime;
    private ImageView imageView;
    private Thread clientThread;
    private volatile boolean running = false;
    private static final String SERVER_IP = "192.168.43.148";
    private static final int SERVER_PORT = 8080;
    private long lastTime = System.currentTimeMillis();
    private int frameCount = 0;
    private float fpsValue = 0f;
    // 固定IP和端口
    private static final String ip = "192.168.43.148";
    private static final String port = "8080";

    // 折线图变量
    private LineChart chart;
    private LineDataSet dataSet;
    private LineData lineData;
    private List<Entry> entries = new ArrayList<>();
    private int chartXIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        new Thread(() ->
        {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 3000);
                Log.i("VideoTest", "连接成功");
                // TODO: 启动视频流
            } catch (IOException e) {
                Log.e("VideoTest", "无法连接到 " + SERVER_IP + ":" + SERVER_PORT, e);
            }
        }).start();


        View view = inflater.inflate(R.layout.tab_vedio, container, false);
        imageView = view.findViewById(R.id.imageView);  //时间显示
        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        tvFps = view.findViewById(R.id.tv_fps);

        // 初始化折线图
        chart = view.findViewById(R.id.chart);
        dataSet = new LineDataSet(entries, "实时数据 (kB)");
        dataSet.setColor(0xFF2196F3); // 蓝色
        dataSet.setValueTextColor(0xFF000000); // 黑色文字
        lineData = new LineData(dataSet);
        chart.setData(lineData);

        // 启动时钟更新
        startClockUpdater();
        running = true;
        clientThread = new Thread(new ClientRunnable());
        clientThread.start();

        return view;
    }


    private void startClockUpdater() {
        new Thread(() ->
        {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            while (running) {
                String now = "时间: " + sdf.format(new Date());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> tvCurrentTime.setText(now));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        running = false;
        if (clientThread != null) {
            clientThread.interrupt();
        }
    }

    private class ClientRunnable implements Runnable {
        private final String SERVER_IP = ip;
        private final int SERVER_PORT = Integer.parseInt(port);

        @Override
        public void run() {
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                 InputStream inputStream = socket.getInputStream()) {

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] tempBuffer = new byte[4096];
                int bytesRead;

                while (running && !Thread.interrupted()) {
                    if ((bytesRead = inputStream.read(tempBuffer)) != -1) {
                        buffer.write(tempBuffer, 0, bytesRead);
                        processBuffer(buffer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void processBuffer(ByteArrayOutputStream buffer) {
            byte[] data = buffer.toByteArray();
            int start = findStartMarker(data, 0);

            while (start != -1) {
                int end = findEndMarker(data, start);
                if (end != -1) {
                    byte[] jpegData = Arrays.copyOfRange(data, start, end + 2);
                    displayImage(jpegData);
                    buffer.reset();
                    try {
                        buffer.write(Arrays.copyOfRange(data, end + 2, data.length));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    data = buffer.toByteArray();
                    start = findStartMarker(data, 0);
                } else {
                    break;
                }
            }
        }


        private void updateChart(float fps) {
            entries.add(new Entry(chartXIndex++, fps));
            dataSet.notifyDataSetChanged();
            lineData.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate(); // 刷新图表
        }


        private int findStartMarker(byte[] data, int offset) {
            for (int i = offset; i < data.length - 1; i++) {
                if ((data[i] & 0xFF) == 0xFF && (data[i + 1] & 0xFF) == 0xD8) {
                    return i;
                }
            }
            return -1;
        }


        private int findEndMarker(byte[] data, int start) {
            for (int i = start; i < data.length - 1; i++) {
                if ((data[i] & 0xFF) == 0xFF && (data[i + 1] & 0xFF) == 0xD9) {
                    return i;
                }
            }
            return -1;
        }

        private void displayImage(final byte[] jpegData) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() ->
            {
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
//                // 计算当前帧大小（kB）
//                float frameSizeKB = jpegData.length / 1024f;
//
//                // 更新折线图
//                entries.add(new Entry(chartXIndex++, frameSizeKB));
//                dataSet.notifyDataSetChanged();
//                lineData.notifyDataChanged();
//                chart.notifyDataSetChanged();
//                chart.invalidate();
                // 统计帧率
                frameCount++;
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTime >= 1000) { // 每 1 秒计算一次
                    fpsValue = frameCount * 1000f / (currentTime - lastTime);
                    tvFps.setText(String.format(Locale.getDefault(), "当前帧率: %.2f FPS", fpsValue));
                    updateChart(fpsValue); // 折线图更新
                    frameCount = 0;
                    lastTime = currentTime;
                }
            });
        }
    }
}