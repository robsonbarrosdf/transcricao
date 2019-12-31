package br.leg.camara.transcricao.utils;

import java.util.List;

public class FFMpegUtils {

	public static long getDuration(String nomeArquivo) {
		// ffmpeg -i input.mp3 -f null -
		return 0;
	}
	
	public static String cutMp3(String arqEntrada, String arqSaida, long pontoInicialEmSegundos, long duracaoEmSegundos) {
		// ffmpeg -ss 30 -t 70 -i inputfile.mp3 -acodec copy outputfile.mp3
		return "";
	}
	
	public static List<String> splitMp3(String arqEntrada, String folderSaida, long tamanhoMaximoEmSegundosDeCadaParte) {
		// ffmpeg -i in.m4a                 -f segment -segment_time 300  -c copy out%03d.m4a
		// ffmpeg -i "input_audio_file.mp3" -f segment -segment_time 3600 -c copy output_audio_file_%03d.mp3
		return null;
	}
	
}
