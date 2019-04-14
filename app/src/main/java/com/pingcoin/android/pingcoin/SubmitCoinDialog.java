package com.pingcoin.android.pingcoin;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

public class SubmitCoinDialog extends DialogFragment {

    public static final String TAG = "example dialog";

    private Toolbar toolbar;


    public static SubmitCoinDialog display(FragmentManager fragmentManager) {
        SubmitCoinDialog submitCoinDialog = new SubmitCoinDialog();
        submitCoinDialog.show(fragmentManager, TAG);
        return submitCoinDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.submit_coin_dialog, container, false);

        toolbar = view.findViewById(R.id.toolbar);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
    }
}
