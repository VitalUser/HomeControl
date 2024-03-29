package com.vital.homecontrol;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class StatActivity extends AppCompatActivity {

    private static final String TAG = "MyclassStat";

    private final int M_SETMAX      = 1;
    private final int M_SETMIN     = 2;
    private final int M_     = 3;

    private static final int IS_TEMP = 1;
    private static final int IS_HUM = 2;
    private static final int IS_PRESS = 3;

    static final String MSG_RCV = "MSG_received";

    static final int SET_W_COMMAND	        =  0x05;
    static final int CMD_SET_STAT_PERIOD    =  0xE3;
    static final int CMD_MSG_STAT_PERIOD    =  0xC5;
    static final int MSG_RE_SENT_W          =  0x11;
    static final int MSG_RCV_OK             =  0xA5;

    static final int NO_CONFIRM             =  0xFF;
    static final double KTM                 = 0.131072;
    static final double minStatPeriod       = 30.015488;


    GraphView graph;
    SharedPreferences prefs;
    UDPserver sUDP;
    TextView valPeriod;
    private String deviceIP;
    private int devPort;
    private int localPort;
    private int sensorTyp;

    private double minTemp;
    private double maxTemp;
    private double minHum;
    private double maxHum;
    private double minPress;
    private double maxPress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);


        switch (prefs.getString("key_theme", "")){
            case "Dark":
                setTheme(R.style.AppThemeDark);
                break;
            case "Light":
                setTheme(R.style.AppTheme);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.stat_layout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        minTemp = prefs.getFloat("MinYTemp", -50);
        maxTemp = prefs.getFloat("MaxYTemp", 50);
        minHum = prefs.getFloat("MinYHum", 0);
        maxHum = prefs.getFloat("MaxYHum", 100);
        minPress = prefs.getFloat("MinYPress", 0);
        maxPress = prefs.getFloat("MaxYPress", 1500);


// http://www.android-graphview.org
// https://github.com/jjoe64/GraphView-Demos/tree/master/app/src/main/java/com/jjoe64/graphview_demos/examples
        graph = (GraphView)findViewById(R.id.stat_graf);

        ArrayList<Float> inbuf = (ArrayList<Float>) getIntent().getSerializableExtra("statBuff");
        sensorTyp = getIntent().getIntExtra("snsType", 0);
        final int snsNum = getIntent().getIntExtra("snsNum", 0);
        final int period = getIntent().getIntExtra("period", 0);
        deviceIP = getIntent().getStringExtra("deviceIP");
        devPort = getIntent().getIntExtra("devPort", 0);
        localPort = getIntent().getIntExtra("localPort", 0);
        String snsTyp = getIntent().getStringExtra("measureTyp");
        String snsText = getIntent().getStringExtra("snsText");
        setTitle(snsTyp + " "+snsText);

        valPeriod = (TextView)findViewById(R.id.val_period);
        String stp = DateUtils.formatElapsedTime((long) (period*minStatPeriod));
        valPeriod.setText(stp);
        valPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder dlg = new AlertDialog.Builder(StatActivity.this);
                View seek = getLayoutInflater().inflate(R.layout.seek_bar, null);
                final TextView seekProgres = seek.findViewById(R.id.seek_progress);
                seekProgres.setText(DateUtils.formatElapsedTime((long) (period*minStatPeriod)));
                final SeekBar newPeriod = seek.findViewById(R.id.seek_bar);

                dlg.setTitle(R.string.period);

                newPeriod.setMax(0xB40);        // 24Hour;
                newPeriod.setProgress((int) (period*minStatPeriod));
                newPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        seekProgres.setText(DateUtils.formatElapsedTime((long) (seekBar.getProgress()*minStatPeriod)));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
//                newPeriod.setText(txt);
//                dlg.setView(newPeriod);
                dlg.setView(seek);

                dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        int val = Integer.parseInt(newPeriod.getText().toString()) ;
                        int val = newPeriod.getProgress();
                        byte[] bufCount = {SET_W_COMMAND, (byte) snsNum, 4, (byte) CMD_SET_STAT_PERIOD, (byte) (val>>8), (byte) (val&0xFF)};
                        if (askUDP(bufCount, MSG_RE_SENT_W, CMD_MSG_STAT_PERIOD)){
                            valPeriod.setText(DateUtils.formatElapsedTime((long) (val*minStatPeriod)));
                        }

                    }
                });


                dlg.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dlg.show();

            }
        });

        DataPoint[] data = new DataPoint[inbuf.size()];
        for (int i = 0; i <inbuf.size() ; i++) {
            data[i] = new DataPoint(i, inbuf.get(i));
        }




        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
        graph.addSeries(series);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setScalableY(true);
        switch (sensorTyp){
            case IS_TEMP:
                graph.setTitle(getString(R.string.temperature));
                graph.getViewport().setMinY(minTemp);
                graph.getViewport().setMaxY(maxTemp);
                break;
            case IS_HUM:
                graph.setTitle(getString(R.string.humidity));
                graph.getViewport().setMinY(minHum);
                graph.getViewport().setMaxY(maxHum);
                break;
            case IS_PRESS:
                graph.setTitle(getString(R.string.pressure));
                graph.getViewport().setMinY(minPress);
                graph.getViewport().setMaxY(maxPress);
                break;
            default:
                graph.setTitle("???");
        }

//        graph.getGridLabelRenderer().setVerticalAxisTitle("t, C");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, M_SETMIN, 1, getString(R.string.min_graph));
        menu.add(0, M_SETMAX, 1, getString(R.string.max_graph));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;

            case M_SETMIN:
                AlertDialog.Builder dlg = new AlertDialog.Builder(StatActivity.this);
                dlg.setTitle(R.string.min_graph);
                final EditText setMinEdit = new EditText(StatActivity.this);
                setMinEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
                String txt = String.valueOf(graph.getViewport().getMinY(false));
                setMinEdit.setText(txt);
                dlg.setView(setMinEdit);

                dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        double resValue = Double.parseDouble(setMinEdit.getText().toString());
                        if (resValue<graph.getViewport().getMaxY(false)){
                            graph.getViewport().setMinY(resValue);
                            graph.getViewport().scrollToEnd();
                            switch (sensorTyp){
                                case IS_TEMP:
                                    prefs.edit().putFloat("MinYTemp", (float) resValue).apply();
                                    break;
                                case IS_HUM:
                                    prefs.edit().putFloat("MinYHum", (float) resValue).apply();
                                    break;
                                case IS_PRESS:
                                    prefs.edit().putFloat("MinYPress", (float) resValue).apply();
                                    break;
                            }
                        }
                    }
                });
                dlg.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                dlg.show();
                return true;
            case M_SETMAX:
                dlg = new AlertDialog.Builder(StatActivity.this);
                dlg.setTitle(R.string.max_graph);
                final EditText setMaxEdit = new EditText(StatActivity.this);
                setMaxEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
                txt = String.valueOf(graph.getViewport().getMaxY(false));
                setMaxEdit.setText(txt);
                dlg.setView(setMaxEdit);

                dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        double resValue = Double.parseDouble(setMaxEdit.getText().toString());
                        if (resValue<graph.getViewport().getMaxY(false)){
                            graph.getViewport().setMaxY(resValue);
                            graph.getViewport().scrollToEnd();
                            switch (sensorTyp){
                                case IS_TEMP:
                                    prefs.edit().putFloat("MaxYTemp", (float) resValue).apply();
                                    break;
                                case IS_HUM:
                                    prefs.edit().putFloat("MaxYHum", (float) resValue).apply();
                                    break;
                                case IS_PRESS:
                                    prefs.edit().putFloat("MaxYPress", (float) resValue).apply();
                                    break;
                            }
                        }

                    }
                });
                dlg.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                dlg.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean askUDP(byte[] inBuf, int hostCmd, int devCmd) {
        if (sUDP == null) {
            int pass = Integer.parseInt(prefs.getString("key_udppass", "0"));
            sUDP = new UDPserver(this, deviceIP, devPort, localPort, pass);
            Log.i(TAG, " askUDP, new sUDP" );
            sUDP.start();
        }
        int curID = sUDP.getCurrentID();
        curID++;
        sUDP.setCurrentID((byte) curID);
        int att;
        for (int i = 1; i <4 ; i++) {
            if (hostCmd==MSG_RCV_OK){
                att=i;
            }else {
                att=NO_CONFIRM;
            }
            sUDP.send(inBuf, inBuf.length, (byte) att, hostCmd, devCmd);
            if (waitForConfirm()){
                Log.i(TAG, " askUDP : confirm "+Integer.toHexString(hostCmd)+ " :" + i);
                return true;
            }
        }
//        sendToast(getString(R.string.no_answer));
//        sendStatusText("No answer to " + byteArrayToHex(inBuf, inBuf.length));
        Log.i(TAG, "No answer to "+MainActivity.byteArrayToHex(inBuf, inBuf.length)+", hostCmd = "+Integer.toHexString(sUDP.hostCmd)+", devCmd = "+Integer.toHexString(sUDP.devCmd));
        return false;
    }

    public boolean waitForConfirm(){
        int att = 0;
        while ((!sUDP.getConfirm())&(att<100)){
            try {
                TimeUnit.MILLISECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            att++;
        }
        Log.i(TAG, " waitForConfirm, time = " + att*2 + "mS");
        return (att<100);
    }


}
