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
        int prev_index_file = -1;
        boolean actual = true;
        List<Material> materials;

        public Category(String category_name, int index_file, boolean actual) {
            this.category = category_name;
            this.materials = new ArrayList<Material>();
            this.prev_index_file = index_file;
            this.actual = actual;
        }

        public Category(String category_name, List<Material> material, int index_file, boolean actual) {
            this.category = category_name;
            this.materials = material;
            this.prev_index_file = index_file;
            this.actual = actual;
        }

        public Category(String category_name, boolean actual) {
            this.category = category_name;
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
        Integer cnt_right = 0;
        Integer cnt_wrong = 0;

        public Material(String title, String text) {
            this.title = title;
            this.text = text;
            this.cnt_right = 0;
            this.cnt_wrong = 0;
        }

        public Material(String title) {
            this.title = title;
            this.cnt_right = 0;
            this.cnt_wrong = 0;
        }

        public Material(String title, String text, int cnt_right, int cnt_wrong) {
            this.title = title;
            this.text = text;
            this.cnt_right = cnt_right;
            this.cnt_wrong = cnt_wrong;
        }

        public Material(){

        }
    }


    static boolean putXMLMetaData(Context context, List<Category> list_out) {
        String filepath = context.getFilesDir().toString() + "/metadata.xml";
        try {
            FileOutputStream outputStream;
            File file = new File(filepath);
            file.createNewFile();
            outputStream = new FileOutputStream(file, false);

            outputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write("<metadata>\n".getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < list_out.size(); i++) {
                outputStream.write(("<category>" + list_out.get(i).category + "</category>\n").getBytes(StandardCharsets.UTF_8));
                for (int j = 0; j < list_out.get(i).materials.size(); j++) {
                    outputStream.write(("<title>" + list_out.get(i).materials.get(j).title + "</title>\n").getBytes(StandardCharsets.UTF_8));
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

    static boolean putXMLFileData(Context context, Category out_data, int index) {
        if (index != out_data.prev_index_file) {
            File fileOld = new File(context.getFilesDir().toString() + "/" + out_data.prev_index_file + ".xml");
            File fileNew = new File(context.getFilesDir().toString() + "/" + index + ".xml");
            if(!fileOld.renameTo(fileNew)){
                Log.e("my_logs_put_xml_file", "Cannot rename file! Name1: " + out_data.prev_index_file + " Name2: " + index);
                return false;
            }
            out_data.prev_index_file = index;
        }
        String filepath = context.getFilesDir().toString() + "/" + out_data.prev_index_file + ".xml";
        try {
            FileOutputStream outputStream;
            File file = new File(filepath);
            file.createNewFile();
            outputStream = new FileOutputStream(file, false);
            outputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write("<filedata>\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write(("<category>" + out_data.category + "</category>\n").getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < out_data.materials.size(); i++) {
                if(out_data.materials.get(i).title == null || out_data.materials.get(i).text == null){
                    Log.e("my_logs_save_material", "Something Null");
                }
                outputStream.write("<material>\n".getBytes(StandardCharsets.UTF_8));
//                outputStream.write(("<material\n" +
//                        "title=\"" + out_data.materials.get(i).title + "\"\n" +
//                        "text=\"" + out_data.materials.get(i).text + "\"\n" +
//                        "cnt_right=\"" + out_data.materials.get(i).cnt_right + "\"\n" +
//                        "cnt_wrong=\"" + out_data.materials.get(i).cnt_wrong + "\"\n" +
//                        "/>\n").getBytes(StandardCharsets.UTF_8));
                outputStream.write(("<title>" + out_data.materials.get(i).title + "</title>\n").getBytes(StandardCharsets.UTF_8));
                outputStream.write(("<text>" + out_data.materials.get(i).text + "</text>\n").getBytes(StandardCharsets.UTF_8));
                outputStream.write(("<cnt_right>" + out_data.materials.get(i).cnt_right + "</cnt_right>\n").getBytes(StandardCharsets.UTF_8));
                outputStream.write(("<cnt_wrong>" + out_data.materials.get(i).cnt_wrong + "</cnt_wrong>\n").getBytes(StandardCharsets.UTF_8));
                outputStream.write("</material>\n".getBytes(StandardCharsets.UTF_8));

            }
            outputStream.write("</filedata>\n".getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("my_logs_put_xml_file", e.getMessage());
            return false;
        }
        Log.i("my_logs_put_xml_file", "File " + out_data.category + " saved");
        return true;
    }

    static List<Category> getXMLUserData(InputStreamReader inpstr) {
        List<Category> cat_list = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inpstr);
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("category")) {
                    parser.next();
                    cat_list.add(new Category(parser.getText(), false));
                    parser.next();
                } else if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("material")) {
                    if (cat_list.isEmpty())
                        throw new FormatException("Haven`t found category!");
                    parser.next();
                    parser.next();
                    Material material = new Material();
                    if(parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("title")){
                        parser.next();
                        material.title = parser.getText();
                        parser.next();
                    } else{
                        throw new FormatException("Invalid format material!");
                    }
                    parser.next();
                    parser.next();
                    if(parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("text")){
                        parser.next();
                        material.text = parser.getText();
                        parser.next();
                    } else{
                        throw new FormatException("Invalid format material!");
                    }
                    material.cnt_right = 0;
                    material.cnt_wrong = 0;
                    cat_list.get(cat_list.size() - 1).materials.add(material);
                    parser.next();
//                    if (parser.getAttributeCount() == 2 && parser.getAttributeName(0).equals("title") &&
//                            parser.getAttributeName(1).equals("text")) {
//                        cat_list.get(cat_list.size() - 1).materials.add(
//                                new Material(parser.getAttributeValue(0), parser.getAttributeValue(1), 0, 0));
//                    } else {
//                        throw new FormatException("Invalid format material!");
//
//                    }
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
        return cat_list;
    }

    static List<Category> getXMLMetaData(Context context) {
        List<Category> list_data = new ArrayList<>();
        String filepath = context.getFilesDir().toString();
        try {
            InputStreamReader inpstream;
            File meta_file = new File(filepath + "/metadata.xml");
            meta_file.createNewFile();
            inpstream = new InputStreamReader(new FileInputStream(meta_file));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inpstream);
            int index_file = 0;
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("category")) {
                    parser.next();
                    list_data.add(new Category(parser.getText(), index_file++, true));
                    parser.next();
                } else if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("title")) {
                    if (list_data.isEmpty())
                        throw new FormatException("Bad metadata!");
                    parser.next();
                    list_data.get(list_data.size() - 1).materials.add(new Material(parser.getText()));
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
        return list_data;
    }

    static Category getXMLFileData(Context context, int prev_index_file) {
        Category category = new Category();
        String filepath = context.getFilesDir().toString() + "/" + prev_index_file + ".xml";
        File file = new File(filepath);
        if (!file.exists()){
            Log.e("my_logs_xml_file", "Could not find file");
            return null;
        }
        try {
            InputStreamReader inpstream = new InputStreamReader(new FileInputStream(file));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inpstream);
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("category")) {
                    parser.next();
                    category.category = parser.getText();
                    parser.next();
                } else if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("material")) {
                    Material material = new Material();
                    parser.next();
                    parser.next();
                    if(parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("title")){
                        parser.next();
                        material.title = parser.getText();
                        parser.next();
                    } else{
                        throw new FormatException("Invalid format material!");
                    }
                    parser.next();
                    parser.next();
                    if(parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("text")){
                        parser.next();
                        material.text = parser.getText();
                        parser.next();
                    } else{
                        throw new FormatException("Invalid format material!");
                    }
                    parser.next();
                    parser.next();
                    if(parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("cnt_right")){
                        parser.next();
                        material.cnt_right = Integer.parseInt(parser.getText());
                        parser.next();
                    } else{
                        throw new FormatException("Invalid format material!");
                    }
                    parser.next();
                    parser.next();
                    if(parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("cnt_wrong")){
                        parser.next();
                        material.cnt_wrong = Integer.parseInt(parser.getText());
                        parser.next();
                    } else{
                        throw new FormatException("Invalid format material!");
                    }
                    category.materials.add(material);
                    parser.next();
//                    if (parser.getAttributeCount() == 4) {
//                        category.materials.add(new Material(parser.getAttributeValue(0),
//                                parser.getAttributeValue(1),
//                                Integer.parseInt(parser.getAttributeValue(2)),
//                                Integer.parseInt(parser.getAttributeValue(3))));
//                    } else {
//                        throw new FormatException("Bad data!");
//                    }
                }
                parser.next();
            }
        } catch (IOException | XmlPullParserException | FormatException e) {
            e.printStackTrace();
            Log.e("my_logs_xml_file", e.getMessage());
            return null;
        }
        category.prev_index_file = prev_index_file;
        Log.i("my_logs_xml_file", "File data received");
        return category;
    }

    static boolean saveAllData(Context context, List<Category> out_data){
        boolean ok = true;
        for (int i = 0; i < out_data.size(); i++) {
            if(!out_data.get(i).actual){
                ok &= putXMLMetaData(context, out_data);
                break;
            }
        }
        for (int i = 0; i < out_data.size(); i++) {
            if(!out_data.get(i).actual){
                ok &= putXMLFileData(context,out_data.get(i),out_data.get(i).prev_index_file);
                out_data.get(i).actual = true;
            }
        }
        return ok;
    }


}
//            File[] files_arr = files_dir.listFiles();
//            for (int i = 0; i < files_arr.length; i++) {
//                inpstream = new InputStreamReader(new FileInputStream(files_arr[i]));
//                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//                XmlPullParser parser = factory.newPullParser();
//                parser.setInput(inpstream);
//                if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("category")) {
//                    parser.next();
//                    if (parser.getText().length() > LIMIT_CATEGORY_NAME)
//                        throw new FormatException("Bad data");
//                    list_data.add(new Category(parser.getText()));
//                    parser.next();
//                } else if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("material")) {
//                    if (list_data.isEmpty())
//                        throw new FormatException("Bad data");
//                    if (parser.getAttributeCount() == 4 &&
//                            parser.getAttributeName(0).equals("title") &&
//                            parser.getAttributeName(1).equals("text") &&
//                            parser.getAttributeName(2).equals("cnt_right") &&
//                            parser.getAttributeName(3).equals("cnt_wrong")) {
//                        list_data.get(list_data.size() - 1).materials.add(
//                                new Material(parser.getAttributeValue(0),
//                                        parser.getAttributeValue(1),
//                                        Integer.parseInt(parser.getAttributeValue(2)),
//                                        Integer.parseInt(parser.getAttributeValue(3))));
//                    } else {
//                        throw new FormatException("Bad data");
//                    }
//                } else if (parser.getEventType() == XmlPullParser.START_TAG && !parser.getName().equals("dictionary")) {
//                    throw new FormatException("Bad data");
//                }
//                parser.next();
//            }
//inpstr = new InputStreamReader(new FileInputStream(filepath));
//inpstr.close();
//XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//XmlPullParser parser = factory.newPullParser();
//parser.setInput(inpstr);
