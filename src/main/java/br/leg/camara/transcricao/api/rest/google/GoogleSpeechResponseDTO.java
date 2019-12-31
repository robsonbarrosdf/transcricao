package br.leg.camara.transcricao.api.rest.google;

import java.util.List;

import lombok.Data;

@Data
public class GoogleSpeechResponseDTO {

	private List<Result> results = null;
	
}
