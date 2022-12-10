package com.vital.homecontrol;

//import android.app.AlertDialog;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;

import java.util.Objects;

public class StatFragment extends AppCompatDialogFragment {

    public static String TAG = "StatFragmentDialog";

    GraphView graph;
    TextView curTemp;
    MainActivity act;
/*
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();
    }
    */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        act = (MainActivity) getActivity();

    }

//передача данных в фрагмент
    //    https://translated.turbopages.org/proxy_u/en-ru.ru.dff7ec67-63939252-1b262eed-74722d776562/https/stackoverflow.com/questions/16036572/how-to-pass-values-between-fragments).

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stat_layout, container, false);

        getDialog().setTitle("Statistic");

        graph = view.findViewById(R.id.stat_graf);


        String st = null;
        if (getArguments() != null) {
            st = getArguments().getString("CurData");
        }else {
            st = "";
        }
        curTemp = view.findViewById(R.id.id_curtemp);
        curTemp.setText(st);
//        ViewCompat.setBackground( curTemp, ContextCompat.getDrawable(Objects.requireNonNull(getContext()), android.R.drawable.dark_header));
        return view;
    }

    ;
}
