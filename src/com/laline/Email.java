package com.laline;

import javax.mail.*;
import javax.mail.internet.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class Email {

    private String to;
    private String bcc;
    private String store;
    private String username;
    private String password;
    private String from;
    private ShippingInfo si;
    private TransactionDetails td;
    private List<String[]> lineItems;
    private Connection con;
    private int worksheetId;



    public Email(Connection con,String t, String f, String bcc, int store, String u, String p, ShippingInfo si, TransactionDetails td, int worksheetId){
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
        this.si = si;
        this.td = td;
        this.lineItems = td.getLineItems();
        this.con = con;
        this.worksheetId = worksheetId;
    }

    //Sends email containing SQL query results
    public void eSend(List<Integer> itemLinesToDrop, List<Integer> itemLinesTook) throws SQLException {

        String subject = "Order " + td.getTransNumber() + " from " + store;
        //Builds HTML for the Customer Info cell in table
        StringBuilder html = new StringBuilder();
        String text = "";
        html.append("<table width=\"100%\" border=\"1\" align=\"center\">\n" +
                "<tr>\n" +
                "<td align=\"left\">" + si.getName() + "<br> \n");

        //Only adds Company if it is present
        if(si.getCompany() != null) {
            if(!(si.getCompany().equals(""))) {
                html.append(si.getCompany() + "<br>\n");
            }
        }

        html.append(si.getAddress() + "<br>\n");

        //Only adds Address2 if it is present
        if(si.getAddress2() != null) {
            if (!si.getAddress2().equals("")) {
                html.append(si.getAddress2() + "<br>\n");
            }
        }

        html.append(si.getCity() + ", " + si.getState() + " " + si.getZip() + " " + si.getCountry() + "</td>\n" +
                "<td><b>Phone Number:</b> " + si.getPhone()  + "<br>\n" +
                "<b>Email Address:</b> " + si.getEmail() + "</td>" +
                "<td></td>\n" +
                "<td></td>\n" +
                "<td><b>Tender Information:</b> <br>\n");

        for(int i = 0; i < td.tenderTypeSize(); i++) {
            html.append(td.getTenderType(i) + ": $" + String.format("%.2f", td.getTenderAmount(i)));
        }


        html.append("</td>\n" +
                "<td align=\"right\"><b>Store:</b> " + this.store + "<br>\n" +
                "<b>Transaction Number:</b> " + si.getTransNumber() + "<br>\n" +
                "<b>Transaction Time:</b> " + td.getTransactionTime() + "<br>\n" +
                "<b>Transfer Number: </b> " + this.worksheetId + "<br>\n");
        if(!td.getTransComment().isEmpty()) {
            html.append("<b>Comment: </b> " + td.getTransComment());
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
            if(td.getLookupCode(i).equals("BAG")) {
                location = "";
            }
            else if(itemLinesToDrop.contains(i)) {
                location = this.store;
            }
            else if(itemLinesTook.contains(i)){
                location = "Customer Took";
            }
            else {
                location = "Warehouse";
            }
            html.append("<tr>\n" +
                    "<td>" + td.getLookupCode(i) + "</td>\n" +
                    "<td>" + td.getDescription(i) + "</td>\n" +
                    "<td>" + "$" + String.format("%.2f", td.getPrice(i)) + "</td>\n" +
                    "<td>" + (int) td.getQty(i) + "</td>\n" +
                    "<td>" + "$" + String.format("%.2f", td.getTransactionSalesTax(i)) + "</td>\n" +
                    "<td>" + location + "</td>\n" +
                    "</tr>");

        }

        //Calculates totals for Transaction
        double transTotal = 0.00;
        double transTaxTotal = 0.00;
        for(int i = 0; i < lineItems.size(); i++) {
            transTotal += td.getTotal(i);
            transTaxTotal += td.getTransactionSalesTax(i);
        }

        transTotal = round(transTotal, 2);
        transTaxTotal = round(transTaxTotal, 2);

        html.append("<tr>\n" +
                "<td></td>\n" +
                "<td align=\"right\"><b>Subtotal:</b></td>\n" +
                "<td>" + "$" + String.format("%.2f", transTotal) + "</td>\n" +
                "<td align=\"right\"><b>Tax Total:</b></td>\n" +
                "<td>" + "$" + String.format("%.2f", transTaxTotal) + "</td>\n" +
                "<td>Shipping Charge: $" + String.format("%.2f", si.getShippingCharge()) + "</td>\n" +
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