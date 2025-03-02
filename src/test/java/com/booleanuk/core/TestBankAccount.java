package com.booleanuk.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class TestBankAccount {
    @Test
    public void newAccountInstancesAreValid() {
        BankAccount currentAccount = new CurrentAccount(999999);
        assertEquals(8, currentAccount.getAccountNumber().length());
        assertEquals(6, currentAccount.getBranchNumber().length());
        assertEquals(0, currentAccount.getBalance());
        assertEquals(0, currentAccount.getTransactions().size());
    }
    @Test
    public void withdrawTooMuchShouldFail() {
        BankAccount currentAccount = new CurrentAccount();
        double amount = 1000;
        assertFalse(currentAccount.withdraw(amount));
    }

    @Test
    public void canWithdrawWithinReason() {
        BankAccount account = new CurrentAccount();
        account.deposit(1000);
        assertTrue(account.withdraw(500));
    }

    @Test
    public void withdrawingRemovesMoney() {
        BankAccount account = new CurrentAccount();
        account.deposit(1000);
        assertTrue(account.withdraw(500));
        assertEquals(500, account.getBalance());
    }
    @Test
    public void withdrawingRemovesMoneyOnlyIfValidAmount() {
        BankAccount account = new CurrentAccount();
        account.deposit(1000);
        assertFalse(account.withdraw(2000));
        assertEquals(1000, account.getBalance());
    }

    @Test
    public void depositAddsTheRightAmount() {
        BankAccount account = new CurrentAccount();
        assertTrue(account.deposit(1000));
        assertEquals(1000, account.getBalance());
    }

    @Test
    public void transactionsAreStored() {
        BankAccount account = new CurrentAccount();
        assertTrue(account.deposit(1000));
        assertTrue(account.withdraw(100));
        assertEquals(2, account.getTransactions().size());
    }

    @Test
    public void transactionsAreStoredCorrectly() {
        BankAccount account = new CurrentAccount();
        LocalDateTime ldt = LocalDateTime.now();
        assertTrue(account.deposit(1000));
        assertTrue(account.withdraw(100));
        assertEquals(2, account.getTransactions().size());
        LocalDateTime depTime = account.getTransactions().getFirst().getDateTime();
        LocalDateTime wdrTime = account.getTransactions().getLast().getDateTime();
        // deposit time correct within 1 s
        assertEquals(ldt.toLocalDate(), depTime.toLocalDate());
        assertEquals(ldt.getHour(), depTime.getHour());
        assertEquals(ldt.getMinute(), depTime.getMinute());
        assertEquals(ldt.getSecond(), depTime.getSecond());
        // withdraw time correct within 1 s
        assertEquals(ldt.toLocalDate(), wdrTime.toLocalDate());
        assertEquals(ldt.getHour(), wdrTime.getHour());
        assertEquals(ldt.getMinute(), wdrTime.getMinute());
        assertEquals(ldt.getSecond(), wdrTime.getSecond());
        // deposit amounts correct
        Transaction dep = account.getTransactions().getFirst();
        assertEquals(1000, dep.getAmount());
        assertEquals(1000, dep.getCurrentBalance());
        // withdraw amounts correct
        Transaction wdr = account.getTransactions().getLast();
        assertEquals(-100, wdr.getAmount());
        assertEquals(900, wdr.getCurrentBalance());
    }

    @Test
    public void generateStatementOnNoTransactions() {
        BankAccount account = new CurrentAccount();
        assertEquals(
                "date||credit||debit||balance",
                account.generateBankStatement().replaceAll("\\s+",""));
    }

    @Test
    public void generateStatementOnSomeTransactions() {
        BankAccount account = new CurrentAccount();
        LocalDateTime ldt1 = LocalDateTime.of(2012, 1, 14, 13, 54);
        LocalDateTime ldt2 = LocalDateTime.of(2012, 1, 13, 22, 18);
        LocalDateTime ldt3 = LocalDateTime.of(2012, 1, 10, 11, 27);
        account.deposit(1000, ldt3);
        account.withdraw(100, ldt2);
        account.withdraw(200, ldt1);
//        System.out.println(account.generateBankStatement());

        String sb = "date||credit||debit||balance" +
                "14/1/2012" +
                "||||200.00||700.00" +
                "13/1/2012" +
                "||||100.00||900.00" +
                "10/1/2012" +
                "||1000.00||||1000.00".replaceAll("\\s+","");

        assertEquals(sb, account.generateBankStatement().replaceAll("\\s+",""));
    }

    @Test
    public void overdraftNeedsToBePositive() {
        SavingsAccount account = new SavingsAccount(523611);
        assertFalse(account.setMaxOverdraft(-50));
    }

    @Test
    public void cannotGoBeyondOverdraftLimit() {
        SavingsAccount account = new SavingsAccount(123456);
        account.deposit(100);
        assertFalse(account.withdraw(200));
        assertEquals(100, account.getBalance());
    }

    @Test
    public void canHaveNegativeBalanceWithinOverdraft() {
        SavingsAccount account = new SavingsAccount(123456);
        account.deposit(100);
        account.setMaxOverdraft(200);
        assertTrue(account.withdraw(200));
        assertEquals(-100, account.getBalance());
    }
}
