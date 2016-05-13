package criptografia.utils;

import java.util.Properties;

public class Utils {
	public static byte[] hexStringAByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static byte[] leerPropByteArray(Properties prop, String clave){		
		String propiedadStr = prop.getProperty(clave);		

		byte[] propiedadByteArray = hexStringAByteArray(propiedadStr);
		
		return propiedadByteArray;
		
	}
}
