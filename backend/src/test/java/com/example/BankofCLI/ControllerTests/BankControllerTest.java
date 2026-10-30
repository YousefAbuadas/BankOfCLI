package com.example.BankofCLI.ControllerTests;

import com.example.JWT.JwtUtil;
import com.example.controllers.AccountController;
import com.example.controllers.AuthController;
import com.example.controllers.TransactionController;
import com.example.service.AccountService;
import com.example.service.AuthService;
import com.example.service.TransactionService;
import com.example.service.contract.BankContracts.AccountResponse;
import com.example.service.contract.BankContracts.AccountType;
import com.example.service.contract.BankContracts.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static com.example.service.contract.BankContracts.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BankControllerTest {
    MockMvc mvc;
    @Mock AuthService authService;
    @Mock AccountService accountService;
    @Mock TransactionService transactionService;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthController authController;
    @InjectMocks AccountController accountController;
    @InjectMocks TransactionController transactionController;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(authController, accountController, transactionController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();
    }

    @Test
    void registerReturnsCreatedResponse() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("user-1", "Ada", "Lovelace", "ada@example.com", "signed-token"));

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","password":"secret"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.token").value("signed-token"));
        verify(authService).register(new RegisterRequest("Ada", "Lovelace", "ada@example.com", "secret"));
    }

    @Test
    void accountsReturnsServiceResponse() throws Exception {
        when(accountService.getAccounts()).thenReturn(List.of(new AccountResponse(
                "account-1", new UserReference("user-1"), "USD", new BigDecimal("25.50"),
                AccountType.CHECKING, "Everyday Checking")));

        mvc.perform(get("/user/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value("account-1"))
                .andExpect(jsonPath("$[0].accountBalance").value(25.50));
    }

    @Test
    void depositPassesPathAndAmountToService() throws Exception {
        mvc.perform(put("/transaction/deposit/account-1").param("amount", "12.34"))
                .andExpect(status().isNoContent());
        verify(transactionService).deposit("account-1", new BigDecimal("12.34"));
    }

    @Test
    void invalidDepositAmountIsBadRequest() throws Exception {
        mvc.perform(put("/transaction/deposit/account-1").param("amount", "invalid"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);
    }
}
