package criptografia.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import criptografia.utils.Utils;

public class Encriptador {
	public static void main(String[] args) {
		
		Properties prop = new Properties();
		
		try {
			prop.load(new FileInputStream("mickey.key"));
		} catch (FileNotFoundException e) {
			System.out.println("Archivo mickey.key no encontrado");
			return;
		} catch (IOException e) {
			System.out.println("No se pudo abrir archivo mickey.key");
			return;
		}	
		
		byte[] key = Utils.leerPropByteArray(prop, "key");
		
		byte[] nonce = Utils.leerPropByteArray(prop, "nonce");
		
		System.out.println("Exito absoluto!");
	}

}
