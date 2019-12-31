package br.leg.camara.transcricao.api;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ITranscricaoApi {

	// public List<String> transcrever(List<String> nomeArquivos);
	public CompletableFuture<String> transcrever(String nomeArquivo) throws IOException;
	public String getName();
	public List<String> converterArquivosMp3(List<String> trechosAudio);
	
}
