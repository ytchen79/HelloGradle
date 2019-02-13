package com.cyt.utils.consts;

/**
 * 返回类型
 * @author cyt
 */
public enum ResultCodeEnum {
	SUCCESS("SUCCESS"),
	ERROR("ERROR"),
	DELETE("DELETE");
	
	private String value;

	public String value() {
		return this.value;
	}
	private ResultCodeEnum(String value) {
		this.value = value;
	}
	
	public static boolean isValid(String value) {
		if (null == value) {
			return false;
		}
		ResultCodeEnum[] eSet = ResultCodeEnum.values();
		for (ResultCodeEnum e : eSet) {
			if (e.value.equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	public static ResultCodeEnum getEnumByValue(String value) {
		if (null == value) {
			return null;
		}
		ResultCodeEnum[] eSet = ResultCodeEnum.values();
		for (ResultCodeEnum e : eSet) {
			if (e.value.equals(value)) {
				return e;
			}
		}
		return null;
	}
	
	public static ResultCodeEnum getEnumByName(String name) {
		if (null == name) {
			return null;
		}
		ResultCodeEnum[] eSet = ResultCodeEnum.values();
		for (ResultCodeEnum e : eSet) {
			if (e.name().equals(name)) {
				return e;
			}
		}
		return null;
	}
}
