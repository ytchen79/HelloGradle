package com.cyt.utils.consts;

/**
 * 文件处理状态
 * @author cyt
 */
public enum ProcessStatusEnum {
	
	SUCCESS("S"),
	ERROR("E"),
	NOHANDLE("N"),
	TERMINAL("T");
	
	private String value;

	public String value() {
		return this.value;
	}
	private ProcessStatusEnum(String value) {
		this.value = value;
	}
	
	public static boolean isValid(String value) {
		if (null == value) {
			return false;
		}
		ProcessStatusEnum[] eSet = ProcessStatusEnum.values();
		for (ProcessStatusEnum e : eSet) {
			if (e.value.equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	public static ProcessStatusEnum getEnumByValue(String value) {
		if (null == value) {
			return null;
		}
		ProcessStatusEnum[] eSet = ProcessStatusEnum.values();
		for (ProcessStatusEnum e : eSet) {
			if (e.value.equals(value)) {
				return e;
			}
		}
		return null;
	}
	
	public static ProcessStatusEnum getEnumByName(String name) {
		if (null == name) {
			return null;
		}
		ProcessStatusEnum[] eSet = ProcessStatusEnum.values();
		for (ProcessStatusEnum e : eSet) {
			if (e.name().equals(name)) {
				return e;
			}
		}
		return null;
	}
}
