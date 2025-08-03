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



    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    OkHttpClient client1;
    String AToken1;

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


    public void SetTheData(String temp, String humi,String lumi,String goodamount,String badamount, String ccoo,String press,String fengshan,String dengdeng,OkHttpClient client, String AToken) {

        client1=client;
        AToken1=AToken;
        TextView tvTemp = viewb.findViewById(R.id.tvTemp);
        TextView tvHumi = viewb.findViewById(R.id.tvHumi);
        TextView tvLumi = viewb.findViewById(R.id.tvLumi);
        TextView tvGoodAmount = viewb.findViewById(R.id.tvGoodAmount);
        TextView tvBadAmount = viewb.findViewById(R.id.tvBadAmount);
        TextView tvCoco = viewb.findViewById(R.id.tvCoco);
        TextView tvPres = viewb.findViewById(R.id.tvPres);
        TextView tvFanfan = viewb.findViewById(R.id.tvFanfan);
        TextView tvDengdeng = viewb.findViewById(R.id.tvDengdeng);

        tvTemp.setText(temp);
        tvHumi.setText(humi);
        tvLumi.setText(lumi);
        tvGoodAmount.setText(goodamount);
        tvBadAmount.setText(badamount);
        tvCoco.setText(ccoo);
        tvPres.setText(press);
        tvFanfan.setText(fengshan);
        tvDengdeng.setText(dengdeng);
    }

    public void SetTheFanstatus(String fanSt, OkHttpClient client, String aToken) {
    }
}