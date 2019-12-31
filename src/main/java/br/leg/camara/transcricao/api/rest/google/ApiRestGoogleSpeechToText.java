package br.leg.camara.transcricao.api.rest.google;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.leg.camara.transcricao.api.ITranscricaoApi;
import br.leg.camara.transcricao.utils.MP3Utils;
import br.leg.camara.transcricao.utils.Utils;

@Service
public class ApiRestGoogleSpeechToText implements ITranscricaoApi {
	
	@Value("${google.apikey}")
	private String APIKEY;
	
	private final RestTemplate restTemplate;
	
	public ApiRestGoogleSpeechToText(RestTemplateBuilder restTemplateBuilder) {
		restTemplate = restTemplateBuilder.build();
	}
	
	@Override
	public String getName() {
		return "google";
	}

    @Async
    @Override
	public CompletableFuture<String> transcrever(String nomeArquivo) throws IOException {
		
		String url = "https://speech.googleapis.com/v1/speech:recognize?key=" + APIKEY;
		
		// Configura filtro e converte objeto para json
		Gson gson = new GsonBuilder().create();
		
		Config config = new Config();
		config.setEncoding("FLAC");
		config.setLanguageCode("pt-BR");
		config.setMaxAlternatives(1);
		config.setProfanityFilter(true);
		
		Audio audio = new Audio();
		audio.setContent(Utils.encodeFileToBase64Binary(nomeArquivo));
		
		GoogleSpeechRequestDTO gsr = new GoogleSpeechRequestDTO();
		gsr.setConfig(config);
		gsr.setAudio(audio);
		String sgsr = gson.toJson(gsr);				
		
		// Configura cabecalho e dados da requisição post
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);		
		HttpEntity<String> postRequest = new HttpEntity<>(sgsr, headers);
		
		String transcricaoJSON = restTemplate.postForObject(url, postRequest, String.class);
		GoogleSpeechResponseDTO response = Utils.json2Object(transcricaoJSON, GoogleSpeechResponseDTO.class);
		
//		GoogleSpeechResponseDTO response = null;
//		try {
//			Thread.sleep(1500);
//			System.out.println(new Date());
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		if (response!=null && !ObjectUtils.isEmpty(response.getResults()) && !ObjectUtils.isEmpty(response.getResults().get(0).getAlternatives()))
			return CompletableFuture.completedFuture(response.getResults().get(0).getAlternatives().get(0).getTranscript());
		else 
			return CompletableFuture.completedFuture("");
		
	}

	@Override
	public List<String> converterArquivosMp3(List<String> trechosAudio) {
		return MP3Utils.convertMp3ToFlac(trechosAudio);
	}

}
