package com.example.shoji.dailytask.ui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.shoji.dailytask.R;


public class TaskEditorActivity extends AppCompatActivity {

    private RadioGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        Context context = this;
        mRadioGroup = findViewById(R.id.radio_group);

        String[] labels = getResources().getStringArray(R.array.priority_label_array);
        int[] values = getResources().getIntArray(R.array.priority_value_array);
        int defaultValue = getResources().getInteger(R.integer.priority_value_default);
        for(int i=0; i< labels.length; ++i) {
            RadioButton button = new RadioButton(context);
            button.setText(labels[i]);
            button.setTag(values[i]);

            if(defaultValue == values[i])
                button.setChecked(true);

            mRadioGroup.addView(button);
        }

    }
}
