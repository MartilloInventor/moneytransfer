package com.interview;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * http://www.dropwizard.io/1.0.6/docs/manual/jdbi.html
 * http://jdbi.org/dbi_handle_and_statement/
 */


public class ServiceDAO {
    private DBI dbi;
    private ExecutorService executor;

    ServiceDAO(DBI dbi) {
        this.dbi = dbi;
        this.executor = Executors.newCachedThreadPool();
    }

    //  We should really use try with resources instead of try + finally

    public List<Account> getAllAccounts() {
        try (Handle h = dbi.open()) {
            return h.createQuery( "SELECT * FROM accounts;" )
                    .mapTo( Account.class )
                    .list();
        }
    }

    public Account getAccount(String id) {
        try (Handle h = dbi.open()) {
            return h.createQuery( "SELECT * FROM accounts " +
                    " WHERE id=:id;" )
                    .bind( "id", id )
                    .mapTo( Account.class )
                    .first();
        }
    }

    /* in case we see obscure behavior from older version of postgres */
    public String getPostgresVersion() {
        try (Handle h = dbi.open()) {
            return "\"" + h.createQuery( "SELECT version();" ).first().toString() + "\"";
        }
    }

    public Integer getAccountBalance(String id) {
        Account result = getAccount( id );
        if (result == null) return 0; // Older Java may have required Integer object here.
        return result.getBalance();
    }

    // resets an account or opens one. balance can be zero but not negative after execution
    public Integer setAccountBalance(String id, Integer balance) {
        if (balance < 0) {
            return 0;
        }
        try (Handle h = dbi.open()) {
            return h.createStatement( "INSERT INTO accounts (id, balance) VALUES (:id, :balance1)" +
                    "ON CONFLICT (id) DO UPDATE SET balance = :balance2;" )
                    .bind( "id", id )
                    .bind( "balance1", balance )
                    .bind( "balance2", balance )
                    .execute();
        }
    }

    /* allows account to be zeroed */
    public Integer addToAccountBalance(String id, Integer amount) {
        if (amount == 0) {
            return 0;
        }
        try (Handle h = dbi.open()) {
            return h.createStatement( "UPDATE accounts SET balance = balance + :amount1 " +
                    "WHERE (id = :id) AND ((balance + :amount2) >= 0 );" )
                    .bind( "amount1", amount )
                    .bind( "id", id )
                    .bind( "amount2", amount )
                    .execute();
        }
    }

    /* only transferring from src to dst. I could change that. Src & dst must exist. */
    public String makeTransfer(String srcid, String dstid, Integer amount) {
        // 0 or negative transfer disallowed.
        if (amount <= 0) {
            return "\"Failed\"";
        }
        try (Handle h = dbi.open()) {
            if (h.createStatement( "UPDATE accounts SET balance = balance + :amount1 " +
                    "WHERE (id = :id) AND ((balance + :amount2) >= 0 );" )
                    .bind( "amount1", -amount )
                    .bind( "id", srcid )
                    .bind( "amount2", -amount )
                    .execute() == 0) {
                return "\"Failed\"";
            }
            if (h.createStatement( "UPDATE accounts SET balance = balance + :amount1 " +
                    "WHERE (id = :id) AND ((balance + :amount2) > 0 );" )
                    .bind( "amount1", amount )
                    .bind( "id", dstid )
                    .bind( "amount2", amount )
                    .execute() == 0) {
                // put the money back -- maybe should check for existence first
                // maybe should change this one to upsert
                h.createStatement( "UPDATE accounts SET balance = balance + :amount1 " +
                        "WHERE (id = :id) AND ((balance + :amount2) > 0 );" )
                        .bind( "amount1", amount )
                        .bind( "id", srcid )
                        .bind( "amount2", amount );
                return "\"Failed\"";
            }
        }
        return "\"Succeeeded\"";
    }

    public String setAccountBalanceV2(String id, Integer balance) {
        executor.execute(new RunSetAccountBalance(id, balance));
        return "In process";
    }

    public String addToAccountBalanceV2(String id, Integer amount) {
        executor.execute(new RunAddToBalance(id, amount));
        return "In process";
    }

    public String makeTransferV2(String srcid, String dstid, Integer amount) {
        executor.execute(new RunMakeTransfer(srcid, dstid, amount));
        return "In process";
    }

    // not static so that outer methods can be directly accessed
    private class RunAddToBalance implements Runnable {
        private String id;
        private Integer amount;

        public RunAddToBalance(String id, Integer amount) {
            this.id = id;
            this.amount = amount;
        }

        @Override
        public void run() {
            // save transaction
            addToAccountBalance(id, amount);
        }

    }

    private class RunSetAccountBalance implements Runnable {
        private String id;
        private Integer amount;

        public RunSetAccountBalance(String id, Integer amount) {
            this.id = id;
            this.amount = amount;
        }

        @Override
        public void run() {
            // save transaction
            setAccountBalance(id, amount);

        }

    }

    private class RunMakeTransfer implements Runnable {
        private String srcid;
        private String dstid;
        private Integer amount;

        public RunMakeTransfer(String srcid, String dstid, Integer amount) {
            this.srcid = srcid;
            this.dstid = dstid;
            this.amount = amount;
        }

        @Override
        public void run() {
            // save transaction
            makeTransfer(srcid, dstid, amount);
        }

    }
}