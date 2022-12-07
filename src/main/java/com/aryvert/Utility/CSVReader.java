package com.aryvert.Utility;

import com.aryvert.Data.Counterparty;
import com.aryvert.Data.DR.Mapping.CPRepo;
import com.aryvert.Data.DR.Mapping.TBillMappingRepo;
import com.aryvert.Data.Mapping;
import com.aryvert.Data.Product.TBill;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CSVReader {
    final static Logger logger = Logger.getLogger(CSVReader.class);

    @Autowired
    TBillMappingRepo tBillMappingRepo;

    @Autowired
    CPRepo cpRepo;

    public List<Counterparty> readCounterparty(InputStreamReader is, String source) {

        try {
            CSVParser csvParser = parseFile(is);
            List<Counterparty> cpList = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord csvRecord : csvRecords) {
//                  logger.info(csvRecord.toString());
                Counterparty cp = new Counterparty(Long.parseLong(csvRecord.get(0)), csvRecord.get(source), csvRecord.get("COUNTERPARTY"), csvRecord.get("CALYPSO"), source);
                cpList.add(cp);

            }
            return cpList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }

    public List<Mapping> readProductMapping(InputStreamReader is, String source) {

        List<Mapping> mappingList = new ArrayList<>();
        try {
            CSVParser csvParser = parseFile(is);

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord csvRecord : csvRecords) {
                if (csvRecord.size() >= csvParser.getHeaderMap().size()) {
                    Mapping mapping = new Mapping(csvRecord.get("CalypsoField"), csvRecord.get("BloombergField")

                    );

                    mappingList.add(mapping);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
        return mappingList;
    }

    private CSVParser parseFile(InputStreamReader is) {
        CSVParser csvParser = null;

        if ((is == null)) logger.info("Empty Configuration file attached");

        try {
            BufferedReader fileReader = new BufferedReader(is);
            csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csvParser;
    }

    public List<TBill> readBloombergTBill(InputStreamReader is) {
        List<TBill> tBillList = new ArrayList<>();

//        //update with bloomberg action and calypso action
//        if (tradeAction.equalsIgnoreCase("Cancelled")) action = "CANCEL";


        try {
            CSVParser csvParser = parseFile(is);

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord csvRecord : csvRecords) {

                //Handle Action
                String action = "NEW";
                //String tradeAction = csvRecord.get(tBillMappingRepo.getByCField("Action"));
                String tradeAction = csvRecord.get(0);
                if (tradeAction.equalsIgnoreCase("Corrected")) action = "AMEND";
                if (tradeAction.equalsIgnoreCase("Cancelled")) action = "CANCEL";


                //Handle Side and Counterparty
                String side = csvRecord.get(tBillMappingRepo.getByCField("BuySell"));
                String counterParty = tBillMappingRepo.getByCField("Counterparty");
                String party = csvRecord.get(counterParty.substring(0, counterParty.indexOf(",")));

                if (side.equals("B") || side.equals("[B]")) {
                    party = csvRecord.get(counterParty.substring(counterParty.indexOf(",") + 1));
//                      logger.info(party);
                    side = "BUY";
                } else {
//                      logger.info(party);
                    side = "SELL";
                }


                //Handle Negotiated Price type and Book for Bond or TBill
                String negotiatedPriceType = tBillMappingRepo.getByCField("NegotiatedPrice");
                String negotiatedPrice = "";
                String val = csvRecord.get(negotiatedPriceType.substring(negotiatedPriceType.indexOf(",") + 1));
                if (val.indexOf('.') == 0)
                    val = "0" + val;
                String book = "";

                if (Double.parseDouble(val) <= 0.0) {
                    negotiatedPrice = csvRecord.get(negotiatedPriceType.substring(0, negotiatedPriceType.indexOf(",")));
                    negotiatedPriceType = "CleanPrice";
                    book = "FVTPL.Bond";

                } else {
                    negotiatedPrice = val;
                    negotiatedPriceType = "Discount";
                    book = "FVTPL.TBill";
                }
                //end Price type and book handler


                //Handle Code Value and type
                String productCodeValue = tBillMappingRepo.getByCField("ProductCodeValue");

                String prdValue = csvRecord.get(productCodeValue.substring(0, productCodeValue.indexOf(",")));
                String prdCodeName = "";
                if (prdValue.isEmpty()) {
                    prdCodeName = "BB_UNIQUE";
                    prdValue = productCodeValue.substring(productCodeValue.indexOf(",") + 1);
                } else {
                    prdCodeName = "ISIN";
                    prdValue = productCodeValue.substring(0, productCodeValue.indexOf(","));
                }


                TBill tBill = new TBill();
                tBill.setAction(action);
                tBill.setExternalRefId(csvRecord.get(tBillMappingRepo.getByCField("ExternalRefId")));
                tBill.setCounterparty(cpRepo.getByCField(party));

                System.out.println("Counterparty reieved: " + cpRepo.getByCField(party));
                tBill.setBook(book);
                tBill.setCurrency("NGN");
                tBill.setTradeSettlementCurrency("NGN");
                tBill.setComment(csvRecord.get(0));
                tBill.setProductCodeType(prdCodeName);
                tBill.setProductCodeValue(csvRecord.get(prdValue));
                tBill.setNominal(csvRecord.get(tBillMappingRepo.getByCField("Nominal")));
                tBill.setBuySell(side);
                tBill.setTradeDate(csvRecord.get(tBillMappingRepo.getByCField("TradeDate")));
                tBill.setSettlementDate(csvRecord.get(tBillMappingRepo.getByCField("SettlementDate")));
                tBill.setNegotiatedPriceType(negotiatedPriceType);
                tBill.setNegotiatedPrice(negotiatedPrice);
                tBill.setTraderName(csvRecord.get(tBillMappingRepo.getByCField("TraderName")));
                tBill.setSalesPerson("NONE"); //csvRecord.get(tBillMappingRepo.getByCField("SalesPerson"))
                //     tBill.setQuantity(csvRecord.get(tBillMappingRepo.getByCField("Quantity")));

                tBillList.add(tBill);
            }


        } catch (Exception e) {
            //handle missing columns and drop error file in failed folder
            logger.info("Mapping Error ::" + e.getMessage());
            e.printStackTrace();
            return null;

        }

        return tBillList;
    }

}
