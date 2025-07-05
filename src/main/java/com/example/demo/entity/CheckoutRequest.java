package com.example.demo.entity;

public class CheckoutRequest {
    private int numAdults;
    private int numChildren;
    private int numSeniors;

    public int getNumAdults() {
        return numAdults;
    }

    public void setNumAdults(int numAdults) {
        this.numAdults = numAdults;
    }

    public int getNumChildren() {
        return numChildren;
    }

    public void setNumChildren(int numChildren) {
        this.numChildren = numChildren;
    }

    public int getNumSeniors() {
        return numSeniors;
    }

    public void setNumSeniors(int numSeniors) {
        this.numSeniors = numSeniors;
    }
}