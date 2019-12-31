package br.leg.camara.transcricao.api.rest.google;

import lombok.Data;

@Data
public class Alternative {

	private String transcript;
	private Float confidence;
	
}
