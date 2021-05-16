package com.student.keyboard.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.student.keyboard.MyApp;
import com.student.keyboard.R;
import com.student.keyboard.constants.DemoKeyCode;
import com.student.keyboard.database.databasepo.CommonWord;
import com.student.keyboard.database.databaseutil.DBManager;
import com.student.keyboard.service.MyInputMethodService;
import com.student.keyboard.util.Flags;
import com.student.keyboard.util.MyUtil;

import java.util.ArrayList;
import java.util.List;

public class MyKeyboardView extends KeyboardView {

    //键盘模式（英文键盘、英文符号键盘、中文符号）
    private Keyboard ChinesesSymbol_Keyboard, EnglishSymbol_Keyboard, English_Keyboard;

    //字库模式
    private Keyboard Quan_Keyboard, Tong_Keyboard, Fan_Keyboard;

    private float mDownX;
    private float mDownY;

    public MyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initKeyboardView(context);
    }


    public MyKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initKeyboardView(Context context) {
        //英文键盘
        English_Keyboard = new Keyboard(context, R.xml.chinese);
        //中文符号键盘
        ChinesesSymbol_Keyboard = new Keyboard(context, R.xml.chinese_symbol);
        //英文符号键盘
        EnglishSymbol_Keyboard = new Keyboard(context, R.xml.english_symbol);
        //中文键盘下的全字库
        Quan_Keyboard = new Keyboard(context, R.xml.quan_keyboard);
        //中文键盘下的通字库
        Tong_Keyboard = new Keyboard(context, R.xml.tong_keyboard);
        //中文键盘下的繁字库
        Fan_Keyboard = new Keyboard(context, R.xml.fan_keyboard);

        setKeyboard(EnglishSymbol_Keyboard);
        setOnKeyboardActionListener(new MyOnKeyboardActionListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        float x = me.getX();
        float y = me.getY();
        switch (me.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                setPreviewEnabled(false);
                //滑动距离小于10dp时不隐藏键盘预览 大于10dp时隐藏键盘按键预览
                if (Math.abs(x - mDownX) >= MyUtil.dp2px(0) || Math.abs(y - mDownY) >= MyUtil.dp2px(0)) {
                    //取消预览
                    setPopupOffset(0, MyUtil.dp2px(0));
                }
                break;
        }
        return super.onTouchEvent(me);

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            int code = key.codes[0];
            if (code == Keyboard.KEYCODE_SHIFT || code == 18) {
                drawKeyBackground(R.drawable.keyboard_shift, canvas, key);
            }
            //删除
            if (code == DemoKeyCode.KEYCODE_DELETE) {
                drawKeyBackground(R.drawable.keyboard_delete, canvas, key);

            } else if (code == DemoKeyCode.CODE_PARTICIPLE) {
                drawKeyBackground(R.drawable.keyboard_participles, canvas, key);
            }
            //完成 return
            else if (code == Keyboard.KEYCODE_DONE
            ) {
                drawKeyBackground(R.drawable.keyboard_enter, canvas, key);

            }
            // 符号 数字 abc
            else if (code == DemoKeyCode.CODE_TYPE_CHANGE || code == DemoKeyCode.CODE_TYPE_ESYMBOL || code == DemoKeyCode.CODE_TYPE_CSYMBOL
            ) {
                drawKeyBackground(R.drawable.keyboard_gray, canvas, key);
                drawText(canvas, key);
            }

        }
    }

    /**
     * 绘制按键背景
     *
     * @param drawableId
     * @param canvas
     * @param key
     */
    private void drawKeyBackground(int drawableId, Canvas canvas, Keyboard.Key key) {
        Drawable npd = (Drawable) getContext().getResources().getDrawable(drawableId);
        int[] drawableState = key.getCurrentDrawableState();
        if (key.codes[0] != 0) {
            npd.setState(drawableState);
        }
        //绘制按键背景  加上 MyUtil.dp2px(4)
        npd.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
        npd.draw(canvas);
    }

    /**
     * 绘制文字
     *
     * @param canvas
     * @param key
     */
    private void drawText(Canvas canvas, Keyboard.Key key) {
        Rect bounds = new Rect();
        bounds.set(key.x, key.y, key.x + key.width, key.y + key.height);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        if (Keyboard.KEYCODE_DONE == key.codes[0]) {
            paint.setColor(Color.WHITE);
        } else {
            paint.setColor(ContextCompat.getColor(MyApp.getInstance(), android.R.color.black));
        }

        if (key.label != null) {
            paint.getTextBounds(key.label.toString(), 0, key.label.toString().length(), bounds);
            canvas.drawText(key.label.toString(), key.x + (key.width / 2), (key.y + key.height / 2) + bounds
                    .height() / 2, paint);
        }
    }

    public class MyOnKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

        private boolean mIsUpper;
        //跟踪英文符号键切换
        private boolean mIsEnglishSymbol = true;
        //跟踪中英文切换
        private boolean mIsChinese = true;
        //跟踪中文符号键切换
        private boolean mIsChineseSymbol=true;
        //用来读取字库文件
//        private ArrayList<Code> Code_List = new ArrayList<>();

        {
            if (Flags.Table_State == 0) {
                setKeyboard(Tong_Keyboard);
            }
            if (Flags.Table_State == 1) {
                setKeyboard(Quan_Keyboard);
            }
            if (Flags.Table_State == -1) {
                setKeyboard(Fan_Keyboard);
            }
        }


        @Override
        public void onPress(int i) {

        }

        @Override
        public void onRelease(int i) {

        }

        /**
         * 2.0版
         * 更新候选栏，针对数据库进行删选
         * @data 2019/11/26
         */
        private void updateCandidates() {
            Flags.isAssociate = false;
            ArrayList<CommonWord> db_list;
            if (Flags.candidateView != null) {
                if(mIsChinese) {
                    ArrayList<String> OldCandidateList = Flags.CandidateList;
                    Flags.CandidateList.clear();
                    if(Flags.InputWords.length()>0){
                        db_list = DBManager.getInstance().queryForKeyBoard(Flags.InputWords.toString().replace('?','_'),Flags.Table_State);
                        if(db_list.size() == 0) {
                            Flags.candidateView.setSuggestions(OldCandidateList);
                            return;
                        }
                        //将数据库中的字添加到候选区前面
                        for(int i = 0; i < db_list.size(); ++i) {
                            Flags.CandidateList.add(db_list.get(i).toString());
                        }
                        Flags.candidateView.setSuggestions(Flags.CandidateList);
                    }else {
                        Flags.candidateView.setSuggestions(Flags.CandidateList);
                    }
                }
                else {
                    Flags.CandidateList.clear();
                    Flags.candidateView.setSuggestions(Flags.CandidateList);
                }
            }
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Log.e("onKey", primaryCode + "");
            //键盘服务
            MyInputMethodService service = (MyInputMethodService) getContext();
            //当前输入的连接
            InputConnection ic = service.getCurrentInputConnection();
            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE:
                    Keyboard keyboard = new Keyboard(getContext(), R.xml.eglish);
                    setKeyboard(keyboard);
                    if (!Flags.isAssociate) {
                        int m_length = Flags.InputWords.length();
                        if (m_length != 0) {
                            Flags.InputWords.delete(m_length - 1, m_length);
                            ic.setComposingText(Flags.InputWords, 1);
                        } else {
                            ic.deleteSurroundingText(1, 0);
                        }
                    } else {
                        Flags.isAssociate = false;
                    }
                    updateCandidates();
                    break;
                // 大小写切换
                case Keyboard.KEYCODE_SHIFT:
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    Flags.initAll();
                    MyUtil.switchUpperOrLowerCase(mIsUpper, English_Keyboard);
                    mIsUpper = !mIsUpper;
                    setKeyboard(English_Keyboard);
                    break;
                //中英文切换
                case DemoKeyCode.CODE_TYPE_CHANGE:
                    mIsChinese = !mIsChinese;
                    if (mIsChinese) {
                        if (Flags.Table_State == 0) {
                            setKeyboard(Tong_Keyboard);
                        }
                        if (Flags.Table_State == 1) {
                            setKeyboard(Quan_Keyboard);
                        }
                        if (Flags.Table_State == -1) {
                            setKeyboard(Fan_Keyboard);
                        }
                    } else {
                        setKeyboard(English_Keyboard);
                        updateCandidates();

                    }

                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);

                    break;
                // 英文符号键盘切换
                case DemoKeyCode.CODE_TYPE_ESYMBOL:
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    Flags.initAll();
                    mIsEnglishSymbol = !mIsEnglishSymbol;
                    if (mIsEnglishSymbol) {
                        setKeyboard(English_Keyboard);
                    } else {
                        setKeyboard(EnglishSymbol_Keyboard);
                    }
                    break;
                //中文符号键
                case DemoKeyCode.CODE_TYPE_JH:
                    mIsChineseSymbol=!mIsChineseSymbol;
                    if (mIsChineseSymbol){
                        if(Flags.Table_State==0){
                            setKeyboard(Tong_Keyboard);
                        }
                        if(Flags.Table_State==1){
                            setKeyboard(Quan_Keyboard);
                        }
                        if(Flags.Table_State==-1){
                            setKeyboard(Fan_Keyboard);
                        }
                    }else {
                        setKeyboard(ChinesesSymbol_Keyboard);
                    }


                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView,null);
                    ic.setComposingText(Flags.InputWords, 1);

                    break;
                //完成
                case Keyboard.KEYCODE_DONE:
                    Flags.clearCandidateListForDelete(Flags.candidateView,null);
                    Flags.initAll();
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    break;
                // 切换字库
                case DemoKeyCode.CODE_TYPE_CSYMBOL:
//                    Code_List.clear();
                    Flags.Table_State++;
                    if (Flags.Table_State == 0) {
                        setKeyboard(Tong_Keyboard);
                    }
                    if (Flags.Table_State == 1) {
                        setKeyboard(Quan_Keyboard);
                    }
                    if (Flags.Table_State == 2) {
                        setKeyboard(Fan_Keyboard);
                        Flags.Table_State = -1;
                    }
                    if (Flags.isAssociate) {
                        Flags.initAll();
                        Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    } else {
                        if (0 != Flags.InputWords.length()) {
                            Flags.clearCandidateListForDelete(Flags.candidateView, null);
                            //ic.setComposingText(Flags.InputWords, 1);
                            updateCandidates();
                        } else {
                            Flags.initAll();
                        }
                    }

                    break;
                case DemoKeyCode.CODE_TYPE_1:
                    ic.commitText("1", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case DemoKeyCode.CODE_TYPE_2:
                    ic.commitText("2", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case DemoKeyCode.CODE_TYPE_3:
                    ic.commitText("3", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case DemoKeyCode.CODE_TYPE_4:
                    ic.commitText("4", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case DemoKeyCode.CODE_TYPE_5:
                    ic.commitText("5", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case DemoKeyCode.CODE_TYPE_6:
                    ic.commitText("6", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case DemoKeyCode.CODE_TYPE_7:
                    ic.commitText("7", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case DemoKeyCode.CODE_TYPE_8:
                    ic.commitText("8", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case DemoKeyCode.CODE_TYPE_9:
                    ic.commitText("9", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case DemoKeyCode.CODE_TYPE_0:
                    ic.commitText("0", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView, null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case DemoKeyCode.CODE_TYPE_SPACE:
                    if(Flags.CandidateList.toString()=="[]" || Flags.isAssociate) {
                        ic.commitText(" ", 1);
                        Flags.clearCandidateListForDelete(Flags.candidateView,null);
                        Flags.initAll();
                    }else {
                        String A=Flags.CandidateList.toString();
                        String b = A.substring(1,A.length()-1);
                        String B[]=b.split(":|, ");
                        String c = B[0];
                        ic.commitText(c, 1);
                        Flags.clearCandidateListForDelete(Flags.candidateView,null);
                        Flags.initAll();
                    }
                    break;
                case DemoKeyCode.CODE_TYPE_DH:
                    ic.commitText("，", 1);
                    Flags.initAll();
                    Flags.clearCandidateListForDelete(Flags.candidateView,null);
                    ic.setComposingText(Flags.InputWords, 1);
                    break;
                case 200:
                    ic.commitText("【", 1);
                    break;
                case 201:
                    ic.commitText("】", 1);
                    break;
                case 202:
                    ic.commitText("｛", 1);
                    break;
                case 203:
                    ic.commitText("｝", 1);
                    break;
                case 204:
                    ic.commitText("#", 1);
                    break;
                case 205:
                    ic.commitText("%", 1);
                    break;
                case 206:
                    ic.commitText("…", 1);
                    break;
                case 207:
                    ic.commitText("*", 1);
                    break;
                case 208:
                    ic.commitText("+", 1);
                    break;
                case 209:
                    ic.commitText("=", 1);
                    break;
                case 210:
                    ic.commitText("-", 1);
                    break;
                case 211:
                    ic.commitText("/", 1);
                    break;
                case 212:
                    ic.commitText("：", 1);
                    break;
                case 213:
                    ic.commitText("；", 1);
                    break;
                case 214:
                    ic.commitText("（", 1);
                    break;
                case 215:
                    ic.commitText("）", 1);
                    break;
                case 216:
                    ic.commitText("￥", 1);
                    break;
                case 217:
                    ic.commitText("@", 1);
                    break;
                case 218:
                    ic.commitText("“", 1);
                    break;
                case 219:
                    ic.commitText("”", 1);
                    break;
                case 220:
                    ic.commitText("\\", 1);
                    break;
                case 221:
                    ic.commitText("。", 1);
                    break;
                case 222:
                    ic.commitText("、", 1);
                    break;
                case 223:
                    ic.commitText("？", 1);
                    break;
                case 224:
                    ic.commitText("！", 1);
                    break;
                case 225:
                    ic.commitText(".", 1);
                    break;

                case 226:
                    ic.commitText("—", 1);
                    break;

                case 227:
                    ic.commitText("《", 1);
                    break;

                case 228:
                    ic.commitText("》", 1);
                    break;

                case 229:
                    ic.commitText("|", 1);
                    break;

                case 230:
                    ic.commitText("_", 1);
                    break;

                case 231:
                    ic.commitText("^", 1);
                    break;
                case 232:
                    ic.commitText("$", 1);
                    break;
                case 233:
                    ic.commitText("~", 1);
                    break;
                case 234:
                    ic.commitText("·", 1);
                    break;
                case 235:
                    ic.commitText("√ ", 1);
                    break;
                case 236:
                    ic.commitText("π", 1);
                    break;

                case 237:
                    ic.commitText(".com", 1);
                    break;
                case 238:
                    ic.commitText("http://", 1);
                    break;
                case 239:
                    ic.commitText(".cn", 1);
                    break;
                case 240:
                    ic.commitText("www.", 1);
                    break;
                //英文符号
                case 300:
                    ic.commitText(".", 1);
                    break;
                case 301:
                    ic.commitText("@", 1);
                    break;
                case 302:
                    ic.commitText("~", 1);
                    break;
                case 303:
                    ic.commitText("-", 1);
                    break;
                case 304:
                    ic.commitText(",", 1);
                    break;
                case 305:
                    ic.commitText(":", 1);
                    break;
                case 306:
                    ic.commitText("*", 1);
                    break;
                case 307:
                    ic.commitText("?", 1);
                    break;
                case 308:
                    ic.commitText("!", 1);
                    break;
                case 309:
                    ic.commitText("_", 1);
                    break;
                case 310:
                    ic.commitText("#", 1);
                    break;
                case 311:
                    ic.commitText("/", 1);
                    break;
                case 312:
                    ic.commitText("=", 1);
                    break;
                case 313:
                    ic.commitText("+", 1);
                    break;
                case 314:
                    ic.commitText("^", 1);
                    break;
                case 315:
                    ic.commitText(";", 1);
                    break;
                case 316:
                    ic.commitText("%", 1);
                    break;
                case 317:
                    ic.commitText("...", 1);
                    break;
                case 318:
                    ic.commitText("$", 1);
                    break;
                case 319:
                    ic.commitText("\\", 1);
                    break;
                case 320:
                    ic.commitText("(", 1);
                    break;
                case 321:
                    ic.commitText(")", 1);
                    break;
                case 322:
                    ic.commitText("→", 1);
                    break;
                case 323:
                    ic.commitText("|", 1);
                    break;
                case 324:
                    ic.commitText("·", 1);
                    break;
                case 325:
                    ic.commitText("￥", 1);
                    break;
                case 326:
                    ic.commitText("[", 1);
                    break;
                case 327:
                    ic.commitText("]", 1);
                    break;
                case 328:
                    ic.commitText("'", 1);
                    break;
                case 329:
                    ic.commitText("{", 1);
                    break;
                case 330:
                    ic.commitText("}", 1);
                    break;
                case 331:
                    ic.commitText("《", 1);
                    break;
                case 332:
                    ic.commitText("》", 1);
                    break;
                case 333:
                    ic.commitText("♫", 1);
                    break;
                case 334:
                    ic.commitText("✔", 1);
                    break;
                case 335:
                    ic.commitText("✘", 1);
                    break;
                case 336:
                    ic.commitText("‖", 1);
                    break;
                //一般文本
                default:
                    Flags.isAssociate = false;
                    char code = (char) primaryCode;
                    if (mIsChinese) {
                        Flags.InputWords.append(code);
                        if(Flags.InputWords.length()>4){
                            String First_of_CandidateList=Flags.CandidateList.toString();
                            if(First_of_CandidateList.length() > 2) {
                                ic.commitText(First_of_CandidateList.split(":")[0].split("\\[")[1], 1);
                            } else {
                                ic.commitText("", 1);
                            }
                            Flags.InputWords.delete(0,4);
                            ic.setComposingText(Flags.InputWords, 1);
                            updateCandidates();
                        }
                        else
                        {
                            if(Flags.CandidateList.size()==1)
                            {
                                String First_of_CandidateList=Flags.CandidateList.toString();
                                ic.commitText(First_of_CandidateList.split(":")[0].split("\\[")[1],1);
                                Flags.InputWords.setLength(0);
                                Flags.InputWords.append(code);
                            }
                            ic.setComposingText(Flags.InputWords, 1);
                            updateCandidates();
                        }
                    }
                    //当为英文状态下
                    else{
                        ic.commitText(code+"",1);
                    }
            }
        }

        @Override
        public void onText(CharSequence charSequence) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    }
}
