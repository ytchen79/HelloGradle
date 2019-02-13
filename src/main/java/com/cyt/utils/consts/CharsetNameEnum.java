package com.cyt.utils.consts;

import java.nio.charset.Charset;

/**
 * 字符编码
 * @author cyt
 */
public enum CharsetNameEnum {
	
	GBK("GBK"),
	UTF_8("UTF-8");
	
	private String value;
	
	private Charset charset;

	public String value() {
		return this.value;
	}
	
	public Charset charset() {
		return this.charset;
	}
	
	private CharsetNameEnum(String value) {
		this.value = value;
		this.charset = Charset.forName(this.value);
	}
	
	public static boolean isValid(String value) {
		if (null == value) {
			return false;
		}
		CharsetNameEnum[] eSet = CharsetNameEnum.values();
		for (CharsetNameEnum e : eSet) {
			if (e.value.equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	public static CharsetNameEnum getEnumByValue(String value) {
		if (null == value) {
			return null;
		}
		CharsetNameEnum[] eSet = CharsetNameEnum.values();
		for (CharsetNameEnum e : eSet) {
			if (e.value.equals(value)) {
				return e;
			}
		}
		return null;
	}
	
	public static CharsetNameEnum getEnumByName(String name) {
		if (null == name) {
			return null;
		}
		CharsetNameEnum[] eSet = CharsetNameEnum.values();
		for (CharsetNameEnum e : eSet) {
			if (e.name().equals(name)) {
				return e;
			}
		}
		return null;
	}
}
