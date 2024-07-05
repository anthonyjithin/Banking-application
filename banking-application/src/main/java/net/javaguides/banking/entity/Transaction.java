package net.javaguides.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id // primary key annotation
    @GeneratedValue(strategy = GenerationType.IDENTITY) //primary key strategy
    private Long id;
    private Long accountId;
    private double amount;
    private String transactionType; //deposit withdraw or transfer
    private LocalDateTime timestamp;
}
