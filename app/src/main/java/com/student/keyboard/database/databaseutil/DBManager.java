package com.student.keyboard.database.databaseutil;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.student.keyboard.database.databasepo.AssociatePhrase;
import com.student.keyboard.database.databasepo.CommonWord;

import java.util.ArrayList;

/**
 * @author shiina
 * @date 2019/11/26
 * 数据库管理交互类
 */
public class DBManager {
    /**
     * 数据库
     */
    private SQLiteDatabase mDB;
    private static String dbPath = FileConfig.getDbPath() + "ECCode.db";
    private static DBManager instance = null;

    /**
     * 各键盘对应的数据库表名
     */
//    private final static String TONG_TABLE = "CommonlyUsed";
    private final static String TONG_TABLE = "AllCode";
    private final static String QUAN_TABLE = "AllCode";
    private final static String FAN_TABLE = "Traditional";
    private final static String SHAPE_TABLE = "Shape";


    /**
     * 中文键盘?对应在数据库的表现形式
     */
    private final static String INPUTQUS_1 = "_";
    private final static String INPUTQUS_2 = "__";
    private final static String INPUTQUS_3 = "___";
    private final static String INPUTQUS_4 = "____";

    public DBManager() {

    }

    /**
     * 初始化
     * @return
     */
    public static DBManager getInstance() {
        if(instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    /**
     * 打开数据库
     */
    private void openDB() {
        if (isSDCard()) {
            if (mDB == null || !mDB.isOpen()) {
                mDB = SQLiteDatabase.openDatabase(dbPath, null,
                        SQLiteDatabase.OPEN_READWRITE);
            }
        }
    }

    /**
     * SD卡
     * @return
     */
    private boolean isSDCard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 联想搜索
     * @param str Search_Pharse.getText()
     * @return
     */
    public ArrayList<AssociatePhrase> queryForAssociate(String str){
        ArrayList<AssociatePhrase> userList = new ArrayList<>();
        String[] selectionArgs = {str + "%"};
        openDB();
        try{
            String font;
            String code;
            String sql = "SELECT * FROM "+ SHAPE_TABLE +" WHERE font LIKE ? ";
            Cursor cursor = mDB.rawQuery(sql,selectionArgs);
            while (cursor.moveToNext()) {
                AssociatePhrase associatePhrase= new AssociatePhrase();
                font = cursor.getString(cursor.getColumnIndex("font"));
                code = cursor.getString(cursor.getColumnIndex("code"));
                associatePhrase.setFont(font);
                associatePhrase.setCode(code);
                userList.add(associatePhrase);
            }
            cursor.close();
            return userList;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 打字搜索
     * @param str Flag.InputWords
     * @param Table_State 键盘类型
     * @return
     */
    public ArrayList<CommonWord> queryForKeyBoard(String str, int Table_State){
        ArrayList<CommonWord> userList = new ArrayList<>();
        String table_Name;
        if(Table_State == 0) {
            table_Name = TONG_TABLE;
        }else if(Table_State == 1) {
            table_Name = QUAN_TABLE;
        }else if(Table_State == -1) {
            table_Name = FAN_TABLE;
        }else {
            return null;
        }
        String[] selectionArgs ;
        //当输入全为?时
        if(INPUTQUS_1.equals(str) || INPUTQUS_2.equals(str) || INPUTQUS_3.equals(str) || INPUTQUS_4.equals(str)) {
            System.out.println("aa" + "%" + " manager Line 109");
            selectionArgs = new String[]{"aa" + "%"};
        }else{
            selectionArgs = new String[]{str + "%"};
        }
        openDB();
        try{
            String font;
            String code;
            int count;
            String sql = "SELECT * FROM "+table_Name+" WHERE code like ? limit 3";
            Cursor cursor = mDB.rawQuery(sql,selectionArgs);
            while (cursor.moveToNext()) {
                CommonWord commonWord= new CommonWord();
                font = cursor.getString(cursor.getColumnIndex("font"));
                code = cursor.getString(cursor.getColumnIndex("code"));
                count = cursor.getInt(cursor.getColumnIndex("count"));
                commonWord.setFont(font);
                commonWord.setCode(code);
                commonWord.setCount(count);
                userList.add(commonWord);
            }
            cursor.close();
            if(Table_State == 1) {
                sql = "SELECT * FROM " + SHAPE_TABLE + " WHERE font LIKE ? ";
                cursor = mDB.rawQuery(sql, selectionArgs);
                while (cursor.moveToNext()) {
                    CommonWord commonWord = new CommonWord();
                    font = cursor.getString(cursor.getColumnIndex("font"));
                    code = cursor.getString(cursor.getColumnIndex("code"));
                    count = 7;
                    commonWord.setFont(font);
                    commonWord.setCode(code);
                    commonWord.setCount(count);
                    userList.add(commonWord);
                }
                cursor.close();
            }
            return userList;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
