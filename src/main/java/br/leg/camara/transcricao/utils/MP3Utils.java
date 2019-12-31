package br.leg.camara.transcricao.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

public class MP3Utils {

	private static final String FFMPEG = "C:\\lixo\\speechtotext\\ffmpeg.exe ";

	public static long getDuration(String nomeArquivo) {
		// ffmpeg -i input.mp3 -f null -
		File file = new File(nomeArquivo);
		try {
			AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
			Map<String,Object> properties = baseFileFormat.properties();
			Long duration = (Long) properties.get("duration");
			return duration / 1000;
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static List<String> splitMp3(String nomeArquivoOrigem, String nomePastaDestino, long tamanhoMaximoEmSegundosDeCadaParte) {
		// ffmpeg -i in.m4a                 -f segment -segment_time 300  -c copy out%03d.m4a
		// ffmpeg -i "input_audio_file.mp3" -f segment -segment_time 3600 -c copy output_audio_file_%03d.mp3
		try {
			
			String PREFIXO_TRECHO_AUDIO = "audio_dividido_";
			StringBuilder comando = new StringBuilder();
			comando.append(FFMPEG);
			comando.append(" -y ");
			comando.append(" -i " + nomeArquivoOrigem);
			comando.append(" -f segment ");
			comando.append(" -segment_time " + (tamanhoMaximoEmSegundosDeCadaParte > 0 ? tamanhoMaximoEmSegundosDeCadaParte : 60));
			comando.append(" -c copy " + nomePastaDestino + File.separator + PREFIXO_TRECHO_AUDIO + "%03d.mp3");

			Utils.executarComando(comando.toString());
			
			return Files.list(Paths.get(nomePastaDestino))
		        .filter(f -> Files.isRegularFile(f) && f.getFileName().toString().startsWith(PREFIXO_TRECHO_AUDIO))
		        .map(p -> p.toAbsolutePath().toString())
		        .collect(Collectors.toList());

		} catch (Exception e) {
			return null;
		}		
	}
	
	public static String cutMP3(String nomeArquivoOrigem, String nomeArquivoDestino, long pontoInicialEmSegundos, long duracaoEmSegundos) {
		// ffmpeg -ss 30 -t 70 -i inputfile.mp3 -acodec copy outputfile.mp3
		try {
			StringBuilder comando = new StringBuilder();
			comando.append(FFMPEG);
			comando.append(" -y ");
			comando.append(" -i " + nomeArquivoOrigem);
			comando.append(" -t " + duracaoEmSegundos);
			comando.append(" -ss " + pontoInicialEmSegundos);
			comando.append(" -acodec copy " + nomeArquivoDestino);

			Utils.executarComando(comando.toString());

			return nomeArquivoDestino;
		} catch (Exception e) {
			return null;
		}
	}

	public static List<String> convertMp3ToFlac(List<String> nomesArquivosAudio) {
		return convertMp3To(nomesArquivosAudio, "flac");
	}

	public static List<String> convertMp3ToWav(List<String> nomesArquivosAudio) {
		return convertMp3To(nomesArquivosAudio, "wav");
	}
	
	public static List<String> convertMp3To(List<String> nomesArquivosAudio, String outFormat) {
		// ffmpeg -y -i audio_dividido_000.mp3 -c:a flac audio_dividido_000.flac
		List<String> result = new ArrayList<>();
		nomesArquivosAudio.forEach(arquivo -> {
			
			String nomeArquivoConvertido = arquivo + "." + outFormat;
			StringBuilder comando = new StringBuilder();
			comando.append(FFMPEG);
			comando.append(" -y ");
			comando.append(" -i " + arquivo);
			comando.append(" -c:a " + outFormat + " " + nomeArquivoConvertido);

			Utils.executarComando(comando.toString());
			
			result.add(nomeArquivoConvertido);
			
		});
		return result;
	}


}
