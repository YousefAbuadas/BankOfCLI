package com.example.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.BankofCLI.models.Account;
import com.example.JWT.AuthUser;
import com.example.JWT.JwtUtil;
import com.example.service.AccountService;
import com.example.service.contract.BankContracts.AccountResponse;

@Controller
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173"}, allowedHeaders = "*")
public class AccountController {
	
	private final JwtUtil jwt;
	private final AccountService acctService;
	

	public AccountController(JwtUtil jwt, AccountService acctService) {
		this.jwt = jwt;
		this.acctService = acctService;
	}
	
	@GetMapping("/getAccount")
	public ResponseEntity<AccountResponse> getAccount(@RequestBody String accountId){
		
		return ResponseEntity.ok(acctService.getAccount(accountId));
	}
	
	@GetMapping("/accounts")
	public ResponseEntity<List<AccountResponse>> getAccountsByUser(@AuthenticationPrincipal AuthUser me){
		
		return ResponseEntity.ok(acctService.getAccounts());
	}
	
	
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalstate(IllegalStateException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e){
        
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
