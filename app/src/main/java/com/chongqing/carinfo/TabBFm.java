package com.chongqing.carinfo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabBFm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabBFm extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public View viewb;
    ToggleButton control;

    private static final String TAG = "MainActivity";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    MediaType mediaType = MediaType.parse("application/json");//修改成这句话，在标准版里面 cgh2024/3/19
    RequestBody body;
    OkHttpClient client1;
    String AToken1;


    private int msg_presh = 0;

    public TabBFm() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabAFm.
     */
    // TODO: Rename and change types and number of parameters
    public static TabBFm newInstance(String param1, String param2) {
        TabBFm fragment = new TabBFm();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewb = inflater.inflate(R.layout.tab_b, container, false);
        return viewb;
    }


    public void SetTheLightstatus(String lightSt, OkHttpClient client, String AToken) {

        ImageView lightold = viewb.findViewById(R.id.light);//句柄，图片


        control = viewb.findViewById(R.id.control_d);
        control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean a) {


                if (buttonView.isPressed()) {
                    if (!buttonView.isChecked()) {
                        //下发开灯
                        body = RequestBody.create(mediaType, "{\n" +
                                " \"service_id\": \"SmartCockpit\",\n" +
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
                                " \"service_id\": \"SmartCockpit\",\n" +
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
                lightold.setImageResource(R.drawable.light_off);
                control.setChecked(true);
            } else {
                lightold.setImageResource(R.drawable.light_on);
                control.setChecked(false);
            }
        } else
            msg_presh = 0;


    }


    public void sendMessage(OkHttpClient client, String AToken, RequestBody body) {
        Request request = new Request.Builder()
                .url("https://5b0d2d88ef.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/bd212c4e68bb42e59dbe9d772e356d8f/devices/674fb735ef99673c8ad24058_SmtHMI/commands")
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