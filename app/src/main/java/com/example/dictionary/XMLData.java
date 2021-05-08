package com.example.dictionary;

import android.content.Context;
import android.nfc.FormatException;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class XMLData {


    static class Category {
        String category;
        int prevIndexFile = -1;
        boolean actual;
        List<Material> materials;

        public Category(String categoryName, List<Material> material, int indexFile, boolean actual) {
            this.category = categoryName;
            this.materials = material;
            this.prevIndexFile = indexFile;
            this.actual = actual;
        }

        public Category(String categoryName, boolean actual) {
            this.category = categoryName;
            this.materials = new ArrayList<Material>();
            this.actual = actual;
        }

        public Category() {
            this.materials = new ArrayList<Material>();
            this.actual = true;
        }

    }

    static class Material {
        String title;
        String text = null;
        Integer cntRight = 0;
        Integer cntWrong = 0;

        public Material(String title) {
            this.title = title;
            this.cntRight = 0;
            this.cntWrong = 0;
        }

        public Material(String title, String text, int cntRight, int cntWrong) {
            this.title = title;
            this.text = text;
            this.cntRight = cntRight;
            this.cntWrong = cntWrong;
        }

        public Material() {}
    }

    //Сохранить метаинформацию (название категорий и заголовки)
    static boolean putXMLMetaData(Context context, List<Category> listOut) {
        String filepath = context.getFilesDir().toString() + "/metadata.xml";
        try {
            FileOutputStream outputStream;
            File file = new File(filepath);
            file.createNewFile();
            outputStream = new FileOutputStream(file, false);

            outputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write("<metadata>\n".getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < listOut.size(); i++) {
                outputStream.write(("<category>" + listOut.get(i).category + "</category>\n").getBytes(StandardCharsets.UTF_8));
                for (int j = 0; j < listOut.get(i).materials.size(); j++) {
                    outputStream.write(("<title>" + listOut.get(i).materials.get(j).title + "</title>\n").getBytes(StandardCharsets.UTF_8));
                }
            }
            outputStream.write("</metadata>\n".getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("my_logs_put_xml_meta", e.getMessage());
            return false;
        }
        Log.i("my_logs_put_xml_meta", "Metadata loaded");
        return true;
    }

    static boolean renameFile(Context context, String oldName, String newName) {
        if (!newName.equals(oldName)) {
            File fileOld = new File(context.getFilesDir().toString() + "/" + oldName + ".xml");
            File fileNew = new File(context.getFilesDir().toString() + "/" + newName + ".xml");
            if (!fileOld.renameTo(fileNew)) {
                Log.e("my_logs_put_xml_file", "Cannot rename file! Name1: " + oldName + " Name2: " + newName);
                return false;
            }
        }
        return true;
    }

    //Сохранить информацию о категории
    static boolean putXMLFileData(Context context, Category outData, int index) {
        renameFile(context, String.valueOf(outData.prevIndexFile), String.valueOf(index));
        outData.prevIndexFile = index;
        String filepath = context.getFilesDir().toString() + "/" + outData.prevIndexFile + ".xml";
        try {
            FileOutputStream outputStream;
            File file = new File(filepath);
            file.createNewFile();
            outputStream = new FileOutputStream(file, false);
            outputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write("<filedata>\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write(("<category>" + outData.category + "</category>\n").getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < outData.materials.size(); i++) {
                if (outData.materials.get(i).title == null || outData.materials.get(i).text == null) {
                    Log.e("my_logs_save_material", "Something Null");
                }
                outputStream.write("<material>\n".getBytes(StandardCharsets.UTF_8));
                outputStream.write(("<title>" + outData.materials.get(i).title + "</title>\n").getBytes(StandardCharsets.UTF_8));
                outputStream.write(("<text>" + outData.materials.get(i).text + "</text>\n").getBytes(StandardCharsets.UTF_8));
                outputStream.write(("<cnt_right>" + outData.materials.get(i).cntRight + "</cnt_right>\n").getBytes(StandardCharsets.UTF_8));
                outputStream.write(("<cnt_wrong>" + outData.materials.get(i).cntWrong + "</cnt_wrong>\n").getBytes(StandardCharsets.UTF_8));
                outputStream.write("</material>\n".getBytes(StandardCharsets.UTF_8));

            }
            outputStream.write("</filedata>\n".getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Log.e("my_logs_put_xml_file", e.getMessage());
            return false;
        }
        Log.i("my_logs_put_xml_file", "File " + outData.category + " saved");
        return true;
    }

    //Парсинг файла, подаваемого пользователем
    static List<Category> getXMLUserData(InputStreamReader inpstr) {
        List<Category> categoryList = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inpstr);
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("category")) {
                    parser.next();
                    categoryList.add(new Category(parser.getText(), false));
                    parser.next();
                } else if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("material")) {
                    if (categoryList.isEmpty())
                        throw new FormatException("Haven`t found category!");
                    parser.next();
                    parser.next();
                    Material material = new Material();
                    if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("title")) {
                        parser.next();
                        material.title = parser.getText();
                        parser.next();
                    } else {
                        throw new FormatException("Invalid format material!");
                    }
                    parser.next();
                    parser.next();
                    if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("text")) {
                        parser.next();
                        material.text = parser.getText();
                        parser.next();
                    } else {
                        throw new FormatException("Invalid format material!");
                    }
                    material.cntRight = 0;
                    material.cntWrong = 0;
                    categoryList.get(categoryList.size() - 1).materials.add(material);
                    parser.next();
                } else if (parser.getEventType() == XmlPullParser.START_TAG && !parser.getName().equals("dictionary")) {
                    throw new FormatException("Unknown tag!");
                }
                parser.next();
            }
        } catch (Exception e) {
            Log.e("my_logs_xml_user_data", e.getMessage());
            e.printStackTrace();
            return null;
        }
        Log.i("my_logs_xml_user_data", "XML parsed");
        return categoryList;
    }

    //Парсинг файла с метаинформацией
    static List<Category> getXMLMetaData(Context context) {
        List<Category> listData = new ArrayList<>();
        String filepath = context.getFilesDir().toString();
        try {
            InputStreamReader inpstream;
            File metaFile = new File(filepath + "/metadata.xml");
            metaFile.createNewFile();
            inpstream = new InputStreamReader(new FileInputStream(metaFile));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inpstream);
            int indexFile = 0;
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("category")) {
                    parser.next();
                    listData.add(new Category(parser.getText(), new ArrayList<>(), indexFile++, true));
                    parser.next();
                } else if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("title")) {
                    if (listData.isEmpty())
                        throw new FormatException("Bad metadata!");
                    parser.next();
                    listData.get(listData.size() - 1).materials.add(new Material(parser.getText()));
                    parser.next();
                }
                parser.next();
            }
        } catch (IOException | FormatException | XmlPullParserException e) {
            Log.e("my_logs_xml_meta", e.getMessage());
            e.printStackTrace();
            return null;
        }
        Log.i("my_logs_xml_meta", "xml metadata received");
        return listData;
    }

    //Парсинг файла с информацией о категории
    static Category getXMLFileData(Context context, int prevIndexFile, List<String> actualTitles) {
        Category category = new Category();
        String filepath = context.getFilesDir().toString() + "/" + prevIndexFile + ".xml";
        File file = new File(filepath);
        if (!file.exists()) {
            Log.e("my_logs_xml_file", "Could not find file");
            return null;
        }
        try {
            InputStreamReader inpstream = new InputStreamReader(new FileInputStream(file));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inpstream);
            int indexTitle = 0;
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("category")) {
                    parser.next();
                    category.category = parser.getText();
                    parser.next();
                } else if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("material")) {
                    Material material = new Material();
                    parser.next();
                    parser.next();
                    if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("title")) {
                        parser.next();
                        material.title = parser.getText();
                        parser.next();
                    } else {
                        throw new FormatException("Invalid format material!");
                    }
                    parser.next();
                    parser.next();
                    if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("text")) {
                        parser.next();
                        material.text = parser.getText();
                        parser.next();
                    } else {
                        throw new FormatException("Invalid format material!");
                    }
                    parser.next();
                    parser.next();
                    if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("cnt_right")) {
                        parser.next();
                        material.cntRight = Integer.parseInt(parser.getText());
                        parser.next();
                    } else {
                        throw new FormatException("Invalid format material!");
                    }
                    parser.next();
                    parser.next();
                    if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("cnt_wrong")) {
                        parser.next();
                        material.cntWrong = Integer.parseInt(parser.getText());
                        parser.next();
                    } else {
                        throw new FormatException("Invalid format material!");
                    }
                    if (actualTitles.get(indexTitle++).equals(material.title))
                        category.materials.add(material);
                    else
                        category.actual = false;
                    parser.next();
                }
                parser.next();
            }
        } catch (IOException | XmlPullParserException | FormatException e) {
            e.printStackTrace();
            Log.e("my_logs_xml_file", e.getMessage());
            return null;
        }
        category.prevIndexFile = prevIndexFile;
        Log.i("my_logs_xml_file", "File data received");
        return category;
    }

    //Сохранить все данные
    static boolean saveAllData(Context context, List<Category> outData, boolean forceUpdate) {
        boolean ok = true;
        for (int i = 0; i < outData.size(); i++) {
            if (!outData.get(i).actual || forceUpdate) {//Если метаинформация была изменена
                ok &= putXMLMetaData(context, outData);
                break;
            }
        }
        //При необходимости содержимое файлов изменяется
        for (int i = 0; i < outData.size(); i++) {
            if (forceUpdate) {
                XMLData.renameFile(context, String.valueOf(outData.get(i).prevIndexFile), String.valueOf(i));
                outData.get(i).prevIndexFile = i;
            }
            if (!outData.get(i).actual) {
                ok &= putXMLFileData(context, outData.get(i), outData.get(i).prevIndexFile);
                outData.get(i).actual = true;
            }
            outData.get(i).prevIndexFile = i;
        }
        return ok;
    }


}