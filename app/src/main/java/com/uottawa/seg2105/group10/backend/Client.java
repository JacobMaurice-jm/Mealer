package com.uottawa.seg2105.group10.backend;

public class Client extends User {

    private String ccNumber, ccHolderName, expiryDate, cvc;


    public Client(String firstName, String lastName, String email, String password, String address) {
        super(firstName, lastName, email, password, address, "Client");
    }

    // Setter method for Credit Card information
    // If CC number, expiry date, and cvc are of correct format, returns true. Otherwise, returns false.
    public boolean setCC(String num, String name, String expiry, String cvc) {
        if (expiry.matches("(?:0[1-9]|1[0-2])/[0-9]{2}") &&
            (num.length() == 16) && (cvc.length() == 3)) {
            ccNumber = num;
            ccHolderName = name;
            expiryDate = expiry;
            this.cvc = cvc;
            return true;
        }
        return false;
    }
}
