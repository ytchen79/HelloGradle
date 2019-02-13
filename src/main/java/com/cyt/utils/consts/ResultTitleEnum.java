package com.cyt.utils.consts;

/**
 * 返回字段名
 * @author cyt
 */
public enum ResultTitleEnum {
	
	RETURN_CODE("return_code"),
	RETURN_MSG("return_message"),
	TIMESTAMP("timestamp"),
	RETURN_DATA("data");
	
	private String value;

	public String value() {
		return this.value;
	}
	private ResultTitleEnum(String value) {
		this.value = value;
	}
	
	public static boolean isValid(String value) {
		if (null == value) {
			return false;
		}
		ResultTitleEnum[] eSet = ResultTitleEnum.values();
		for (ResultTitleEnum e : eSet) {
			if (e.value.equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	public static ResultTitleEnum getEnumByValue(String value) {
		if (null == value) {
			return null;
		}
		ResultTitleEnum[] eSet = ResultTitleEnum.values();
		for (ResultTitleEnum e : eSet) {
			if (e.value.equals(value)) {
				return e;
			}
		}
		return null;
	}
	
	public static ResultTitleEnum getEnumByName(String name) {
		if (null == name) {
			return null;
		}
		ResultTitleEnum[] eSet = ResultTitleEnum.values();
		for (ResultTitleEnum e : eSet) {
			if (e.name().equals(name)) {
				return e;
			}
		}
		return null;
	}
}
