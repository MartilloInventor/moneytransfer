package com.api;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class AccessClient {
    final static Client client = Client.create();

    public static void main(String[] args) {
        System.out.print(getInterviewVersion());
        System.out.print(getInterviewPostgresVersion());
    }

    static public String sendInterviewPing() {
        return "pong";
    }
    static public String getInterviewVersion() {
        return "todo";
    }
    static public String getInterviewPostgresVersion() {
        return "todo";
    }
    static public void setInterviewAccountBalance(String acct, int amount) {
    }
    static public void addInterviewAccountBalance(String acct, int amount) {
    }
    static public void makeTransfer(String src, String dst, int amount) {
    }

}


