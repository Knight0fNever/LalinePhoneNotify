package com.laline;

/**
 * Created by Josh on 8/8/2017.
 */
public class Customer {

    private String firstName;
    private String lastName;
    private Address address;

    public Customer() {
        this.address = new Address();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
