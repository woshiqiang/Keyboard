package com.student.keyboard.service;

import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.student.keyboard.R;
import com.student.keyboard.candidate.CandidateView;
import com.student.keyboard.database.databasepo.AssociatePhrase;
import com.student.keyboard.database.databaseutil.DBManager;
import com.student.keyboard.database.databaseutil.FileConfig;
import com.student.keyboard.util.Flags;

import java.util.ArrayList;

public class MyInputMethodService extends InputMethodService {
    public MyInputMethodService() {
        Log.i("MyInputService Open", "MyInuputService Open Success");
    }

    @Override
    public View onCreateInputView() {
        Log.i("onCreateInputView","onCreateInputView Success");
        View view = getLayoutInflater().inflate(R.layout.keyboard_layout, null);
        //添加时间2019/11/29
        FileConfig.setWorkPath(this, "ECCode/DATA_STORAGE/");
        FileConfig.copyAccessDB(this);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateCandidatesView() {

        Flags.candidateView = new CandidateView(this);
        Flags.candidateView.setService(this);
        this.setCandidatesViewShown(true);
        return Flags.candidateView;
    }

    /**
     * 选中待选栏的字时触发，上屏并触发联想associate(String strs)方法
     * @date 2019/11/28
     * @param mSelectedIndex
     */
    public void pickSuggestionManually(int mSelectedIndex) {
        //每次点击字上屏时，先把存储字给清除
        String A=Flags.CandidateList.toString();
        String c=A.substring(1,A.length()-1);
        String b[]=c.split(":|, ");
        ArrayList<String> mCandidateList = new ArrayList<String>();
        for (int i = 0; i < b.length; i=i+2) {
            mCandidateList.add(b[i]);
        }
        if(Flags.isAssociate) {
            if(null != Flags.Old_Word) {
                getCurrentInputConnection().deleteSurroundingText(Flags.Old_Word.length(),0);
            }
        }
        // 往输入框输出内容
        getCurrentInputConnection().commitText(mCandidateList.get(mSelectedIndex), 1);
        Flags.Old_Word = mCandidateList.get(mSelectedIndex);
        Flags.Old_Put = Flags.Old_Word.length();
        //将该次上屏的字存储下来
        Flags.InputWords.setLength(0);
        Flags.isAssociate = false;
        associate(mCandidateList.get(mSelectedIndex));
    }

    /**
     * 联想上屏
     * @date 2019/11/28
     * @param strs
     */
    public void associate(String strs) {
        //联想标志位置为true
        Flags.isAssociate = true;
        //记录原CandidateList
        ArrayList<String> OldCandidateList = new ArrayList<>(Flags.CandidateList);
        Flags.initAll();
        if(Flags.candidateView != null) {
            //清空候选表
            Flags.CandidateList.clear();
            //联想列表
            ArrayList<AssociatePhrase> lists = DBManager.getInstance().queryForAssociate(strs);

            //联想列表String可展示型
            ArrayList<String> listsToString = new ArrayList<>();
            String[] candidate;

            //如果是空链表CandidateList初始化并且联想标识记为false
            if(lists == null) {
                Flags.CandidateList = new ArrayList<>();
                Flags.isAssociate = false;
                Flags.candidateView.setSuggestions(Flags.CandidateList);
                return;
            }

            //先将Associate类型的lists集合转成string类型
            for (int i = 0; i < lists.size(); ++i) {
                listsToString.add(lists.get(i).toString());
            }

            String temp1;
            String[] temp2;

            for (int i = 0; i < listsToString.size(); ++i) {
                temp1 = listsToString.get(i);
                temp2 = temp1.split(":");
                if (temp2[0].equals(strs)) {
                    continue;
                }
                Flags.CandidateList.add(temp1);
            }

            if(Flags.CandidateList.size() == 0) {
                Flags.isAssociate = false;
                Flags.candidateView.setSuggestions(Flags.CandidateList);
                return;
            }

            //判断是否是两个相同的联想
            int count = 0;
            if(Flags.CandidateList.size() == OldCandidateList.size()) {
                for (int i = 0; i < Flags.CandidateList.size(); ++i) {
                    if(Flags.CandidateList.get(i).equals(OldCandidateList.get(i))) {
                        ++count;
                    }
                }
                //如果全相等CandidateList初始化并且联想标识记为false
                if(count == Flags.CandidateList.size()) {
                    Flags.CandidateList = new ArrayList<>();
                    Flags.isAssociate = false;
                }else {
                    Flags.isAssociate = true;
                }
            }else if(Flags.CandidateList.size() == 1){
                //如果全相等CandidateList初始化并且联想标识记为false
                candidate = Flags.CandidateList.get(0).split(":");
                if(candidate[0].equals(strs)) {
                    Flags.CandidateList = new ArrayList<>();
                    Flags.isAssociate = false;
                }else {
                    Flags.isAssociate = true;
                }
            }else {
                Flags.isAssociate = true;
            }
            Flags.candidateView.setSuggestions(Flags.CandidateList);
        }
    }
}
