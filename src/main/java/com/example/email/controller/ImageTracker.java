package com.example.email.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageTracker {
	@GetMapping(path = "/signal")
	public ResponseEntity<Void> signal(@RequestParam String id) {
		String s = "Got Signal " + id;
		System.out.println(s);
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("http://www.programacion.net/files/article/20160124010121_url1.jpg")).build();
	}
}