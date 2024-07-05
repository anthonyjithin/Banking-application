package net.javaguides.banking.service.impl;

import net.javaguides.banking.dto.AccountDto;
import net.javaguides.banking.dto.TransactionDto;
import net.javaguides.banking.dto.TransferFundDto;
import net.javaguides.banking.entity.Account;
import net.javaguides.banking.entity.Transaction;
import net.javaguides.banking.exception.AccountException;
import net.javaguides.banking.mapper.AccountMapper;
import net.javaguides.banking.repository.AccountRepository;
import net.javaguides.banking.repository.TransactionRepository;
import net.javaguides.banking.service.AccountService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    //dependency injection
    private AccountRepository accountRepository;

    //inject Transaction repository
    private TransactionRepository transactionRepository;

    private static final String TRANSACTION_TYPE_DEPOSIT = "DEPOSIT";
    private static final String TRANSACTION_TYPE_WITHDRAW = "WITHDRAW";
    private static final String TRANSACTION_TYPE_FROM_TRANSFER = "DEBIT";
    private static final String TRANSACTION_TYPE_TO_TRANSFER = "CREDIT";

    public AccountServiceImpl(AccountRepository accountRepository,
                              TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    //method saves the account to database using account mapper and account Dto
    public AccountDto createAccount(AccountDto accountDto) {
        Account account = AccountMapper.mapToAccount(accountDto);
        Account savedAccount = accountRepository.save(account);
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountException("Account not found"));
        return AccountMapper.mapToAccountDto(account);
    }

    @Override
    public AccountDto deposit(Long id, double amount) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountException("Account not found"));
        double total = account.getBalance() + amount;
        account.setBalance(total);

        Account savedAccount = accountRepository.save(account);
        Transaction transaction = new Transaction();
        transaction.setAccountId(account.getId());
        transaction.setAmount(amount);
        transaction.setTransactionType(TRANSACTION_TYPE_DEPOSIT);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);

        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto withdraw(Long id, double amount) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountException("Account not found"));
        if(account.getBalance() < amount){
            throw  new RuntimeException("Insufficient Balance in account");
        }
        double total = account.getBalance()-amount;
        account.setBalance(total);
        Account savedAccount = accountRepository.save(account);
        Transaction transaction = new Transaction();
        transaction.setAccountId(account.getId());
        transaction.setAmount(amount);
        transaction.setTransactionType(TRANSACTION_TYPE_WITHDRAW);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);


        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream().map((account) -> AccountMapper.mapToAccountDto(account)).collect(Collectors.toList());
    }

    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountException("Account not found"));
        accountRepository.deleteById(id);

        }

    @Override
    public void transferFunds(TransferFundDto transferFundDto) {
        //Retrieve the account from which we send the amount
       Account fromAccount =  accountRepository
               .findById(transferFundDto.fromAccountId())
               .orElseThrow(() -> new AccountException("Account does not exist"));
        //Retrieve the account to which we send the amount
        Account toAccount = accountRepository
                .findById(transferFundDto.toAccountId())
                .orElseThrow(() -> new AccountException("Account does not exist"));
        if (fromAccount.getBalance()< transferFundDto.amount()){
            throw new RuntimeException("Insufficient Balance in account");
        }

        //Debit amount from fromAccountObject
        fromAccount.setBalance(fromAccount.getBalance() - transferFundDto.amount());

        //credit the amount to ToAccountObject
        toAccount.setBalance(toAccount.getBalance() + transferFundDto.amount());
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        //TRANSACTION DEBIT
        Transaction fromTransaction = new Transaction();
        fromTransaction.setAccountId(transferFundDto.fromAccountId());
        fromTransaction.setAmount(transferFundDto.amount());
        fromTransaction.setTransactionType(TRANSACTION_TYPE_FROM_TRANSFER);
        fromTransaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(fromTransaction);

        //TRANSACTION CREDIT
        Transaction toTransaction = new Transaction();
        toTransaction.setAccountId(transferFundDto.toAccountId());
        toTransaction.setAmount(transferFundDto.amount());
        toTransaction.setTransactionType(TRANSACTION_TYPE_TO_TRANSFER);
        toTransaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(toTransaction);
    }

    @Override
    public List<TransactionDto> getAccountTransactions(Long accountId) {
        List<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByTimestampDesc(accountId);

        return transactions.stream()
                .map((transaction) -> convertEntityToDto(transaction))
                .collect(Collectors.toList());
    }
    private TransactionDto convertEntityToDto(Transaction transaction){
        return new TransactionDto(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getTimestamp()
        );
    }
}
