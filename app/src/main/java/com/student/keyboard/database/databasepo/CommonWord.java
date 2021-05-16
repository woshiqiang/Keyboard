package com.student.keyboard.database.databasepo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shiina
 * @date 2019/11/26
 * 有优先级表的通用po类
 */
public class CommonWord {
    /**
     * 自增id
     */
    private Integer id;
    /**
     * 字/词
     */
    private String font;
    /**
     * 汉译码
     */
    private String code;
    /**
     * 优先级
     */
    private int count;

    /**
     * 重写toString()方法输出到键盘
     * @return
     */
    @Override
    public String toString() {
        return getFont() + ":[" + getCode() + "]";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
