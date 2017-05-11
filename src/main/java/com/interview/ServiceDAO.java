package com.interview;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.List;

/**
 * http://www.dropwizard.io/1.0.6/docs/manual/jdbi.html
 * http://jdbi.org/dbi_handle_and_statement/
 */


public class ServiceDAO {

    private DBI dbi;

    ServiceDAO(DBI dbi) {
        this.dbi = dbi;
    }


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

    public String getPostgresVersion() {
        try (Handle h = dbi.open()) {
            return "\"" + h.createQuery("SELECT version();").first().toString() + "\"";
        }
    }

    public Integer getAccountBalance(String id) {
        Account result = getAccount(id);
        if(result == null) return 0; // Older Java may have required Integer object here.
        return result.getBalance();
    }

    public Integer setAccountBalance(String id, Integer balance) {
        try (Handle h = dbi.open()) {
            return h.createStatement("insert into accounts (id, balance) values (:id, :balance1)" +
                    "ON CONFLICT (id) DO UPDATE SET balance = :balance2;")
                    .bind("id", id)
                    .bind("balance1", balance)
                    .bind("balance2", balance)
                    .execute();
        }
    }

    public Integer addValueAccountBalance(String id, Integer balance) {
        return balance;
    }

}
