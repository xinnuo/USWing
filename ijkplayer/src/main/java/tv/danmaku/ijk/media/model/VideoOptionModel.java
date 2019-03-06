package tv.danmaku.ijk.media.model;

/**
 * 配置ijk option用
 */
public class VideoOptionModel {

    public static final int VALUE_TYPE_INT = 0;
    public static final int VALUE_TYPE_STRING = 1;

    //你设置的value是int还是string，ijk的option value 对应。
    private int valueType;
    private int category;
    private int valueInt;
    private String name;
    private String valueString;

    public VideoOptionModel(int category, String name, int value) {
        super();
        this.category = category;
        this.name = name;
        this.valueInt = value;
        valueType = VALUE_TYPE_INT;
    }

    public VideoOptionModel(int category, String name, String value) {
        super();
        this.category = category;
        this.name = name;
        this.valueString = value;
        valueType = VALUE_TYPE_STRING;
    }

    public int getValueType() {
        return valueType;
    }

    public void setValueType(int valueType) {
        this.valueType = valueType;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getValueInt() {
        return valueInt;
    }

    public void setValueInt(int valueInt) {
        this.valueInt = valueInt;
        valueType = VALUE_TYPE_INT;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
        valueType = VALUE_TYPE_STRING;
    }

}
