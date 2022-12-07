package com.aryvert.Data.Product;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Entity
@ToString
public class TBill {

    @Id
    private String TradeId;

    private String Action;

    private String ExternalRefId;
    private String Counterparty;
    private String Book;
    private String Currency;
    private String TradeSettlementCurrency;
    private String ProductCodeType;
    private String ProductCodeValue;
    private String Nominal;
    private String BuySell;
    private String TradeDate;
    private String SettlementDate;
    private String NegotiatedPriceType;
    private String NegotiatedPrice;
    private String TraderName;
    private String SalesPerson;
    private String Comment;
    private String quantity;


    public TBill() {

    }

}
