package com.example.dictionary;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MaterialActivity extends AppCompatActivity {
    TextView materialTitle;
    TextView materialText;
    TextView countGoodRepeat;
    TextView countBadRepeat;
    Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material);
        materialTitle = findViewById(R.id.materialTitle);
        materialText = findViewById(R.id.materialText);
        countGoodRepeat = findViewById(R.id.countGoodRepeat);
        countBadRepeat = findViewById(R.id.countBadRepeat);
        materialTitle.setText(getIntent().getStringExtra("materialTitle"));
        materialText.setText(getIntent().getStringExtra("materialText"));
        countGoodRepeat.setText(String.valueOf(getIntent().getIntExtra("materialCntRight", 0)));
        countBadRepeat.setText(String.valueOf(getIntent().getIntExtra("materialCntWrong", 0)));
        Button resetButton = findViewById(R.id.resetCounts);
        //Сбрасываем прогресс
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countBadRepeat.setText("0");
                countGoodRepeat.setText("0");
            }
        });
    }

    @Override
    public void onBackPressed() {
        intent.putExtra("materialCntRight", Integer.parseInt(countGoodRepeat.getText().toString()));
        intent.putExtra("materialCntWrong", Integer.parseInt(countBadRepeat.getText().toString()));
        intent.putExtra("categoryIndex", getIntent().getIntExtra("categoryIndex", 0));
        intent.putExtra("materialIndex", getIntent().getIntExtra("materialIndex", 0));
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

}