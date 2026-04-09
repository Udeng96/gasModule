package com.eseict.gasmodule.data.constant;

public enum StateEnum {
    CLEAR("해제", 0),
    OCCUR("발생", 1),
    INVALID("유효하지 않음", -1);

    private final String state;
    private final int code;

    StateEnum(String state, int code) {
        this.state = state;
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public int getCode() {
        return code;
    }

    public static StateEnum fromState(String rawState) {
        for (StateEnum value : values()) {
            if (rawState.contains(value.state)) {
                return value;
            }
        }
        return INVALID;
    }

    public static StateEnum fromCode(int code) {
        for (StateEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return INVALID;
    }
}
