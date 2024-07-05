package net.javaguides.banking.dto;

import java.time.LocalDateTime;

public record TransactionDto(Long id, Long accountID, double amount, String transactionType, LocalDateTime timeStamp) {

}
