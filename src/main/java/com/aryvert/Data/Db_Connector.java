package com.aryvert.Data;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;

@Service
public class Db_Connector {

    final static Logger logger = Logger.getLogger(Db_Connector.class);

    @Value("${db_url}")
    private String db_url;

    @Value("${db_username}")
    private String db_username;

    @Value("${db_pwd}")
    private String db_pwd;

    private Connection con;

    public Connection connectdb() {
        try {

            logger.info("DB URL is:: " + db_url);
            logger.info("DB username is:: " + db_username);
            Class.forName("oracle.jdbc.driver.OracleDriver"); //com.mysql.jdbc.Driver
            con = DriverManager.getConnection(
                    db_url, db_username, db_pwd);
            if (con == null)
                throw new IllegalStateException("unable to get DB connection");

        } catch (Exception e) {
            logger.info(e);
        }
        return con;
    }
}
