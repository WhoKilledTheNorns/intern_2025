package com.chongqing.carinfo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabAFm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabAFm extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public View viewa;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    RequestBody body;
    OkHttpClient client1;
    String AToken1;

    public TabAFm() {
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
    public static TabAFm newInstance(String param1, String param2) {
        TabAFm fragment = new TabAFm();
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
        viewa = inflater.inflate(R.layout.tab_a, container, false);
//        final GradientDrawable gradientDrawable = new GradientDrawable(
//                GradientDrawable.Orientation.TOP_BOTTOM,
//                new int[]{Color.parseColor("#372b9b"), Color.TRANSPARENT});
//        viewa.findViewById(R.id.TempHumDash).setBackground(gradientDrawable);
        return viewa;
    }

    public void SetTheTempandHumi(String temp, String humi,String lumi, OkHttpClient client, String AToken) {

        client1=client;
        AToken1=AToken;

        TextView tvTemp = viewa.findViewById(R.id.tvTemp);
        TextView tvHumi = viewa.findViewById(R.id.tvHumi);
        TextView tvLumi = viewa.findViewById(R.id.tvLumi);
        tvTemp.setText(temp);
        tvHumi.setText(humi);
        tvLumi.setText(lumi);

    }
}