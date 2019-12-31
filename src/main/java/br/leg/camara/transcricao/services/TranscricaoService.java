package br.leg.camara.transcricao.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.leg.camara.transcricao.api.ITranscricaoApi;
import br.leg.camara.transcricao.api.rest.google.ApiRestGoogleSpeechToText;
import br.leg.camara.transcricao.controller.FileInputModel;
import br.leg.camara.transcricao.utils.MP3Utils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TranscricaoService {

	public Map<String, Object> transcrever(FileInputModel fileInputModel) {
		
//		ITranscricaoApi transcricaoApi = null;
//		
//		// Cria array com apis de transcrição a serem utilizadas
//		if (fileInputModel.getApi().toLowerCase().equals("amazon")) transcricaoApi = null;
//		else if (fileInputModel.getApi().toLowerCase().equals("microsoft")) transcricaoApi = null;	
//		else if (fileInputModel.getApi().toLowerCase().equals("ibm")) transcricaoApi = null;
//		else if (fileInputModel.getApi().toLowerCase().equals("google")) transcricaoApi = new ApiRestGoogleSpeechToText(new RestTemplateBuilder());
//		
//		log.info("APIs de transcrição: " + (transcricaoApi == null ? "NÃO RECONHECIDA - " : "") + fileInputModel.getApi());
//		if (transcricaoApi == null) throw new NullPointerException();
//		
//		return transcrever(fileInputModel.getFile(), transcricaoApi);
		
		try {
			return transcreverUsandoMultiplasApis(fileInputModel);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public Map<String, Object> transcreverUsandoMultiplasApis(FileInputModel fileInputModel) throws InterruptedException, IOException {
		
		List<ITranscricaoApi> transcricoes = new ArrayList<>();
		
		// Cria array com apis de transcrição a serem utilizadas
		if (fileInputModel.getApi().toLowerCase().contains("amazon")) transcricoes.add(null);
		if (fileInputModel.getApi().toLowerCase().contains("microsoft")) transcricoes.add(null);	
		if (fileInputModel.getApi().toLowerCase().contains("ibm")) transcricoes.add(null);
		if (fileInputModel.getApi().toLowerCase().contains("google")) transcricoes.add(new ApiRestGoogleSpeechToText(new RestTemplateBuilder()));
		log.info("APIs de transcrição: " + (transcricoes.isEmpty() ? "NÃO RECONHECIDA - " : "") + fileInputModel.getApi());
		if (transcricoes.isEmpty()) return null;
		
		// Criar pasta temporária, salva audio em disco e divide em trechos de, no máximo, 60 segundos
		Path tempFolder = Files.createTempDirectory("trascricao");
		String arquivoAudioOriginal = multiPartFileToDisk(fileInputModel.getFile(), tempFolder);
		List<String> trechosAudio = MP3Utils.splitMp3(arquivoAudioOriginal, tempFolder.toString(), 60);
		
		// Executa apis "simultaneamente"
		CountDownLatch latch = new CountDownLatch(transcricoes.size());
		ExecutorService executor = Executors.newFixedThreadPool(transcricoes.size());
		List<TranscricaoExecutor> executoresDeTranscricao = new ArrayList<>();
		for (ITranscricaoApi apiTranscricao : transcricoes) {
			TranscricaoExecutor te = new TranscricaoExecutor(latch, trechosAudio, apiTranscricao);
			executoresDeTranscricao.add(te);
			executor.submit((Runnable) (te));
		}

		latch.await();
		// latch.await(10, TimeUnit.SECONDS);		
				
		Map<String, Object> result = new HashMap<>();
		for (TranscricaoExecutor te : executoresDeTranscricao) {
			result.put(te.getApiTranscricao().getName(), te.getResult());
		}		
		return result;
	}
	
	public Map<String, Object> transcrever(MultipartFile file, ITranscricaoApi transcricaoApi) {
		long start = System.currentTimeMillis();
		Map<String, Object> result = new HashMap<>();
		
		try {
			Path tempFolder = Files.createTempDirectory("transcricao");
			String arquivoAudioOriginal = multiPartFileToDisk(file, tempFolder);
			
			List<String> trechosAudio = MP3Utils.splitMp3(arquivoAudioOriginal, tempFolder.toString(), 60);
			List<String> trechosAudioConvertidos = MP3Utils.convertMp3ToFlac(trechosAudio);
			
			List<String> transcricoes = transcrever(trechosAudioConvertidos, transcricaoApi);
						
			log.info(tempFolder.toString());
			
			result.put(transcricaoApi.getName(), transcricoes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		log.info("Elapsed time: " + (System.currentTimeMillis() - start));
		
		return result;
	}
	
	public List<String> transcrever(List<String> nomeArquivos, ITranscricaoApi transcricaoApi) {
		final List<CompletableFuture<String>> cfTranscricoes = new ArrayList<>();
		
		nomeArquivos.forEach(nomeArquivo -> {
			try {
				// log.info("Enviando requisição trecho: " + nomeArquivo);
				cfTranscricoes.add(transcricaoApi.transcrever(nomeArquivo));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
				
		// Aguarda a execução de todas as threads
		CompletableFuture.allOf(cfTranscricoes.toArray(new CompletableFuture[0])).join();
		
		// Coloca resultado das threads em uma lista
		List<String> transcricoes = new ArrayList<>();
		for(CompletableFuture<String> cf : cfTranscricoes) {
			try {
				transcricoes.add(cf.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		log.info("Transcrições " + transcricoes.size());
		int i = 1;
		for(String texto : transcricoes) {
			log.info("Transcrição trecho " + i++ + " - " + texto);
		}		
		
		return transcricoes;
	}

	private String multiPartFileToDisk(MultipartFile file, Path tempFolder) throws IllegalStateException, IOException {
        String orgName = file.getOriginalFilename();
        File dest = new File(tempFolder.toString(), orgName);
        file.transferTo(dest);
        return dest.getPath();
	}
}
