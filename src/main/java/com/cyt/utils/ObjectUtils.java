package com.cyt.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;

public class ObjectUtils {

	private static final String BigDecimal_Type = "java.math.BigDecimal";
	private static final String String_Type = "java.lang.String";
	
	public static Object paddingNull(Object obj) throws Exception {
		Field[] fields = obj.getClass().getDeclaredFields();
		for (int i=0; i<fields.length; ++i) {
			Field field = fields[i];
			field.setAccessible(true);
			String type = field.getType().getName();
			if (String_Type.equals(type) && StringUtils.trim(field.get(obj)) == null) {
				field.set(obj, " ");
				
			} else if (BigDecimal_Type.equals(type) && field.get(obj) == null) {
				field.set(obj, new BigDecimal(0));
			}
		}
		return obj;
	}
	
}
