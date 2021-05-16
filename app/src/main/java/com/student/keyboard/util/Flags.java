package com.student.keyboard.util;

import android.app.Activity;
import android.content.Context;


import com.student.keyboard.candidate.CandidateView;
import com.student.keyboard.listener.PermissionListener;
import com.student.keyboard.listener.PermissionListenerImp;

import java.util.ArrayList;

/**
 * @author shiina
 * @author zz
 * @author yjm
 * @date 2019/11/24
 */
public class Flags {
    /**
     * 判断当前字库
     */
    public static int Table_State;
    /**
     * 联想判断符
     *
     */
    public static Boolean isAssociate = false;

    /**
     * 候选列表
     *
     */
    public static ArrayList<String> CandidateList = new ArrayList<String>();
    /**
     * 输入的字符
     *
     */
     public static StringBuilder InputWords = new StringBuilder();

    public static String Old_Word;

    /**
     * 登入URL，传的东西在Login_Class类，收的东西在ReceiveLogin
     */
    public static String Login_URL="http://81.68.78.60:8080/user/login";


    /**
     * checkURL的固定部分   传的东西在Checkc_Class，返回值是 true 或者 false
     */
    public static String check_URL="http://81.68.78.60:8080/user/checkDeviceNumber";


    /**
     * InfoUrl的固定部分    返回值是 User_Class
     */
    public static String Info_Url = "http://81.68.78.60:8080/user/getInfo?username=";

    /**
     * 判断返回数据为“0”
     */
    public static String NI = "0";

    /**
     * 连接不上网络
     */
    public static String Error1= "NO INTERNET";


    /**
     * 用于判定，不改变
     */
    public static String True= "true";
    public static String False= "false";

    /**
     * 服务器传输回来的信息
     */
    public static String result;




    /**
     * username
      */
    public static String username;
    /**
     * 拼接获取信息接口的URL
     */
    public static String InfoURL(){
        return Info_Url + username;
    }

    /**
     * 拼接获取信息接口的头字段
     */
    public static String getUsernameHead(){
        return username + "; charset=utf-8";
    }

    public static CandidateView candidateView;
    public static int Old_Put = 0;

    public static void initAll(){
        isAssociate = false;
        CandidateList = new ArrayList<>();
        InputWords = new StringBuilder();
    }


    public static void clearCandidateListForDelete(CandidateView candidateView, ArrayList<String> CandidateList) {
        Flags.isAssociate = false;
        candidateView.setSuggestions(CandidateList);
    }

    public static boolean isGetPermission = false;
    public static Context context;
    public static Activity activity;

    /**
     * 权限监听接口
     */
    public static PermissionListener listener = new PermissionListenerImp();
    public static String nowActivity="";


    /**
     * 试用管理
     */
    public static boolean canUse = false;

    /**
     * 登录状态
     */


}
