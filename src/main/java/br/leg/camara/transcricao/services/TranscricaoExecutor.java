package br.leg.camara.transcricao.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import br.leg.camara.transcricao.api.ITranscricaoApi;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TranscricaoExecutor implements Runnable, Callable<TranscricaoExecutor> {

	private CountDownLatch latch;
	private List<String> trechosAudio;
	private ITranscricaoApi apiTranscricao;
	private List<String> result;
	private Boolean finalizado = false;
	
	public TranscricaoExecutor(CountDownLatch latch, List<String> trechosAudio, ITranscricaoApi apiTranscricao) {
		this.latch = latch;
		this.trechosAudio = trechosAudio;
		this.apiTranscricao = apiTranscricao;
	}
	
	@Override
	public void run() {
		try {
			transcrever();
			finalizado = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		latch.countDown();
	}

	@Override
	public TranscricaoExecutor call() throws Exception {
		transcrever();
		finalizado = true;
		return this;
	}
	
	public List<String> getResult() {
		return this.result;
	}
	
	public ITranscricaoApi getApiTranscricao() {
		return this.apiTranscricao;
	}
	
	public List<String> getTrechosAudio() {
		return this.trechosAudio;
	}
	
	public boolean isFinalizado() {
		return finalizado;
	}
	
	private void transcrever() throws IOException {
		Path tempFolder = Files.createTempDirectory("trascricao_" + apiTranscricao.getName());
		List<String> trechosAudioConvertidos = apiTranscricao.converterArquivosMp3(trechosAudio);
		result = transcrever(trechosAudioConvertidos);
		log.info(tempFolder.toString());
	}
	
	public List<String> transcrever(List<String> nomeArquivos) {
		final List<CompletableFuture<String>> cfTranscricoes = new ArrayList<>();
		
		nomeArquivos.forEach(nomeArquivo -> {
			try {
				// log.info("Enviando requisição trecho: " + nomeArquivo);
				cfTranscricoes.add(apiTranscricao.transcrever(nomeArquivo));
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
	

}
