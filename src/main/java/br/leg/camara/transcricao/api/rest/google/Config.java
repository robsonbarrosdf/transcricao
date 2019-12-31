package br.leg.camara.transcricao.api.rest.google;

import lombok.Data;

@Data
public class Config {
	
	private String encoding;
	// private Integer sampleRateHertz;
	private Boolean profanityFilter;
	private String languageCode;
	// private SpeechContexts speechContexts;
	private Integer maxAlternatives;
	
}
