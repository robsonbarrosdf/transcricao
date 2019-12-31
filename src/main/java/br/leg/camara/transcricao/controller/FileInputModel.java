package br.leg.camara.transcricao.controller;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class FileInputModel {

	private MultipartFile file;
	private String api;
	
}
