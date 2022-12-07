package com.aryvert;

import com.aryvert.Data.Counterparty;
import com.aryvert.Data.Mapping;
import com.aryvert.Data.Product.TBill;
import com.aryvert.Utility.CDUFUtil;
import com.aryvert.Utility.CSVReader;
import com.aryvert.Utility.Config.Configurer;
import com.aryvert.Utility.FileManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class AryvertStart {

    final static Logger logger = Logger.getLogger(AryvertStart.class);

    @Autowired
    private Configurer configurer;

    @Autowired
    private FileManager fileManager;

    @Autowired
    private CSVReader reader;

    @Autowired
    private CDUFUtil cdufUtil;


    @Value("${interface_Bloomberg}")
    private Boolean bloomberg;

    @Value("${product.TBill.active}")
    private Boolean tBill;

    @Value("${interval}")
    private long interval;
    @Value("${file_prefix}")
    private String filePrefix;

    public void initialize() {

        configurer.dropAll();
        loadProductMapping();
        loadCounterpartyMapping();
        while (true) {

            readInput();
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void readInput() {

        File[] files = fileManager.readFiles();
        try {
            if (files == null) {
                logger.info(":::: No files found in directory ::::");
            } else {

                for (File file : files) {

                    InputStreamReader isr = null;
                    logger.info(":::: Picking files from directory ::::");

                    if (file.getName().contains(filePrefix)) {
                        isr = fileManager.readTradeFile(file);
                        logger.debug(":::: Picked file :::: " + file.getName() + " for processing");

                        List<TBill> tBillList = reader.readBloombergTBill(isr);
                        isr.close();
                        if (tBillList == null) {

                            fileManager.dropFile(file);

                        } else {
                            cdufUtil.createTBILLXMLCDUF(tBillList);
                            fileManager.deleteFile(file);
                        }

                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCounterpartyMapping() {
        loadBloomberg();
    }

    private void loadProductMapping() {

        InputStreamReader is = null;
        if (tBill) {
            configurer.showConfig();
            is = fileManager.readMappingFile("TBillMapping.csv");
            List<Mapping> tBillMapping = new ArrayList<>();
            tBillMapping = reader.readProductMapping(is, "Bloomberg");
            configurer.saveMapping("TBill", tBillMapping);
        }
        configurer.showConfig();

    }

    private void loadBloomberg() {
        if (bloomberg) {
            List<Counterparty> bloombergList = new ArrayList<>(
                    reader.readCounterparty(
                            fileManager.readMappingFile("BloombergCounterparty.csv"), "BLOOMBERG"));
            configurer.saveCounterParties(bloombergList);
        } else {
            logger.info("::: Bloomberg not configured :::");
        }
    }


}
