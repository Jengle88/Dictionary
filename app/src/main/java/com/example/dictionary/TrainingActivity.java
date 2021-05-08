package com.example.dictionary;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TrainingActivity extends AppCompatActivity {
    static class TrainingMaterial {
        String title;
        String text;
        int deltaRight;
        int deltaWrong;
        int trueIndex;

        public static Comparator<TrainingMaterial> comparatorByIndex() {
            return new Comparator<TrainingMaterial>() {
                @Override
                public int compare(TrainingMaterial a, TrainingMaterial b) {
                    return Integer.compare(a.trueIndex, b.trueIndex);
                }
            };
        }

        public TrainingMaterial(String _title, String _text, int _trueIndex) {
            title = _title;
            text = _text;
            trueIndex = _trueIndex;
            deltaRight = 0;
            deltaWrong = 0;
        }
    }

    List<TrainingMaterial> mixList;
    int categoryIndex;
    int actualIndex;
    TextView titleView;
    TextView textView;
    Intent result = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        List<String> titlesList = getIntent().getStringArrayListExtra("titlesList");
        List<String> textsList = getIntent().getStringArrayListExtra("textsList");
        mixList = new ArrayList<>(titlesList.size());
        for (int i = 0; i < titlesList.size(); i++) {
            mixList.add(i, new TrainingMaterial(titlesList.get(i), textsList.get(i), i));
        }
        categoryIndex = getIntent().getIntExtra("categoryIndex", 0);
        Collections.shuffle(mixList);
        ImageButton cancelButton = findViewById(R.id.cancelButton);
        ImageButton acceptButton = findViewById(R.id.acceptButton);
        Button checkButton = findViewById(R.id.checkButton);
        titleView = findViewById(R.id.trainTitle);
        textView = findViewById(R.id.trainText);
        actualIndex = 0;
        //Успешно
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mixList.get(actualIndex).deltaRight++;
                actualIndex = (actualIndex + 1) % mixList.size();
                previewData(actualIndex);
            }
        });
        //Провалено
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mixList.get(actualIndex).deltaWrong++;
                actualIndex = (actualIndex + 1) % mixList.size();
                previewData(actualIndex);
            }
        });
        //ВЫзов подсказки
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView.getVisibility() == View.INVISIBLE)
                    textView.setVisibility(View.VISIBLE);
                else
                    textView.setVisibility(View.INVISIBLE);
            }
        });
        previewData(actualIndex);
    }
    //Подготовка данных
    void previewData(int index) {
        titleView.setText(mixList.get(index).title);
        textView.setVisibility(View.INVISIBLE);
        textView.setText(mixList.get(index).text);
    }

    @Override
    public void onBackPressed() {
        Collections.sort(mixList, TrainingMaterial.comparatorByIndex());
        ArrayList<Integer> cntRight = new ArrayList<>(mixList.size());
        ArrayList<Integer> cntWrong = new ArrayList<>(mixList.size());
        for (int i = 0; i < mixList.size(); i++) {
            cntRight.add(i, mixList.get(i).deltaRight);
            cntWrong.add(i, mixList.get(i).deltaWrong);
        }
        result.putExtra("categoryIndex", categoryIndex);
        result.putIntegerArrayListExtra("cntRight", cntRight);
        result.putIntegerArrayListExtra("cntWrong", cntWrong);
        setResult(RESULT_OK, result);
        super.onBackPressed();
    }

}