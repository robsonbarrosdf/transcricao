package br.leg.camara.transcricao.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {
	
	public static <T> T json2Object(String jsonString, Class<T> clazz) {
		Gson gson = new GsonBuilder()
				// .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
				// .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
				.create();
		
	    return gson.fromJson(jsonString, clazz);	 		
	}
	
	public static String encodeFileToBase64Binary(String fileName) throws IOException {
	    File file = new File(fileName);
	    byte[] encoded = Base64.getEncoder().encode(Files.readAllBytes(file.toPath()));
	    return new String(encoded, StandardCharsets.US_ASCII);
	}
	
	public static void executarComando(String comando) {

		try {
			// System.out.println("COMANDO: " + comando);
			ProcessBuilder pb = new ProcessBuilder();
			pb.command("cmd.exe", "/c", comando);
			pb.redirectError();
			// pb.directory(new File(System.getProperty("user.home")));
			Process process = pb.start();

			InputStreamConsumer isc = new InputStreamConsumer(process.getInputStream());
			isc.start();
			process.waitFor();
			
			//assert exitCode == 0;
			isc.join();
			// System.out.println("Process terminated with " + exitCode);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public static class InputStreamConsumer extends Thread {

        private InputStream is;

        public InputStreamConsumer(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {

            try {
                int value = -1;
                while ((value = is.read()) != -1) {
                    System.out.print((char)value);
                }
            } catch (IOException exp) {
                exp.printStackTrace();
            }

        }

    }
	

}
