package com.example.dictionary;

import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MaterialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material);
        TextView material_title = findViewById(R.id.material_title);
        TextView material_text = findViewById(R.id.material_text);
        TextView count_good_repeat = findViewById(R.id.count_good_repeat);
        TextView count_bad_repeat = findViewById(R.id.count_bad_repeat);
        material_title.setText(getIntent().getStringExtra("material_title"));
        material_text.setText(getIntent().getStringExtra("material_text"));
        count_good_repeat.setText(String.valueOf(getIntent().getIntExtra("material_cnt_right",0)));
        count_bad_repeat.setText(String.valueOf(getIntent().getIntExtra("material_cnt_wrong",0)));

       //Toast.makeText(this, getIntent().getStringExtra("category"), Toast.LENGTH_LONG).show();
       //Toast.makeText(this, getIntent().getStringExtra("material"), Toast.LENGTH_LONG).show();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_material_activity, menu);
        return true;
    }

}