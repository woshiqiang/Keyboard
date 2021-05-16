package com.student.keyboard.database.databasepo;


/**
 * @author shiina
 * @date 2019/11/26
 * 无优先级表的通用po类
 */
public class AssociatePhrase {
    /**
     * 自增id
     */
    private Integer id;
    /**
     * 字/词
     */
    private String font;
    /**
     * 对应汉译码
     */
    private String code;
    /**
     * 重写的toString()方法，输出到键盘。
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
}
