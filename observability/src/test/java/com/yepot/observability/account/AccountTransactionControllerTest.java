package com.yepot.observability.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yepot.observability.account.domain.Account;
import com.yepot.observability.account.dto.request.AmountRequest;
import com.yepot.observability.account.repository.AccountRepository;
import com.yepot.observability.account.repository.AccountTransactionRepository;
import com.yepot.observability.transfer.dto.request.TransferRequest;
import com.yepot.observability.transfer.repository.TransferTransactionRepository;
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
class AccountTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private TransferTransactionRepository transferTransactionRepository;

    @AfterEach
    void tearDown() {
        transferTransactionRepository.deleteAll();
        accountTransactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void depositReturnsUpdatedBalance() throws Exception {
        Account account = saveAccount("양은서", 100000L, "100-0000-0001");
        AmountRequest request = new AmountRequest(50000L);

        mockMvc.perform(post("/accounts/{accountId}/deposit", account.getAccountId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(account.getAccountId()))
            .andExpect(jsonPath("$.balance").value(150000))
            .andExpect(jsonPath("$.transactionId").isNumber())
            .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void depositWithNonPositiveAmountReturnsBadRequest() throws Exception {
        Account account = saveAccount("양은서", 100000L, "100-0000-0001");
        AmountRequest request = new AmountRequest(0L);

        mockMvc.perform(post("/accounts/{accountId}/deposit", account.getAccountId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Illegal Argument"))
            .andExpect(jsonPath("$.message").value("입금 금액은 0원보다 커야 합니다."))
            .andExpect(jsonPath("$.path").value("/accounts/" + account.getAccountId() + "/deposit"));
    }

    @Test
    void withdrawReturnsUpdatedBalance() throws Exception {
        Account account = saveAccount("양은서", 100000L, "100-0000-0001");
        AmountRequest request = new AmountRequest(30000L);

        mockMvc.perform(post("/accounts/{accountId}/withdraw", account.getAccountId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(account.getAccountId()))
            .andExpect(jsonPath("$.balance").value(70000))
            .andExpect(jsonPath("$.transactionId").isNumber())
            .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void withdrawWithInsufficientBalanceReturnsBadRequest() throws Exception {
        Account account = saveAccount("양은서", 10000L, "100-0000-0001");
        AmountRequest request = new AmountRequest(30000L);

        mockMvc.perform(post("/accounts/{accountId}/withdraw", account.getAccountId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Illegal Argument"))
            .andExpect(jsonPath("$.message").value("잔액이 부족합니다."))
            .andExpect(jsonPath("$.path").value("/accounts/" + account.getAccountId() + "/withdraw"));
    }

    @Test
    void transferReturnsSuccessAndUpdatesBalances() throws Exception {
        Account fromAccount = saveAccount("양은서", 100000L, "100-0000-0001");
        Account toAccount = saveAccount("김예봄", 50000L, "100-0000-0002");
        TransferRequest request = new TransferRequest(fromAccount.getAccountId(), toAccount.getAccountId(), 10000L);

        mockMvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transferId").isNumber())
            .andExpect(jsonPath("$.fromAccountId").value(fromAccount.getAccountId()))
            .andExpect(jsonPath("$.toAccountId").value(toAccount.getAccountId()))
            .andExpect(jsonPath("$.amount").value(10000))
            .andExpect(jsonPath("$.status").value("SUCCESS"));

        Account updatedFromAccount = accountRepository.findById(fromAccount.getAccountId()).orElseThrow();
        Account updatedToAccount = accountRepository.findById(toAccount.getAccountId()).orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(90000L, updatedFromAccount.getBalance());
        org.junit.jupiter.api.Assertions.assertEquals(60000L, updatedToAccount.getBalance());
    }

    @Test
    void transferWithInsufficientBalanceReturnsBadRequest() throws Exception {
        Account fromAccount = saveAccount("양은서", 5000L, "100-0000-0001");
        Account toAccount = saveAccount("김예봄", 50000L, "100-0000-0002");
        TransferRequest request = new TransferRequest(fromAccount.getAccountId(), toAccount.getAccountId(), 10000L);

        mockMvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Illegal Argument"))
            .andExpect(jsonPath("$.message").value("출금 계좌의 잔액이 부족합니다."))
            .andExpect(jsonPath("$.path").value("/transfers"));
    }

    @Test
    void getTransactionsReturnsAccountTransactionHistory() throws Exception {
        Account fromAccount = saveAccount("양은서", 100000L, "100-0000-0001");
        Account toAccount = saveAccount("김예봄", 50000L, "100-0000-0002");

        mockMvc.perform(post("/accounts/{accountId}/deposit", fromAccount.getAccountId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AmountRequest(50000L))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new TransferRequest(fromAccount.getAccountId(), toAccount.getAccountId(), 10000L)
                )))
            .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                "/accounts/{accountId}/transactions", fromAccount.getAccountId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(fromAccount.getAccountId()))
            .andExpect(jsonPath("$.transactions[0].transactionId").isNumber())
            .andExpect(jsonPath("$.transactions[0].type").value("DEPOSIT"))
            .andExpect(jsonPath("$.transactions[0].amount").value(50000))
            .andExpect(jsonPath("$.transactions[0].status").value("SUCCESS"))
            .andExpect(jsonPath("$.transactions[0].createdAt").exists())
            .andExpect(jsonPath("$.transactions[1].transactionId").isNumber())
            .andExpect(jsonPath("$.transactions[1].type").value("TRANSFER"))
            .andExpect(jsonPath("$.transactions[1].amount").value(10000))
            .andExpect(jsonPath("$.transactions[1].status").value("SUCCESS"))
            .andExpect(jsonPath("$.transactions[1].createdAt").exists());
    }

    @Test
    void getTransactionsWithUnknownAccountReturnsNotFound() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                "/accounts/{accountId}/transactions", 99999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 계좌입니다."))
            .andExpect(jsonPath("$.path").value("/accounts/99999/transactions"));
    }

    private Account saveAccount(String userName, Long balance, String accountNumber) {
        Account account = accountRepository.save(new Account(userName, balance));
        account.updateAccountNumber(accountNumber);
        return accountRepository.save(account);
    }
}
