package com.aryvert.Data.DR.Mapping;

import com.aryvert.Data.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface CPRepo extends JpaRepository<Counterparty,Long> {
    List<Counterparty> findAllBySource(String bloomberg);

    @Query("select calypsoName from Counterparty where counterpartyName = ?1")
    String getByCField(String party);
}
