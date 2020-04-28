package com.example.doubleselectsidebar;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DoubleSelectSeekBar seekBar;
    private int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekBar = findViewById(R.id.seek_bar);
        seekBar.setMinValue(0);
        seekBar.setMaxValue(400);
        seekBar.setDegree(50);
        seekBar.setNumStep(25);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setPosition(num, num + 50);
                num += 50;
            }
        });
    }
}
