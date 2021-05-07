package com.example.dictionary;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/*
* TODO:
*   1)Сделать обработчик кнопки сброса
*   2)Сделать меню выбора тренировки, удаления элементов и перемещения title
*   3)Сделать layout для тренировки
*
* */


public class MainActivity extends AppCompatActivity {

    /* Checks if external storage is available for read and write */
    boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    List<XMLData.Category> full_data;
    final int CHECK_STORAGE_PERMISSION = 1;
    final int CHECK_FILE_CHOSEN = 1;
    boolean startAnotherActivity = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionReadStatus = ContextCompat.
                checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionReadStatus == PackageManager.PERMISSION_GRANTED) {
            Log.i("my_logs_permission", "Permission is available");
        } else {
            Log.i("my_logs_permission", "Permission will request");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CHECK_STORAGE_PERMISSION);
        }

//        File file = new File(this.getFilesDir() + "/metadata.xml");
//        File file1 = new File(this.getFilesDir() + "/0.xml");
//        File file2 = new File(this.getFilesDir() + "/1.xml");
//        File file3 = new File(this.getFilesDir() + "/2.xml");
//
//        if(file.exists())
//            file.delete();
//        if(file1.exists())
//            file1.delete();
//        if(file2.exists())
//            file2.delete();
//        if(file3.exists())
//            file3.delete();

        expListView = (ExpandableListView) findViewById(R.id.group_items);//Связываемся с нашим ExpandableListView
        prepareListData();//Подготавливаем список данных
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);//Настраиваем listAdapter

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Log.i("my_logs_repeat", listDataHeader.get(groupPosition) + " repeated");
                if (full_data.get(groupPosition).materials.get(childPosition).text == null) {
                    full_data.set(groupPosition, XMLData.getXMLFileData(getApplicationContext(), groupPosition));
                }
                if (full_data.get(groupPosition).materials.get(childPosition).text != null) {
                    startAnotherActivity = true;
                    Intent intent = new Intent(MainActivity.this, MaterialActivity.class);
                    intent.putExtra("category", listDataHeader.get(groupPosition));
                    intent.putExtra("material_title", full_data.get(groupPosition).materials.get(childPosition).title);
                    intent.putExtra("material_text", full_data.get(groupPosition).materials.get(childPosition).text);
                    intent.putExtra("material_cnt_right", full_data.get(groupPosition).materials.get(childPosition).cnt_right);
                    intent.putExtra("material_cnt_wrong", full_data.get(groupPosition).materials.get(childPosition).cnt_wrong);
                    Log.i("my_logs_start_activity", "Start repeat Activity");
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Не удалось загрузить информацию", Toast.LENGTH_LONG).show();
                    Log.e("my_logs_chosen_title", "Could not find file");
                }
                return false;
            }
        });


        expListView.setOnItemLongClickListener(new ExpandableListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    //лонгклик был на child'е
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    Toast.makeText(
                            getApplicationContext(), "Длинное нажатие: " +
                                    listDataHeader.get(groupPosition)
                                    + " : "
                                    + listDataChild.get(
                                    listDataHeader.get(groupPosition)).get(
                                    childPosition), Toast.LENGTH_SHORT)
                            .show();
                    return true;
                } else {
                    //лонгклик был на группе
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    Toast.makeText(
                            getApplicationContext(), "Длинное нажатие: " +
                                    listDataHeader.get(groupPosition), Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            }

        });
    }


    @Override
    protected void onStop() {
        if(!startAnotherActivity){
            Log.i("my_logs_stop", "Stop application");
            XMLData.saveAllData(this,full_data);
        }
        else{
            Log.i("my_logs_stop", "Just another Activity");
            startAnotherActivity = false;
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CHECK_STORAGE_PERMISSION && grantResults.length == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение получено", Toast.LENGTH_SHORT).show();
                Log.i("my_logs_permission", "Permission have received");
            } else {
                Toast.makeText(this, "Без доступа к памяти нельзя загрузить материалы.", Toast.LENGTH_SHORT).show();
                Log.w("my_logs_permission", "Permission haven`t received");
            }
        } else {
            Toast.makeText(this, "Без доступа к памяти нельзя загрузить материалы.", Toast.LENGTH_SHORT).show();
            Log.w("my_logs_permission", "Permission haven`t received");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHECK_FILE_CHOSEN:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    String filename = data.getData().getLastPathSegment();
                    Log.i("my_logs_filename", "Filename: " + filename);
                    String filepath = Environment.getExternalStorageDirectory().toString() +
                            "/" + filename.substring(8);
                    try {
                        InputStreamReader inputStream;
                        inputStream = new InputStreamReader(new FileInputStream(filepath));
                        List<XMLData.Category> categories_list = XMLData.getXMLUserData(inputStream);
                        inputStream.close();
                        if (categories_list == null)
                            throw new Exception("List is null");
                        for (int i = 0; i < categories_list.size(); i++) {
                            int idx_category = listDataHeader.indexOf(categories_list.get(i).category);
                            if (idx_category == -1) {
                                listDataHeader.add(categories_list.get(i).category);
                                idx_category = listDataHeader.size() - 1;
                            }
                            String category = listDataHeader.get(idx_category);
                            List<String> titles = new ArrayList<String>();
                            for (int j = 0; j < categories_list.get(i).materials.size(); j++)
                                titles.add(categories_list.get(i).materials.get(j).title);
                            if (listDataChild.containsKey(category)) {
                                if (full_data.get(idx_category).materials.size() == 0 ||
                                        full_data.get(idx_category).materials.get(0).text == null)
                                    full_data.set(idx_category, XMLData.getXMLFileData(this, idx_category));
                                List<String> temp = listDataChild.get(category);
                                temp.addAll(titles);
                                listDataChild.put(category, temp);
                                full_data.get(idx_category).materials.addAll(categories_list.get(i).materials);
                                full_data.get(idx_category).actual = false;
                            } else {
                                listDataChild.put(category, titles);
                                full_data.add(new XMLData.Category(category, categories_list.get(i).materials, full_data.size(), false));
                                full_data.get(idx_category).prev_index_file = idx_category;
                            }
                            Log.i("my_logs_cnt_data", "full_data with category " + full_data.get(idx_category).category +
                                    " have " + full_data.get(idx_category).materials.size() + " materials");
                            //XMLData.putXMLFileData(this, full_data.get(idx_category), idx_category);
                        }
                        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
                        expListView.setAdapter(listAdapter);//Настраиваем listAdapter
                        //XMLData.putXMLMetaData(this, full_data);
                        Log.i("my_logs_menu", "Materials have added");
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Возникла ошибка", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    Log.e("my_logs_file", "Error with file");
                }
                break;
        }
    }


    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        full_data = XMLData.getXMLMetaData(this);
        for (int i = 0; i < full_data.size(); i++) {
            listDataHeader.add(full_data.get(i).category);
            List<String> material_title = new ArrayList<>(full_data.get(i).materials.size());
            for (int j = 0; j < full_data.get(i).materials.size(); j++) {
                material_title.add(full_data.get(i).materials.get(j).title);
            }
            listDataChild.put(listDataHeader.get(i), material_title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_materials) {
            Log.i("my_logs_menu", "add_materials pressed");
            startAnotherActivity = true;
            Intent intent = new Intent();
            intent.setType("text/xml");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, CHECK_FILE_CHOSEN);
        } else if (item.getItemId() == R.id.add_group) {
            Log.i("my_logs_menu", "add_group pressed");
            LayoutInflater li = LayoutInflater.from(this);
            View alertView = li.inflate(R.layout.template_alert_dialog, null);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Название раздела");
            alert.setView(alertView);
            final EditText input = alertView.findViewById(R.id.input_text);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = String.valueOf(input.getText());
                    if (!listDataChild.containsKey(value)) {
                        full_data.add(new XMLData.Category(value, full_data.size(), false));
                        Log.i("my_logs_groups", "Group have been added");
                        listDataHeader.add(value);
                        listDataChild.put(value, new ArrayList<String>());
                        listAdapter = new ExpandableListAdapter(getBaseContext(), listDataHeader, listDataChild);
                        expListView.setAdapter(listAdapter);//Настраиваем listAdapter
                        return;
                    }
                    Log.i("my_logs_groups", "Group have been found");
                }
            });
            alert.show();
        } else if(item.getItemId() == R.id.support){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Справка о загрузке материалов")
                    .setMessage("Материалы загружаются с использованием XML-файлов\n" +
                            "Иерархия тегов:\n" +
                            "<dictionary> - начало" +
                            "   <category>Название</category>\n" +
                            "   <material>\n" +
                            "       <title>Заголовок1</title>\n" +
                            "       <text>Текст1</text>\n" +
                            "   </material>\n" +
                            "</dictionary> - конец\n" +
                            "В одной категории может быть несколько материалов");
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

        /*but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if(isExternalStorageReadable())
                //    Toast.makeText(MainActivity.this, "Readable", Toast.LENGTH_LONG).show();
                //else
                //    Toast.makeText(MainActivity.this, "Not readable", Toast.LENGTH_LONG).show();
//
                //if(isExternalStorageWritable())
                //    Toast.makeText(MainActivity.this, "Writable", Toast.LENGTH_LONG).show();
                //else
                //    Toast.makeText(MainActivity.this, "Not writable", Toast.LENGTH_LONG).show();

                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,PICKFILE_RESULT_CODE);

                //String str = Environment.getExternalStorageDirectory().toString();
                //Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
               //try {
               //    outputStream = openFileOutput(str, Context.MODE_PRIVATE);
               //    outputStream.write(string.getBytes());
               //    outputStream.close();
               //} catch (Exception e) {
               //    e.printStackTrace();
               //}



            }
        });*/


}