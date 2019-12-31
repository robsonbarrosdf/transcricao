package br.leg.camara.transcricao.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import br.leg.camara.transcricao.services.TranscricaoService;

@RestController
public class TranscricaoController {
	
	@Autowired
	private TranscricaoService transcricaoService;
	
//	@PostMapping(value="/api/transcrever")
//	public List<String> transcrever(MultipartFile file) {
//		return transcricaoService.transcrever(file);
//	}
	
	@PostMapping(value="/api/transcrever")
	public Map<String, Object> transcrever(FileInputModel fileInputModel) {
		return transcricaoService.transcrever(fileInputModel);
	}	

}
