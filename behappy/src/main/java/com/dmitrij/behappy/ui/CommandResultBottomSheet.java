package com.dmitrij.behappy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dmitrij.behappy.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CommandResultBottomSheet extends BottomSheetDialogFragment {

    private static final String K_TITLE = "k_title";
    private static final String K_CONTENT = "k_content";

    public static CommandResultBottomSheet newInstance(String h, String o) {
        CommandResultBottomSheet f = new CommandResultBottomSheet();
        Bundle a = new Bundle();
        a.putString(K_TITLE, h);
        a.putString(K_CONTENT, o);
        f.setArguments(a);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_command_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tTxt = view.findViewById(R.id.result_title);
        TextView cTxt = view.findViewById(R.id.result_text);
        View btn = view.findViewById(R.id.btn_close);

        Bundle a = getArguments();
        if (a != null) {
            tTxt.setText(a.getString(K_TITLE));
            cTxt.setText(a.getString(K_CONTENT));
        }

        btn.setOnClickListener(v -> dismiss());
    }
}
