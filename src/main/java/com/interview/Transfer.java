package com.interview;

/**
 * Created by algotrader on 5/10/17.
 *
 * I need an object to describe a transfer
 */
public class Transfer {

    private Account source;
    private Account destination;
    private Integer amount;

    public Account getSource() {
        return source;
    }
    public void setSource(Account source) {
        this.source = source;
    }
    public Account getDestination() {
        return destination;
    }
    public void setDestination(Account destination) {
        this.destination = destination;
    }
    public Integer getAmount() {
        return amount;
    }
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
