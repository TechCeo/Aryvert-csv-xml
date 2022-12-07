package com.aryvert.Utility;

import com.aryvert.Data.DR.Mapping.TBillMappingRepo;
import com.aryvert.Data.Db_Connector;
import com.aryvert.Data.Product.TBill;
import com.aryvert.Utility.Config.Configurer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class CDUFUtil {

    final static Logger logger = Logger.getLogger(CDUFUtil.class);
    @Autowired
    private TBillMappingRepo tBillMappingRepo;

    @Autowired
    private Configurer configurer;

    @Autowired
    private Db_Connector dbConnector;

    @Value("${ouput_path}")
    private String outFolder;

    public void createTBILLXMLCDUF(List<TBill> tBillList) {

        File file = new File(outFolder + "BondTrade_MMDiscount" + LocalDateTime.now().toString().replace(":", "-") + ".xml");
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<CalypsoUploadDocument UploadDate=\"" + LocalDateTime.now().toString() + "\" ClientCode=\"INDI\" Version=\"1\">";
            //  StringBuilder xmlBody = new StringBuilder();
            String xmlFooter = "</CalypsoUploadDocument>";
            outputfile.write(xmlHeader);

            for (TBill tBill : tBillList) {


                String newTDate = "";
                Date date = new SimpleDateFormat("dd/MM/yyyy").parse(tBill.getTradeDate());
                Date setDate = new SimpleDateFormat("dd/MM/yyyy").parse(tBill.getSettlementDate());
                //System.out.println("Tradedate from file: " + date);
                //System.out.println("Settle date from file: " + setDate);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); //"yyyy-MM-dd"
                String tradeDate = formatter.format(date);
                String settleDate = formatter.format(setDate);

                //System.out.println("Trade Date formatted: " + tradeDate);
                //System.out.println("Settle date formatted: " + settleDate);

                if (!tradeDate.startsWith("20")){
                    String sub = tradeDate.substring(2);
                    tradeDate = "20" + sub;
                    System.out.println("trade date and transformed: " + tradeDate);
                }

                if (!settleDate.startsWith("20")){
                    String sub = settleDate.substring(2);
                    settleDate = "20" + sub;
                    System.out.println("settled date and transformed: " + settleDate);
                }

                //System.out.println("passing tradedate: " + tradeDate + "into CDUF");
                //System.out.println("Passing settedate: " + settleDate + "into CDUF");

                String bookName = tBill.getBook();
                if (!tBill.getAction().equals("NEW")) {
                    try {

                        Connection con = dbConnector.connectdb();

                        //DEV & PROD
                        String sql = "SELECT BOOK_NAME FROM biabprod.BOOK WHERE BOOK.BOOK_ID = (SELECT BOOK_ID FROM biabprod.TRADE WHERE EXTERNAL_REFERENCE = '" + tBill.getExternalRefId() + "')";
                        //UAT
                        //String sql = "SELECT BOOK_NAME FROM calypso_uat.BOOK WHERE BOOK.BOOK_ID = (SELECT BOOK_ID FROM calypso_uat.TRADE WHERE EXTERNAL_REFERENCE = '" + tBill.getExternalRefId() + "')";
                        Statement smt = con.createStatement();
                        ResultSet rs = smt.executeQuery(sql);

                        if (rs.next())
                            bookName = rs.getString("BOOK_NAME");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


                String payload = "<CalypsoTrade>\n" +
                        "    <ExternalReference>" + tBill.getExternalRefId() + "</ExternalReference>\n" +
                        "    <TradeId xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>\n" +
                        "    <Action>" + tBill.getAction() + "</Action>\n" +
                        "    <TradeCounterParty>" + tBill.getCounterparty() + "</TradeCounterParty>\n" +
                        "    <TradeBook>" + bookName + "</TradeBook>\n" +
                        "    <TradeBundleOneMsg>false</TradeBundleOneMsg>\n" +
                        "    <TradeEventsInSameBundle>false</TradeEventsInSameBundle>\n" +
                        "    <TradeCurrency>NGN</TradeCurrency>\n" +
                        "    <TradeSettlementCurrency>NGN</TradeSettlementCurrency>\n" +
                        "    <TradeNotional>" + tBill.getNominal().replace(",", "") + "</TradeNotional>\n" +
                        "    <BuySell>" + tBill.getBuySell() + "</BuySell>\n" +
                        "    <TradeDateTime>" + tradeDate + "</TradeDateTime>\n" +
                        "    <TradeSettleDate>" + settleDate + "</TradeSettleDate>\n" +
                        "    <TraderName>NONE</TraderName>\n" +
                        "    <SalesPerson>NONE</SalesPerson>\n" +
                        "    <Comment></Comment>\n" +
                        "    <ProductType>Bond</ProductType>\n" +
                        "    <ProductSubType xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>\n" +
                        "    <Product>\n" +
                        "        <Bond>\n" +
                        "            <ProductCodeType>" + tBill.getProductCodeType() + "</ProductCodeType>\n" +
                        "            <ProductCodeValue>" + tBill.getProductCodeValue() + "</ProductCodeValue>\n" +
                        "            <NegotiatedPriceType>" + tBill.getNegotiatedPriceType() + "</NegotiatedPriceType>\n" +
                        "            <NegotiatedPrice>" + tBill.getNegotiatedPrice() + "</NegotiatedPrice>\n" +
                        "            <Quantity>" + tBill.getNominal() + "</Quantity>\n" +
                        "            <FxRate xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>\n" +
                        "        </Bond>\n" +
                        "    </Product>\n" +
                        "    <TradeKeywords>\n" +
                        "        <Keyword>\n" +
                        "            <KeywordName>TradeSource</KeywordName>\n" +
                        "            <KeywordValue>Bloomberg</KeywordValue>\n" +
                        "        </Keyword>\n" +
                        "    </TradeKeywords>\n" +
                        "</CalypsoTrade>";

                outputfile.write(payload);

            }
            outputfile.write(xmlFooter);
            outputfile.close();
            logger.info("Finished processing file ...");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}


