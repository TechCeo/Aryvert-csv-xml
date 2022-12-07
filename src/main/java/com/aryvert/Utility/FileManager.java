package com.aryvert.Utility;

import com.aryvert.AryvertStart;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class FileManager {
    final static Logger logger = Logger.getLogger(FileManager.class);
    @Value("${input_path}")
    private String inputPath;
    @Value("${error_path}")
    private String failedPath;
    @Value("${maping_file_path}")
    private String mappingfilepath;

//    private File[] directoryFiles = null;

    public File[] readFiles() {

        File inputFile = new File(inputPath);
        File[] files = inputFile.listFiles();

        return files;
    }

    public InputStreamReader readMappingFile(String fileName) {

        InputStreamReader is = null;

        try {
            File initialFile = new File(mappingfilepath + fileName);

            if (!(initialFile == null))
                  logger.info("Mapping file : " + fileName + " found :::");

            InputStream targetStream = new FileInputStream(initialFile);
            is = new InputStreamReader(targetStream, "UTF-8");

        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }

        return is;
    }

    public InputStreamReader readTradeFile(File file) {
        InputStreamReader is = null;
        try {
            InputStream targetStream = new FileInputStream(file);
            is = new InputStreamReader(targetStream, "UTF-8");


        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }

        return is;
    }


    public void dropFile(File file) {
        try {

            String fileName = file.getName().substring(0, file.getName().indexOf(".csv")) + "_Failed.csv";
            logger.info(":::: Dropping error file :::: " + fileName);
            FileOutputStream outputStream = new FileOutputStream(failedPath + fileName);

            byte[] buffer = java.nio.file.Files.readAllBytes(file.toPath());
            outputStream.write(buffer);
            outputStream.close();
            deleteFile(file);


        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteFile(File file) {

        File toDelete = new File(inputPath + file.getName());
        try {
            FileUtils.delete(toDelete);
        } catch (IOException e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
//        toDelete.delete();
    }
}
