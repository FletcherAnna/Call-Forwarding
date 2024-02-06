package com.e.callforwarding;

public class Contact {
    String contactName;
    String contactNumber;
    public Contact(String name, String number) {
        contactName = name;
        contactNumber = number;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    @Override
    public String toString() {
        return contactName + "-" + contactNumber;
    }
}
