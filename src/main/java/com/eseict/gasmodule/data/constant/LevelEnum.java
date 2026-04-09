package com.eseict.gasmodule.data.constant;

public enum LevelEnum {
    LEVEL0("초기화", 0),
    LEVEL1("1차", 1),
    LEVEL2("2차", 2),
    LEVEL3("3차", 3),
    LEVEL_INVALID("유효하지 않음", -1);

    private final String state;
    private final int code;

    LevelEnum(String state, int code) {
        this.state = state;
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public int getCode() {
        return code;
    }

    public static LevelEnum fromState(String rawState) {
        for (LevelEnum value : values()) {
            if (rawState.contains(value.state)) {
                return value;
            }
        }
        return LEVEL_INVALID;
    }

    public static LevelEnum fromCode(int code) {
        for (LevelEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return LEVEL_INVALID;
    }
}
