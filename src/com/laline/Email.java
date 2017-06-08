package com.laline;

import javax.mail.*;
import javax.mail.internet.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Email {

    private String to;
    private String bcc;
    private String store;
    private String username;
    private String password;
    private String from;
    private Transaction transaction;
    private List<LineItem> lineItems;
    private Connection con;
    private int worksheetId;



    public Email(Connection con,String t, String f, String bcc, int store, String u, String p, Transaction transaction, int worksheetId){
        this.to = t;
        this.from = f;
        this.bcc = bcc;
        if(store == 2) {
            this.store = "Pier";
        }
        else if(store == 3) {
            this.store = "Jefferson";
        }
        else if(store == 4) {
            this.store = "Chestnut";
        }
        else if(store == 1) {
            this.store = "Warehouse";
        }
        this.username = u;
        this.password = p;
        this.transaction = transaction;
        this.lineItems = transaction.getLineItems();
        this.con = con;
        this.worksheetId = worksheetId;
    }

    //Sends email containing SQL query results
    public void eSend() throws SQLException {

        String subject = "Order " + transaction.getTransactionNumber() + " from " + store;
        //Builds HTML for the Customer Info cell in table
        StringBuilder html = new StringBuilder();
        String text = "";
        html.append("<table width=\"100%\" border=\"1\" align=\"center\">\n" +
                "<tr>\n" +
                "<td align=\"left\">" + transaction.getCustomerName() + "<br> \n");

        //Only adds Company if it is present
        if(transaction.getCustomerCompany() != null) {
            if(!(transaction.getCustomerCompany().equals(""))) {
                html.append(transaction.getCustomerCompany() + "<br>\n");
            }
        }

        html.append(transaction.getCustomerAddress() + "<br>\n");

        html.append("</td>\n" +
                "<td><b>Phone Number:</b> " + transaction.getCustomerPhone()  + "<br>\n" +
                "<b>Email Address:</b> " + transaction.getCustomerEmail() + "</td>" +
                "<td></td>\n" +
                "<td></td>\n" +
                "<td><b>Tender Information:</b> <br>\n");

        for(int i = 0; i < transaction.getTenders().size(); i++) {
            html.append(transaction.getTenders().get(i).getTenderType() + ": $" + String.format("%.2f", transaction.getTenders().get(i).getTenderAmount()));
        }


        html.append("</td>\n" +
                "<td align=\"right\"><b>Store:</b> " + this.store + "<br>\n" +
                "<b>Transaction Number:</b> " + transaction.getTransactionNumber() + "<br>\n" +
                "<b>Transaction Time:</b> " + transaction.getTransactionTime() + "<br>\n" +
                "<b>Transfer Number: </b> " + this.worksheetId + "<br>\n");
        if(!transaction.getComment().isEmpty()) {
            html.append("<b>Comment: </b> " + transaction.getComment());
        }
        html.append(" </td> <tr>\n" +
                "<th>ItemLookupCode</th>\n" +
                "<th>Description</th>\n" +
                "<th>Price</th>\n" +
                "<th>Quantity</th>\n" +
                "<th>Tax</th>" +
                "<th>Location</th>\n" +
                "</tr>");


        //Adds HTML to add each line item to email table
        for(int i = 0; i < lineItems.size(); i++) {
            String location;
            if(lineItems.get(i).getIlc().equals("BAG")) {
                location = "";
            }
            else if(lineItems.get(i).getWhQty() < lineItems.get(i).getQty()) {
                location = this.store;
            }
            else if(lineItems.get(i).getComment().toLowerCase().matches("cc(.*)")){
                location = "Customer Took";
            }
            else {
                location = "Warehouse";
            }
            html.append("<tr>\n" +
                    "<td>" + lineItems.get(i).getIlc() + "</td>\n" +
                    "<td>" + lineItems.get(i).getDescription() + "</td>\n" +
                    "<td>" + "$" + String.format("%.2f", lineItems.get(i).getPrice()) + "</td>\n" +
                    "<td>" + (int) lineItems.get(i).getQty() + "</td>\n" +
                    "<td>" + "$" + String.format("%.2f", lineItems.get(i).getSalesTax()) + "</td>\n" +
                    "<td>" + location + "</td>\n" +
                    "</tr>");

        }

        //Calculates totals for Transaction
        double transTotal = 0.00;
        double transTaxTotal = 0.00;
        for(int i = 0; i < lineItems.size(); i++) {
            transTotal += lineItems.get(i).getPrice();
            transTaxTotal += lineItems.get(i).getSalesTax();
        }

        transTotal = round(transTotal, 2);
        transTaxTotal = round(transTaxTotal, 2);

        html.append("<tr>\n" +
                "<td></td>\n" +
                "<td align=\"right\"><b>Subtotal:</b></td>\n" +
                "<td>" + "$" + String.format("%.2f", transTotal) + "</td>\n" +
                "<td align=\"right\"><b>Tax Total:</b></td>\n" +
                "<td>" + "$" + String.format("%.2f", transTaxTotal) + "</td>\n" +
                "<td>Shipping Charge: $" + String.format("%.2f", transaction.getShippingCharge()) + "</td>\n" +
                "</tr>");

        //Email account server settings
        Properties props = new Properties();
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtpout.secureserver.net");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        //Composes the email and sends it using above information
        try {

            Message message = new MimeMessage(session);
            Multipart multipart  = new MimeMultipart("alternative");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText( subject, "utf-8" );

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent( html.toString(), "text/html; charset=utf-8" );

            multipart.addBodyPart( textPart );
            multipart.addBodyPart( htmlPart );

            message.setFrom(new InternetAddress(this.from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(this.to));
            message.setRecipients(Message.RecipientType.BCC,
                    InternetAddress.parse(this.bcc));
            message.setSubject(subject);
            message.setContent( multipart );
            message.saveChanges();

            Transport.send(message);

            System.out.println("Done - Order");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    //Rounds double value int number of decimal places
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}