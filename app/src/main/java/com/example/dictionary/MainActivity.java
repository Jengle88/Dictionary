package com.example.dictionary;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
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

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    List<XMLData.Category> fullData;
    final int CHECK_STORAGE_PERMISSION = 1;
    final int CHECK_FILE_CHOSEN = 1;
    final int OPEN_TEXT = 2;
    final int OPEN_TRAINING = 3;
    boolean startAnotherActivity = false;
    boolean categoryChange = false;
    Context appContext;

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

        expListView = findViewById(R.id.groupItems);//Связываемся с нашим ExpandableListView
        prepareListData();//Подготавливаем список данных
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);//Настраиваем listAdapter
        appContext = this;

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Log.i("my_logs_repeat", listDataHeader.get(groupPosition) + " repeated");
                if (fullData.get(groupPosition).materials.get(childPosition).text == null) {
                    fullData.set(groupPosition, XMLData.getXMLFileData(getApplicationContext(), groupPosition, listDataChild.get(listDataHeader.get(groupPosition))));
                }
                if (fullData.get(groupPosition).materials.get(childPosition).text != null) {
                    startAnotherActivity = true;
                    Intent intent = new Intent(MainActivity.this, MaterialActivity.class);
                    intent.putExtra("category", fullData.get(groupPosition).category);
                    intent.putExtra("categoryIndex", groupPosition);
                    intent.putExtra("materialIndex", childPosition);
                    intent.putExtra("materialTitle", fullData.get(groupPosition).materials.get(childPosition).title);
                    intent.putExtra("materialText", fullData.get(groupPosition).materials.get(childPosition).text);
                    intent.putExtra("materialCntRight", fullData.get(groupPosition).materials.get(childPosition).cntRight);
                    intent.putExtra("materialCntWrong", fullData.get(groupPosition).materials.get(childPosition).cntWrong);
                    Log.i("my_logs_start_activity", "Start repeat Activity");
                    startActivityForResult(intent, OPEN_TEXT);
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
                    LayoutInflater li = LayoutInflater.from(appContext);
                    View alertView = li.inflate(R.layout.template_alert_dialog, null);
                    AlertDialog.Builder alert = new AlertDialog.Builder(appContext);
                    alert.setTitle("Удалить этот материал?");
                    alert.setView(alertView);
                    alert.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fullData.get(groupPosition).materials.remove(childPosition);
                            listDataChild.get(fullData.get(groupPosition).category).remove(childPosition);
                            fullData.get(groupPosition).actual = false;
                            listAdapter = new ExpandableListAdapter(appContext, listDataHeader, listDataChild);
                            expListView.setAdapter(listAdapter);//Настраиваем listAdapter
                        }
                    });
                    alert.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                    return true;
                } else {
                    //лонгклик был на группе
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    LayoutInflater li = LayoutInflater.from(appContext);
                    View alertView = li.inflate(R.layout.template_alert_dialog, null);
                    AlertDialog.Builder alert = new AlertDialog.Builder(appContext);
                    alert.setTitle("Что нужно сделать?");
                    alert.setView(alertView);
                    alert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fullData.remove(groupPosition);
                            listDataChild.remove(listDataHeader.get(groupPosition));
                            listDataHeader.remove(groupPosition);
                            listAdapter = new ExpandableListAdapter(appContext, listDataHeader, listDataChild);
                            expListView.setAdapter(listAdapter);
                            categoryChange = true;
                        }
                    });
                    alert.setNegativeButton("Тренировка", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startAnotherActivity = true;
                            Intent intent = new Intent(MainActivity.this, TrainingActivity.class);
                            if (fullData.get(groupPosition).materials.get(0).text == null)
                                fullData.set(groupPosition, XMLData.getXMLFileData(appContext, groupPosition, listDataChild.get(listDataHeader.get(groupPosition))));

                            ArrayList<String> titlesList = new ArrayList<>(fullData.get(groupPosition).materials.size());
                            ArrayList<String> textsList = new ArrayList<>(fullData.get(groupPosition).materials.size());
                            for (int i = 0; i < fullData.get(groupPosition).materials.size(); i++) {
                                titlesList.add(fullData.get(groupPosition).materials.get(i).title);
                                textsList.add(fullData.get(groupPosition).materials.get(i).text);
                            }
                            intent.putExtra("titlesList", titlesList);
                            intent.putExtra("textsList", textsList);
                            intent.putExtra("categoryIndex", groupPosition);
                            startActivityForResult(intent, OPEN_TRAINING);
                        }
                    });
                    //Создаем AlertDialog:
                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                    return true;
                }
            }

        });
    }

    @Override
    protected void onStop() {
        if (!startAnotherActivity) {
            Log.i("my_logs_stop", "Stop application");
            XMLData.saveAllData(this, fullData, categoryChange);

        } else {
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
        switch (requestCode) {
            case CHECK_FILE_CHOSEN:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {

                    String filename = data.getData().getLastPathSegment();
                    Log.i("my_logs_filename", "Filename: " + filename);
                    String filepath;
                    filename = filename.substring(filename.indexOf(':') + 1);
                    if (!filename.startsWith("/storage"))
                        filepath = Environment.getExternalStorageDirectory().toString() +
                                "/" + filename;
                    else
                        filepath = filename;
                    try {
                        InputStreamReader inputStream;
                        inputStream = new InputStreamReader(new FileInputStream(filepath));
                        List<XMLData.Category> categoriesList = XMLData.getXMLUserData(inputStream);
                        inputStream.close();
                        if (categoriesList == null)
                            throw new Exception("List is null");
                        for (int i = 0; i < categoriesList.size(); i++) {
                            int idxCategory = listDataHeader.indexOf(categoriesList.get(i).category);
                            if (idxCategory == -1) {
                                listDataHeader.add(categoriesList.get(i).category);
                                idxCategory = listDataHeader.size() - 1;
                            }
                            String category = listDataHeader.get(idxCategory);
                            List<String> titles = new ArrayList<String>();
                            for (int j = 0; j < categoriesList.get(i).materials.size(); j++)
                                titles.add(categoriesList.get(i).materials.get(j).title);
                            if (listDataChild.containsKey(category)) {
                                if (fullData.get(idxCategory).materials.size() == 0 ||
                                        fullData.get(idxCategory).materials.get(0).text == null)
                                    fullData.set(idxCategory, XMLData.getXMLFileData(this, idxCategory, listDataChild.get(category)));
                                List<String> temp = listDataChild.get(category);
                                temp.addAll(titles);
                                listDataChild.put(category, temp);
                                fullData.get(idxCategory).materials.addAll(categoriesList.get(i).materials);
                                fullData.get(idxCategory).actual = false;
                            } else {
                                listDataChild.put(category, titles);
                                fullData.add(new XMLData.Category(category, categoriesList.get(i).materials, fullData.size(), false));
                                fullData.get(idxCategory).prevIndexFile = idxCategory;
                            }
                            Log.i("my_logs_cnt_data", "full_data with category " + fullData.get(idxCategory).category +
                                    " have " + fullData.get(idxCategory).materials.size() + " materials");
                        }
                        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
                        expListView.setAdapter(listAdapter);//Настраиваем listAdapter
                        Log.i("my_logs_menu", "Materials have added");
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Возникла проблема с файлом", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Log.e("my_logs_file", "Error with file");
                }
                break;
            case OPEN_TEXT:
                if (resultCode == RESULT_OK && data != null) {
                    int categoryIndex = data.getIntExtra("categoryIndex", 0);
                    int materialIndex = data.getIntExtra("materialIndex", 0);
                    fullData.get(categoryIndex).materials.get(materialIndex).cntRight = data.getIntExtra("materialCntRight", 0);
                    fullData.get(categoryIndex).materials.get(materialIndex).cntWrong = data.getIntExtra("materialCntWrong", 0);
                    fullData.get(categoryIndex).actual = false;
                } else {
                    Log.e("my_logs_result", "Data error");
                }
                break;
            case OPEN_TRAINING:
                if (resultCode == RESULT_OK && data != null) {
                    int categoryIndex = data.getIntExtra("categoryIndex", -1);
                    ArrayList<Integer> cntRight = data.getIntegerArrayListExtra("cntRight");
                    ArrayList<Integer> cntWrong = data.getIntegerArrayListExtra("cntWrong");
                    for (int i = 0; i < fullData.get(categoryIndex).materials.size(); i++) {
                        fullData.get(categoryIndex).materials.get(i).cntRight += cntRight.get(i);
                        fullData.get(categoryIndex).materials.get(i).cntWrong += cntWrong.get(i);
                    }
                    fullData.get(categoryIndex).actual = false;
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        fullData = XMLData.getXMLMetaData(this);
        if (fullData == null)
            return;
        for (int i = 0; i < fullData.size(); i++) {
            listDataHeader.add(fullData.get(i).category);
            List<String> materialTitle = new ArrayList<>(fullData.get(i).materials.size());
            for (int j = 0; j < fullData.get(i).materials.size(); j++) {
                materialTitle.add(fullData.get(i).materials.get(j).title);
            }
            listDataChild.put(listDataHeader.get(i), materialTitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addMaterials) {
            Log.i("my_logs_menu", "add_materials pressed");
            startAnotherActivity = true;
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, CHECK_FILE_CHOSEN);
        } else if (item.getItemId() == R.id.addGroup) {
            Log.i("my_logs_menu", "add_group pressed");
            LayoutInflater li = LayoutInflater.from(this);
            View alertView = li.inflate(R.layout.typing_alert_dialog, null);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Название раздела");
            alert.setView(alertView);
            final EditText input = alertView.findViewById(R.id.inputText);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = String.valueOf(input.getText());
                    if (!listDataChild.containsKey(value)) {
                        fullData.add(new XMLData.Category(value, new ArrayList<>(), fullData.size(), false));
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
        } else if (item.getItemId() == R.id.support) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Справка о загрузке материалов")
                    .setMessage("1)Материалы загружаются с использованием XML-файлов\n\n" +
                            "2)ВАЖНО! Файл выбирать через полный путь к нему, а не через вкладки, такие как \"Загрузка\" и т.п.\n\n" +
                            "3)Иерархия тегов:\n" +
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
}