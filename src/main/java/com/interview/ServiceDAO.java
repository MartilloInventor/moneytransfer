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


    List<Account> getAllAccounts() {
        try (Handle h = dbi.open()) {
            return h.createQuery( "SELECT * FROM accounts;" )
                    .mapTo( Account.class )
                    .list();
        }
    }

    Account getAccount(String id) {
        try (Handle h = dbi.open()) {
            return h.createQuery( "SELECT * FROM accounts " +
                    " WHERE id=:id;" )
                    .bind( "id", id )
                    .mapTo( Account.class )
                    .first();
        }
    }

    public Integer getAccountBalance(String id) {
        Account result = getAccount(id);
        if(result == null) return 0; // Older Java may have required Integer object here.
        return result.getBalance();
    }

    public Integer setAccountBalance(String id, Integer balance) {
        return balance;
    }

    public Integer addValueAccountBalance(String id, Integer balance) {
        return balance;
    }

}
