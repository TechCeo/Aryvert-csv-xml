package com.aryvert.Data;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Entity
public class Counterparty {

    @Id
    private long id;
    private String code;
    private String counterpartyName;
    private String calypsoName;
    private String source;


    public Counterparty() {

    }
}
