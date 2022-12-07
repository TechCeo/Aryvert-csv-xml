package com.aryvert.Utility.Config;

import com.aryvert.Data.Counterparty;
import com.aryvert.Data.DR.Mapping.CPRepo;
import com.aryvert.Data.DR.Mapping.TBillMappingRepo;
import com.aryvert.Data.Mapping;
import com.aryvert.Utility.FileManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Configurer {

    final static Logger logger = Logger.getLogger(Configurer.class);
    @Autowired
    private CPRepo cpRepo;

    @Autowired
    private TBillMappingRepo tBillMappingRepo;


    public void showConfig() {
        List<Counterparty> counterparties = cpRepo.findAll();

    }

    public void saveCounterParties(List<Counterparty> cp) {

        cpRepo.saveAll(cp);

    }


    public void dropAll() {
        tBillMappingRepo.deleteAll();
    }


    public void saveMapping(String productName, List<Mapping> product) {

        switch (productName) {

            case "TBill":
                for (Mapping f : product) {
                    tBillMappingRepo.save(f);
                }
                break;

            default:
                  logger.info("No Mapping Saved");
        }
    }


}


