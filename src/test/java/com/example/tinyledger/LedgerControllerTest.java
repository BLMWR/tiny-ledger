package com.example.tinyledger;

import com.example.tinyledger.service.LedgerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE = "/accounts/" + LedgerService.DEFAULT_ACCOUNT_ID;

    @Test
    void shouldReturnZeroBalance_whenAccountIsNew() throws Exception {
        mockMvc.perform(get(BASE + "/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(LedgerService.DEFAULT_ACCOUNT_ID))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void shouldReturn201WithTransaction_whenDepositIsMade() throws Exception {
        mockMvc.perform(post(BASE + "/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 250.00, "description": "paycheck" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.balanceAfter").value(250.00))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldReturn201WithUpdatedBalance_whenWithdrawalIsMade() throws Exception {
        mockMvc.perform(post(BASE + "/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 500.00 }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE + "/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 200.00, "description": "groceries" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.balanceAfter").value(300.00));
    }

    @Test
    void shouldReturn422_whenWithdrawalExceedsBalance() throws Exception {
        mockMvc.perform(post(BASE + "/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 1.00 }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturn400_whenDepositIsMissingAmount() throws Exception {
        mockMvc.perform(post(BASE + "/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "description": "no amount here" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnAllTransactionsInOrder_whenHistoryIsRequested() throws Exception {
        mockMvc.perform(post(BASE + "/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 100.00 }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE + "/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 40.00 }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"));
    }

    @Test
    void shouldReturn404_whenAccountDoesNotExist() throws Exception {
        mockMvc.perform(get("/accounts/unknown-account/balance"))
                .andExpect(status().isNotFound());
    }
}
