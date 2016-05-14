package criptografia.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import criptografia.mickey.MICKEY;
import criptografia.utils.Utils;

public class Encriptador {
	/**
	 * Se ejecuta por linea de comando.
	 * Pasar el nombre del archivo como primer parámetro en el args (y asegurarse de que exista).
	 * También asume que existe el archivo mickey.key en el mismo directorio con las propiedades key y nonce.
	 * @param args
	 */
	public static void main(String[] args) {
		/////////////
		//Variables//
		/////////////
		Properties prop = new Properties();
		String nombreDeArchivo 	= null;
		String nombreNuevo		= null;
		byte[] key 				= null;
		byte[] nonce 			= null;
		byte[] fotoContenido 	= null;
		byte[] fotoEncriptada 	= null;
		byte[] header 			= null;
		FileInputStream fin 	= null;
		File foto 				= null;
		FileOutputStream fout	= null; 
		File archivoEncriptado	= null;
		MICKEY mickey 			= new MICKEY();
		
		///////////////////////////////
		// Cargo la clave y el nonce //
		///////////////////////////////
		try {
			prop.load(new FileInputStream("mickey.key"));
		} catch (FileNotFoundException e) {
			System.out.println("Archivo mickey.key no encontrado");
			return;
		} catch (IOException e) {
			System.out.println("No se pudo abrir archivo mickey.key");
			return;
		}	
		try {
			key = Utils.leerPropByteArray(prop, "key");
			
			nonce = Utils.leerPropByteArray(prop, "nonce");
		} catch (Exception e) {
			System.out.println("Clave o nonce malformadas o inexistentes");
			return;
		}
		
		/////////////////////////////
		// Cargo la foto a memoria //
		/////////////////////////////
		if (args.length == 0) {
			System.out.println("Debe ingresar un nombre de archivo en formato BMP");
			return;
		}
		nombreDeArchivo = args[0];
		foto = new File(nombreDeArchivo);
		fotoContenido 	= new byte[(int) foto.length()];
		fotoEncriptada 	= new byte[(int) foto.length()];
		
		try {
			fin = new FileInputStream(foto);
			fin.read(fotoContenido);
		} catch (FileNotFoundException e) {
			System.out.println("Archivo no encontrado");
			return;
		}catch (IOException e) {
			System.out.println("No se puede leer el archivo");
			return;
		} finally {
			try {
				if (fin != null) 
					fin.close();
			} catch (IOException e) {}
		}
		
		//////////////////////
		// Guardo el header //
		//////////////////////
		header = new byte[54];
		for (int i = 0; i < header.length; i++) {
			header[i] = fotoContenido[i];
		}
		
		/////////////////////
		// Setup de Mickey //
		/////////////////////
		try {
			mickey.reset();
			mickey.setupKey(0, key, 1);
			mickey.setNonceSize(nonce.length - 1);
			mickey.setupNonce(nonce, 0);
		} catch (Exception e) {
			System.out.println("Error durante el setup de MICKEY");
			return;
		}
		////////////////////////////////////////////////////////////////////////////////
		// Se procesa la imagen y se deposita el contenido en el array fotoEncriptada //
		////////////////////////////////////////////////////////////////////////////////
		try {
			mickey.process(fotoContenido, 0, fotoEncriptada, 0, fotoContenido.length);
		} catch (Exception e) {
			System.out.println("Error durante el proceso del archivo");
			return;
		}
		
		////////////////////////////////////////////
		// Sobreescribimos con el header original //
		////////////////////////////////////////////
		for (int i = 0; i < header.length; i++) {
			fotoEncriptada[i] = header[i];
		}
		
		//////////////////////////////////////////////////
		// Escribimos el archivo con la foto encriptada //
		//////////////////////////////////////////////////
		nombreNuevo = 	nombreDeArchivo.substring(0, nombreDeArchivo.length() - 4) + 
						"-encriptado" + 
						nombreDeArchivo.substring(nombreDeArchivo.length()- 4, nombreDeArchivo.length());
		
		archivoEncriptado = new File(nombreNuevo);
		
		try {
			fout = new FileOutputStream(archivoEncriptado);
			fout.write(fotoEncriptada);
		} catch (FileNotFoundException e) {
			System.out.println("Error escribiendo el archivo");
			return;
		} catch (IOException e) {
			System.out.println("Error escribiendo el archivo");
			return;
		} finally {
			try {
				fout.close();
			} catch (IOException e) {}
		}
		
		
		System.out.println("Fin exitoso de la ejecución");
		System.out.println("Archivo escrito: " + nombreNuevo);
	}

}
