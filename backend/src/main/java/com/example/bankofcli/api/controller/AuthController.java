package com.example.bankofcli.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.bankofcli.infrastructure.persistence.entity.User;
import com.example.bankofcli.infrastructure.security.jwt.JwtUtil;
import com.example.bankofcli.application.service.AuthService;
import com.example.bankofcli.application.contract.BankContracts.AuthResponse;
import com.example.bankofcli.application.contract.BankContracts.LoginRequest;
import com.example.bankofcli.application.contract.BankContracts.RegisterRequest;

@RestController
@RequestMapping("/auth")
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
