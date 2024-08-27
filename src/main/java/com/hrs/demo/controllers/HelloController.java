package com.hrs.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloController {

    @RequestMapping("/")
    String hello() {
        return "Hello World, Spring Boot!";
    }

	@GetMapping("/reverse/{name}")
	public String reverse(@PathVariable("name") String name) {
		String reversed = null;
		return "Reversed " + reversed;
	}
	
	@PostMapping("/process/")
	public String process(@RequestBody String message) {
		String processedMessage = message;
		return "Processed " + processedMessage;
	}
	
	@GetMapping("/sort/{input}")
	public String sort(@PathVariable("input") String input) {
		String sorted = null;
		return "Characters in alphabetical order " + sorted;
	}
}

