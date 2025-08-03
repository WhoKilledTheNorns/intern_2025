package com.chongqing.carinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.Manifest;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;


import org.json.JSONException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;

public class TabDS extends Fragment
{
    private static final String TAG = "TabDS"; // 日志标签
    // UI 控件（Fragment 自己管理自己的按钮、TextView）
    private Button bt_opencloseSpeech;
    private TextView tv_sendspeeddata, getTv_sendspeeddata;

    // 本地状态（Fragment 内部使用的标志）
    private boolean lightflag = true;
    private boolean responseflag = true;
    private String speekstring = "";

    // 录音相关变量（跟 Fragment 的语音功能强绑定）
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private AudioRecord audioRecord;
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final String AUDIO_FILE_PATH =
            Environment.getExternalStorageDirectory() + "/user_audio.pcm";
    private File audioFile;
    private boolean isRecording = false;
    private Thread recordingThread;

    // TTS（文字转语音）相关
    private TextToSpeech textToSpeech;
    private boolean isTtsInitialized = false;
    private boolean isSpeakEnabled = true;
    private float speechRate = 1.0f;
    private float pitch = 1.0f;

    private OkHttpClient client;

    private String APIkey = "sk-8c9b995fe08c4b179591913eab5bc7a7";



    // 录音相关变量
    private static final String AUDIO_FILE_NAME = "user_audio.pcm";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.tab_testds, container, false);

        // 初始化 OkHttpClient
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // 初始化录音文件
        audioFile = new File(requireContext().getFilesDir(), AUDIO_FILE_NAME);

        // 找控件
        bt_opencloseSpeech = view.findViewById(R.id.bt_openclosespeech);
        tv_sendspeeddata = view.findViewById(R.id.textSpeechTO);
        getTv_sendspeeddata = view.findViewById(R.id.textSpeechFrom);
        ToggleButton tb_speak = view.findViewById(R.id.tb_speak);
        Button btnSendText = view.findViewById(R.id.btnSendText); // 新增发送按钮

        // 发送按钮点击事件
        btnSendText.setOnClickListener(v -> {
            String text = tv_sendspeeddata.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                return;
            }
            sendTextToDeepSeek(text); // 调用你现有的方法
        });





        // 语音按钮触摸事件
        bt_opencloseSpeech.setOnTouchListener((v, event) ->
        {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (requireContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                REQUEST_RECORD_AUDIO_PERMISSION);
                    } else {
                        startRecording();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopRecording();
                    processAudio();
                    return true;
            }
            return false;
        });

        return view;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(requireContext(), "需要录音权限才能使用语音功能", Toast.LENGTH_SHORT).show();
            }
        }
    }





    // 开始录音（Fragment版本）
    private void startRecording() {
        isRecording = true;

        // 1. 更改按钮状态（UI操作必须在主线程）
        requireActivity().runOnUiThread(() -> {
            bt_opencloseSpeech.setText("录音中...");
            bt_opencloseSpeech.setBackgroundResource(R.drawable.shape_button_red);
        });

        // 2. 添加振动反馈（Fragment中要用 requireContext()）
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(100); // 振动100毫秒
        }

        // 3. 初始化音频录制
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRecord.startRecording();

            // 4. 开线程写文件
            recordingThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                try (FileOutputStream fileOutputStream = new FileOutputStream(audioFile)) {
                    while (isRecording) {
                        int read = audioRecord.read(buffer, 0, buffer.length);
                        if (read > 0) {
                            fileOutputStream.write(buffer, 0, read);
                        }
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Log.e(TAG, "录音错误: " + e.getMessage());
                }
            });
            recordingThread.start();

        } else {
            Log.e(TAG, "音频录制初始化失败");
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "录音初始化失败", Toast.LENGTH_SHORT).show()
            );
        }
    }

    // 停止录音（Fragment版本）
    private void stopRecording()
    {
        isRecording = false;

        // 按钮样式恢复为就绪状态（绿色）
        bt_opencloseSpeech.setText("按住说话");
        bt_opencloseSpeech.setBackgroundResource(R.drawable.shape_button_green);

        // 停止并释放 AudioRecord
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

        // 等待录音线程结束
        if (recordingThread != null && recordingThread.isAlive()) {
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "等待录音线程结束错误: " + e.getMessage());
            }
        }
    }


    private void processAudio()
    {
        if (!audioFile.exists() || audioFile.length() == 0) {
            Toast.makeText(requireContext(), "录音文件不存在或为空", Toast.LENGTH_SHORT).show();
            return;
        }

        bt_opencloseSpeech.setText("处理中...");
        bt_opencloseSpeech.setEnabled(false);

        new Thread(() -> {
            try {
                // 从 MainActivity 直接获取 token
                String token = ((MainActivity) requireActivity()).getAToken();

                if (token != null && !token.isEmpty()) {
                    final String[] result = recognizeSpeechWithHuawei(token);

                    requireActivity().runOnUiThread(() -> {
                        if (result[0] != null && !result[0].isEmpty()) {
                            tv_sendspeeddata.setText(result[0]);
                            sendTextToDeepSeek(result[0]);
                        } else {
                            Toast.makeText(requireContext(), "语音识别失败", Toast.LENGTH_SHORT).show();
                            resetButtonState();
                        }
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "AToken 无效，请检查网络或重新启动应用", Toast.LENGTH_SHORT).show();
                        resetButtonState();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "处理音频错误: " + e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "处理音频时出错: " + e.toString(), Toast.LENGTH_SHORT).show();
                    resetButtonState();
                });
            }
        }).start();
    }


    private String parseRecognizedText(String jsonData)
    {
        try {
            Log.d(TAG, "开始解析JSON数据: " + jsonData);
            JSONObject response = JSON.parseObject(jsonData);

            if (response == null) {
                Log.e(TAG, "JSON解析结果为空");
                return "";
            }

            if (!response.containsKey("segments")) {
                Log.e(TAG, "JSON数据中缺少segments字段: " + response.toJSONString());
                return "";
            }

            JSONArray segments = response.getJSONArray("segments");

            if (segments == null || segments.size() == 0) {
                Log.e(TAG, "segments数组为空或不存在");
                return "";
            }

            JSONObject segment = segments.getJSONObject(0);

            if (segment == null || !segment.containsKey("result")) {
                Log.e(TAG, "segment中缺少result字段");
                return "";
            }

            JSONObject resultObj = segment.getJSONObject("result");

            if (resultObj == null || !resultObj.containsKey("text")) {
                Log.e(TAG, "result中缺少text字段");
                return "";
            }

            String text = resultObj.getString("text");
            Log.d(TAG, "成功解析出文本: " + text);
            return text;
        } catch (Exception e) {
            Log.e(TAG, "解析语音识别结果出错: " + e.getMessage(), e);
            return "";
        }
    }



    private String[] recognizeSpeechWithHuawei(String token)
    {
        String[] result = new String[2];
        try
        {
            String url = "wss://sis-ext." + "cn-north-4" + ".myhuaweicloud.cn/v1/" +
                    "0c784d91a300f4fa2ff7c01c20885fa6" + "/rasr/short-stream";

            OkHttpClient wsClient = new OkHttpClient();
            final WebSocket ws = wsClient.newWebSocket(
                    new Request.Builder()
                            .url(url)
                            .addHeader("X-Auth-Token", token)
                            .build(),
                    new WebSocketListener() {
                        @Override
                        public void onMessage(WebSocket webSocket, String text)
                        {
                            String recognizedText = parseRecognizedText(text);
                            if (!recognizedText.isEmpty()) {
                                result[0] = recognizedText;
                                try {
                                    DeepSeekAPI deepSeekAPI = new DeepSeekAPI(APIkey);
                                    result[1] = deepSeekAPI.generateResponse(recognizedText);
                                } catch (IOException e) {
                                    Log.e(TAG, "调用 DeepSeek 出错", e);
                                }
                                webSocket.close(1000, "识别完成");
                            }
                        }
                    }
            );

            Thread.sleep(5000);
            ws.close(1000, "完成识别");
        } catch (InterruptedException e) {
            Log.e(TAG, "线程中断", e);
            Thread.currentThread().interrupt(); // 保留中断状态
        }
        return result;
    }


    // 读取音频文件
    private byte[] readAudioFile()
    {
        try
        {
            return java.nio.file.Files.readAllBytes(audioFile.toPath());
        } catch (IOException e) {
            Log.e(TAG, "读取音频文件错误: " + e.getMessage());
            return null;
        }
    }

    private void sendTextToDeepSeek(String text) {
        MediaType mediaType = MediaType.parse("application/json");
        JSONObject payload = new JSONObject();

        // 构造请求 JSON
        payload.put("model", "deepseek-chat");

        JSONArray messages = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一位经验丰富的洁净车间运行专家，对电子元器件生产的环境要求非常熟悉，精通 ISO 14644 等相关标准。\n" +
                "\n" +
                "接下来我会给你一组实时监测数据，包括温度、湿度、PM2.5、悬浮粒子数量、压差、风速等。  \n" +
                "你的任务是像和同事交流一样，先简单描述一下车间目前的状态，再指出哪些参数不在正常范围，并分析可能的原因，最后给出实际可行的调整建议。\n" +
                "\n" +
                "请注意：\n" +
                "1. 说话要像人一样，有逻辑、有层次，但不要用太多专业术语吓人。\n" +
                "2. 如果数据正常，可以简单说“整体运行稳定”，但也可以补充一些小优化建议。\n" +
                "3. 如果有问题，请说明严重程度，比如“这个需要马上处理”或“暂时不影响生产，但建议尽快调整”。\n" +
                "4. 输出最后要用一个简短的总结，比如：“总体来看，目前的情况是……”。t.");
        messages.add(systemMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", text + " 限制字数200字");
        messages.add(userMsg);

        payload.put("messages", messages);

        // OkHttp 3.x 写法
        RequestBody body = RequestBody.create(mediaType, payload.toJSONString());

        Request request = new Request.Builder()
                .url("https://api.deepseek.com/v1/chat/completions")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + APIkey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "DeepSeek API调用失败: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respStr = response.body().string();
                Log.i(TAG, "HTTP Code: " + response.code() + " Body: " + respStr);

                if (!response.isSuccessful()) {
                    return;
                }

                try {
                    JSONObject object = JSONObject.parseObject(respStr);
                    JSONArray choices = object.getJSONArray("choices");
                    JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                    speekstring = message.getString("content");

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            getTv_sendspeeddata.setText(speekstring);
                            speakText(speekstring);
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析错误: " + e.getMessage());
                }
            }
        });
    }




    // 文字转语音
    private void speakText(String text) {
        if (!isSpeakEnabled || !isTtsInitialized || text == null || text.isEmpty()) {
            return;
        }

        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    // 修改语速
    public void setSpeechRate(float rate) {
        speechRate = rate;
        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(speechRate);
        }
    }

    // 修改音调
    public void setPitch(float pitch) {
        this.pitch = pitch;
        if (textToSpeech != null) {
            textToSpeech.setPitch(pitch);
        }
    }

    // 释放 TextToSpeech 资源
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    // 重置按钮状态
    private void resetButtonState() {
        bt_opencloseSpeech.setText("请录音");
        bt_opencloseSpeech.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        bt_opencloseSpeech.setEnabled(true);
    }

    // DeepSeek API调用类（原样保留）
    private class DeepSeekAPI
    {
        private String apiKey;
        private OkHttpClient client;

        public DeepSeekAPI(String apiKey) {
            this.apiKey = apiKey;
            this.client = new OkHttpClient();
        }

        public String generateResponse(String prompt) throws IOException {
            String fullPrompt = prompt + "\n\n请保持回答简洁，不超过200字。";

            JSONObject data = new JSONObject();
            data.put("model", "deepseek-chat");

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", fullPrompt);
            messages.add(userMessage);
            data.put("messages", messages);

            data.put("temperature", 0.7);
            data.put("max_tokens", 200);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, data.toJSONString());

            Request request = new Request.Builder()
                    .url("https://api.deepseek.com/v1/chat/completions")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                JSONObject result = JSON.parseObject(response.body().string());
                if (result.containsKey("choices")) {
                    JSONObject choice = result.getJSONArray("choices").getJSONObject(0);
                    if (choice.containsKey("message") && choice.getJSONObject("message").containsKey("content")) {
                        String answer = choice.getJSONObject("message").getString("content");
                        if (answer.length() > 200) {
                            answer = answer.substring(0, 200) + "...";
                        }
                        return answer;
                    }
                }
            }

            Log.e(TAG, "DeepSeek API响应错误: " + response.code() + " " + response.body().string());
            return "抱歉，未能获取到回答";
        }
    }










}





//// 获取华为云认证Token
//private String getHuaweiAuthToken() throws IOException {
//    String url = "https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens";
//
//    JSONObject auth = new JSONObject();
//    JSONObject identity = new JSONObject();
//    identity.put("methods", new String[]{"password"});
//
//    JSONObject password = new JSONObject();
//    JSONObject user = new JSONObject();
//    JSONObject domain = new JSONObject();
//    domain.put("name", "cgh_date");
//    user.put("domain", domain);
//    user.put("name", "cgh_date2025");
//    user.put("password", "zheng22800988");
//    password.put("user", user);
//
//    identity.put("password", password);
//    auth.put("identity", identity);
//
//    JSONObject scope = new JSONObject();
//    JSONObject project = new JSONObject();
//    project.put("name", huaweiRegion);
//    scope.put("project", project);
//    auth.put("scope", scope);
//
//    JSONObject requestBody = new JSONObject();
//    requestBody.put("auth", auth);
//
//    MediaType mediaType = MediaType.parse("application/json");
//    RequestBody body = RequestBody.create(mediaType, requestBody.toJSONString());
//
//    Request request = new Request.Builder()
//            .url(url)
//            .method("POST", body)
//            .addHeader("Content-Type", "application/json;charset=UTF-8")
//            .build();
//
//    Response response = client.newCall(request).execute();
//    if (response.isSuccessful() && response.headers().get("X-Subject-Token") != null) {
//        return response.headers().get("X-Subject-Token");
//    } else {
//        Log.e(TAG, "获取Token失败: " + response.code() + " " + response.body().string());
//        return null;
//    }
//}


