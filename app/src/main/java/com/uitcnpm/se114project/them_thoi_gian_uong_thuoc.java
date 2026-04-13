package com.uitcnpm.se114project;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class them_thoi_gian_uong_thuoc extends AppCompatActivity {
        EditText edtMedicine;
        TextView txtDate, txtTime;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_them_thoi_gian_uong_thuoc);
            edtMedicine = findViewById(R.id.edtMedicine);
            txtDate = findViewById(R.id.edtDate);
            txtTime = findViewById(R.id.edtTime);
            txtDate.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(this,
                        (view, y, m, d) ->
                                txtDate.setText(d + "/" + (m + 1) + "/" + y),
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)
                ).show();
            });
            txtTime.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new TimePickerDialog(this,
                        (view, h, m) ->
                                txtTime.setText(h + ":" + (m < 10 ? "0" + m : m)),
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE),
                        true
                ).show();
            });
        }
    }