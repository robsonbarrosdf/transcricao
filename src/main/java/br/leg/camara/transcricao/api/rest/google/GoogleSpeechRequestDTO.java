package br.leg.camara.transcricao.api.rest.google;

import lombok.Data;

@Data
public class GoogleSpeechRequestDTO {
	private Config config;
	private Audio audio;
}
