package com.vital.homecontrol;

//import android.app.AlertDialog;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Period;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatFragment extends AppCompatDialogFragment {

    public static String TAG = "StatFragmentDialog";
    public static final int IS_DS18B20 = 3;
    public static final int IS_SHT21 = 4;
    public static final int IS_BMP180 = 5;

    private final String SNS_TYPE = "SensorType";
    // values for SNS_TYPE ana R.id.sns_typ
    public static final int IS_TEMP = 1;
    public static final int IS_HUM = 2;
    public static final int IS_PRESS = 3;

    private static final double K_MEASURE = 0.131072;
    private static final double K_STAT = 30.015488;


    GraphView graph;
    TextView curTemp;
    TextView valPeriod;
    TextView valCount;
    MainActivity act;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        act = (MainActivity) getActivity();

    }

//передача данных в фрагмент
    //    https://translated.turbopages.org/proxy_u/en-ru.ru.dff7ec67-63939252-1b262eed-74722d776562/https/stackoverflow.com/questions/16036572/how-to-pass-values-between-fragments).

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stat_frame, container, false);

//        Objects.requireNonNull(getDialog()).setTitle("Statistic");

        graph = view.findViewById(R.id.stat_graf);
        ArrayList<Float> data = new ArrayList<>();


        String st = "";
        byte[] stat = {};
        int typ = 0;
        int model = 0;
        double period = 0;
        if (getArguments() != null) {
            st = getArguments().getString("CurData", "");
            stat = getArguments().getByteArray("Data");
            model = getArguments().getInt("Model", 0);
        }
        if (stat != null) {
            int count = stat[7]&0xFF;
            period = ((stat[5]&0xFF)<<8 | (stat[6]&0xFF)) * K_STAT;
            typ = stat[4]&0xFF;
            for (int i = count-1; i >=0 ; i--) {
                int val;
                if (stat[4] == IS_PRESS) {
                    val = ((stat[i * 3 + 8] & 0xFF) << 16 | (stat[i * 3 + 9] & 0xFF) << 8 | stat[i * 2 + 10] & 0xFF);
                } else {
                    val = ((stat[i * 2 + 8] & 0xFF) << 8 | stat[i * 2 + 9] & 0xFF);
                }
                if (val>0){
                    data.add(getFloatSensorValue(val, model, typ));
                }
            }
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd hh:mm", Locale.getDefault());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dfp = new SimpleDateFormat("dd hh:mm");

        DataPoint[] dp = new DataPoint[data.size()];
        for (int i = 0; i <data.size() ; i++) {
            dp[i] = new DataPoint(i, data.get(i));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dp);
        graph.addSeries(series);
        graph.setCursorMode(true);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setMinY(minData(data)-10);
        graph.getViewport().setMaxY(maxData(data)+10);

        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        Date curDate = Calendar.getInstance().getTime();

        double finalPeriod = period;
        double maxX = graph.getViewport().getMaxY(true);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX){
                if (isValueX){
                    Date dt = new Date();
                    dt.setTime((long) (curDate.getTime() - (maxX - value) * finalPeriod *1000));
                    return df.format(dt);
                }else{
                    return super.formatLabel(value, isValueX);
                }
            }
        });


//        Log.i(TAG, String.valueOf(s));

        curTemp = view.findViewById(R.id.id_curtemp);
        curTemp.setText(st);

        valPeriod = view.findViewById(R.id.val_period);

        st = String.valueOf(period) + " sec";
        valPeriod.setText(st);

        valCount = view.findViewById(R.id.val_count);
        valCount.setText(String.valueOf(data.size()));
//        ViewCompat.setBackground( curTemp, ContextCompat.getDrawable(Objects.requireNonNull(getContext()), android.R.drawable.dark_header));



        return view;
    }

    public Float getFloatSensorValue(int inData, int model, int sensType){
        switch (sensType){
            case IS_TEMP:
                switch (model){
                    case IS_DS18B20:
                        return (float) (inData / 16);
                    case IS_SHT21:
                        return (float) (((inData&0xFFFC)*175.72)/0x10000 - 46.85);
                    case IS_BMP180:
                        return (float) (inData/10);
                }
            case IS_HUM:
                if (model == IS_SHT21) {
                    return (float) (((inData & 0xFFFC) * 125) / 0x10000 - 6);
                }
            case IS_PRESS:
                if (model == IS_BMP180) {
                    return (float) (inData / 100);
                }
        }
        return (float) 0;
    }

    private double minData(ArrayList<Float> data){
        double res;
        if (data.size()>0){
            res = data.get(0);
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i)<res){
                    res = data.get(i);
                }
            }
        }else{
            res = 0;
        }
       return res;
    }

    private double maxData(ArrayList<Float> data){
        double res;
        if (data.size()>0){
            res = data.get(0);
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i)>res){
                    res = data.get(i);
                }
            }
        }else{
            res = 0;
        }
        return res;
    }


}
