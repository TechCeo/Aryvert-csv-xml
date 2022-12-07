package com.aryvert.Data.DR.Mapping;

import com.aryvert.Data.Mapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TBillMappingRepo extends JpaRepository<Mapping, Long> {

    @Query("select BloombergField from Mapping where CalypsoField = ?1")
    String getByCField(String fieldName);

    @Query(value = "Select * from Mapping where Source = ?1", nativeQuery = true)
    List<Mapping> findBySource(String bloomberg);
}
