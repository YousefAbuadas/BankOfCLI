package com.example.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.BankofCLI.models.User;
import com.example.JWT.JwtUtil;
import com.example.service.AuthService;
import com.example.service.contract.BankContracts.AuthResponse;
import com.example.service.contract.BankContracts.LoginRequest;
import com.example.service.contract.BankContracts.RegisterRequest;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:5173"}, allowedHeaders = "*")
public class AuthController {
	
	private final JwtUtil jwt;
	private final AuthService auth;
	

	public AuthController(JwtUtil jwt, AuthService auth) {
		this.jwt = jwt;
		this.auth = auth;
	}

	@PostMapping({"", "/login"})
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest dto){
		
		AuthResponse loggedin = auth.login(dto);
		
		return ResponseEntity.ok(loggedin);
	}
	
	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest newUser){
		
		AuthResponse registerd = auth.register(newUser);;
		
		return ResponseEntity.status(201).body(registerd);
	}
	
	
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e){
        
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
