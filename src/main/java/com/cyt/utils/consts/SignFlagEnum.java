package com.cyt.utils.consts;

/**
 * 签名校验状态
 * @author cyt
 */
public  enum SignFlagEnum {
	
	SUCCESS("Y"),		// 全通过
	MD5_FAIL("M"), 		// MD5校验失败
	SIGN_FAIL("S");		// 签名校验失败
	
	private String value;

	public String value() {
		return this.value;
	}
	private SignFlagEnum(String value) {
		this.value = value;
	}
	
	public static boolean isValid(String value) {
		if (null == value) {
			return false;
		}
		SignFlagEnum[] eSet = SignFlagEnum.values();
		for (SignFlagEnum e : eSet) {
			if (e.value.equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	public static SignFlagEnum getEnumByValue(String value) {
		if (null == value) {
			return null;
		}
		SignFlagEnum[] eSet = SignFlagEnum.values();
		for (SignFlagEnum e : eSet) {
			if (e.value.equals(value)) {
				return e;
			}
		}
		return null;
	}
	
	public static SignFlagEnum getEnumByName(String name) {
		if (null == name) {
			return null;
		}
		SignFlagEnum[] eSet = SignFlagEnum.values();
		for (SignFlagEnum e : eSet) {
			if (e.name().equals(name)) {
				return e;
			}
		}
		return null;
	}
}
