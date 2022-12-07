package com.aryvert.Data;


import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Entity
@ToString
public class Mapping {

    @Id
    private String CalypsoField;

    private String BloombergField;


    public Mapping() {
    }
}
