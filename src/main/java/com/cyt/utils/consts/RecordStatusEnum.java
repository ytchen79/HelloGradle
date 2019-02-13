package com.cyt.utils.consts;

/**
 * 有效状态位
 * @author cyt
 */
public enum RecordStatusEnum {
	VALID("Y"),
	INVALID("N");
	
	
	private String value;

	public String value() {
		return this.value;
	}
	private RecordStatusEnum(String value) {
		this.value = value;
	}
	
	public static boolean isValid(String value) {
		if (null == value) {
			return false;
		}
		RecordStatusEnum[] eSet = RecordStatusEnum.values();
		for (RecordStatusEnum e : eSet) {
			if (e.value.equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	public static RecordStatusEnum getEnumByValue(String value) {
		if (null == value) {
			return null;
		}
		RecordStatusEnum[] eSet = RecordStatusEnum.values();
		for (RecordStatusEnum e : eSet) {
			if (e.value.equals(value)) {
				return e;
			}
		}
		return null;
	}
	
	public static RecordStatusEnum getEnumByName(String name) {
		if (null == name) {
			return null;
		}
		RecordStatusEnum[] eSet = RecordStatusEnum.values();
		for (RecordStatusEnum e : eSet) {
			if (e.name().equals(name)) {
				return e;
			}
		}
		return null;
	}
}
