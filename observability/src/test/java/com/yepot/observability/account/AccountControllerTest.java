package com.yepot.observability.account;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yepot.observability.account.domain.Account;
import com.yepot.observability.account.dto.request.CreateAccountRequest;
import com.yepot.observability.account.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    void createAccountReturnsCreatedAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("양은서", 100000L);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").isNumber())
            .andExpect(jsonPath("$.accountNumber").value(matchesPattern("100-\\d{4}-\\d{4}")))
            .andExpect(jsonPath("$.userName").value("양은서"))
            .andExpect(jsonPath("$.balance").value(100000))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createAccountWithNegativeBalanceReturnsBadRequest() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("양은서", -1L);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Illegal Argument"))
            .andExpect(jsonPath("$.message").value("초기 잔액은 0원 이상이어야 합니다."))
            .andExpect(jsonPath("$.path").value("/accounts"));
    }

    @Test
    void getAccountReturnsSavedAccount() throws Exception {
        Account account = accountRepository.save(new Account("양은서", 100000L));
        account.updateAccountNumber("100-0000-0001");
        accountRepository.save(account);

        mockMvc.perform(get("/accounts/{accountId}", account.getAccountId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(account.getAccountId()))
            .andExpect(jsonPath("$.accountNumber").value("100-0000-0001"))
            .andExpect(jsonPath("$.userName").value("양은서"))
            .andExpect(jsonPath("$.balance").value(100000))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getAccountWithUnknownIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/accounts/{accountId}", 99999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 계좌입니다."))
            .andExpect(jsonPath("$.path").value("/accounts/99999"));
    }
}
