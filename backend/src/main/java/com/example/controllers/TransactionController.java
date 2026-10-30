package com.example.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.JWT.JwtUtil;
import com.example.service.TransactionService;
import com.example.service.contract.BankContracts.TransactionResponse;

@Controller
@RequestMapping("/transaction")
@CrossOrigin(origins = {"http://localhost:5173"}, allowedHeaders = "*")
public class TransactionController {

	private final JwtUtil jwt;
	private final TransactionService tranService;
	
	public TransactionController(JwtUtil jwt, TransactionService tranService) {
		this.jwt = jwt;
		this.tranService = tranService;
	}
	@PutMapping("/deposit/{accountId}")
	public ResponseEntity<?> deposit(@PathVariable String accountId,@RequestParam(required = true) BigDecimal amount){
		tranService.deposit(accountId,amount);
		
		return ResponseEntity.noContent().build();
	}
	@PutMapping("/withdraw/{accountId}")
	public ResponseEntity<?> withdraw(@PathVariable String accountId,@RequestParam(required = true) BigDecimal amount){
		tranService.withdraw(accountId, amount);
		return ResponseEntity.noContent().build();
	}
	@PutMapping("/transfer/{accountId}")
	public ResponseEntity<?> transfer(@PathVariable String accountId,@RequestParam(required = true) String destinationAccountId,@RequestParam(required = true) BigDecimal amount){
		tranService.transfer(accountId, destinationAccountId, amount);
		return ResponseEntity.noContent().build();
	}
	@GetMapping("/transactions/{accountId}")
	public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable String accountId){
		
		return ResponseEntity.ok(tranService.getHistory(accountId));
	}
	
	
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e){
        
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
