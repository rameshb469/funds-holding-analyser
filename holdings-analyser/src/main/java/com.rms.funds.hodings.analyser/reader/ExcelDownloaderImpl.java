package com.rms.funds.hodings.analyser.reader;

import com.rms.funds.hodings.analyser.modal.ExcelDownloaderAttributes;
import com.rms.funds.hodings.analyser.modal.MutualFundStockHolding;
import com.rms.funds.hodings.analyser.modal.SheetColumnMapper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

@Service
public class ExcelDownloaderImpl implements ExcelDownloader {

    private static final String SEPARATOR = "-";
    private static final String XLS_FILE_EXTENSION = "xls";

    @Async
    @Override
    public List<MutualFundStockHolding> load(ExcelDownloaderAttributes attributes) {

        File file = null;
        try {
            URL excelUrl = new URL(attributes.getUrl());
//            Runtime rt = Runtime.getRuntime();
//            InputStream inputStream2 = rt.exec("rundll32 url.dll,FileProtocolHandler " + attributes.getUrl())
//                    .getInputStream();
            Thread.sleep(1000);

            URLConnection urlConnection = excelUrl.openConnection();
            //        System.setProperty ("jsse.enableSNIExtension", "false");
//            urlConnection.setRequestProperty("vary", "Accept-Encoding");
//            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
//            urlConnection.setRequestProperty("Accept", "*/*");
//            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
//            urlConnection.setRequestProperty("Origin","https://"+excelUrl.getHost());
            //  urlConnection.setRequestProperty("Cookie", cookingService.geCookie(excelUrl,attributes.getUrl()));


//

            file = new File(getFileName(attributes));

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] byteArray = new byte[1024]; // amount of bytes reading from input stream at a given time
            int readLength;

            InputStream inputStream = excelUrl.openStream();
//            if(attributes.isZipped()){
//                ZipInputStream zis = new ZipInputStream(inputStream);
//                ZipEntry zipEntry = zis.getNextEntry();
//                while (zipEntry != null) {
//                    zipEntry.
//                }
//
//                zis.closeEntry();
//                zis.close();
//            }


            System.out.println("waiting..................." + attributes.getUrl());
            Thread.sleep(5000);
            System.out.println("Proceed..................");
            //  InputStream inputStream = cookingService.geCookie(excelUrl, attributes.getUrl());
            while ((readLength = inputStream.read(byteArray)) > 0) {
                outputStream.write(byteArray, 0, readLength);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();


            return process(file, attributes);
        } catch (FileNotFoundException nf) {
            System.out.println("File not found" + nf.getMessage());

            throw new RuntimeException("FileNotFoundException");
        } catch (Exception e) {
            e.printStackTrace();
            // Exception
            throw  new RuntimeException(e.getMessage());
        } finally {
            if (file != null) {
                file.delete();
            }
        }

       // return Collections.emptyList();
    }

    @Override
    public List<MutualFundStockHolding> process(File file, String sheetName,
                                                SheetColumnMapper columnMapper) {
        try {
            return process(file, ExcelDownloaderAttributes.builder()
                    .sheetName(sheetName)
                    .isPickupBySystem(columnMapper.isNull())
                    .sheetColumnMapper(columnMapper)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String getFileName(ExcelDownloaderAttributes attributes) {
        return new StringBuilder(attributes.getMutualFundName() + "")
                .append(SEPARATOR)
                .append(System.currentTimeMillis())
                .append("." + attributes.getExtension())
                .toString();
    }

    private Map<Workbook,FileInputStream> initializeWorkbook(File file) throws Exception {

        Workbook workbook = null;
        FileInputStream fileInputStream = null;
        try{
            fileInputStream = new FileInputStream(file);
            workbook = new XSSFWorkbook(fileInputStream);
        }catch (Exception e){
            workbook = WorkbookFactory.create(file);
        }

        if (workbook == null){
            throw new RuntimeException("Invalid file type");
        }
        return Map.of(workbook, fileInputStream);
    }


    private <W extends Workbook,S extends Sheet> List<Row> getHoldings(File file, String sheetName,
                          Class<W> workbookClassType,
                          Class<S> sheetClassType) {
        W workbook = null;
        FileInputStream fileInputStream = null;

        try {
            Map<Workbook,FileInputStream>  workMap = initializeWorkbook(file);
            for (var entry : workMap.entrySet()){
                  workbook = (W) entry.getKey();
                  fileInputStream = entry.getValue();
            }
        } catch (Exception e){
            throw new RuntimeException("Invalid file tpe"+e.getMessage());
        }

        if (workbook == null) throw new RuntimeException("Workbook is null");

        S sheet = null;
        if (StringUtils.hasLength(sheetName)) {
              sheet = (S) workbook.getSheet(sheetName);
        } else {
            //Get first/desired sheet from the workbook
            sheet = (S) workbook.getSheetAt(0);
        }


        //Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();

        List<Row> holdingDataRows = new ArrayList<>();

        while (rowIterator.hasNext()) {

            Row row = rowIterator.next();

            //For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {

                Cell cell = cellIterator.next();

                if (CellType.STRING.equals(cell.getCellType())) {
                    if (cell.getStringCellValue() != null &&
                            cell.getStringCellValue().toUpperCase().startsWith("INE")) {
                        holdingDataRows.add(row);
                    }
                }

            }
        }

        try {
            fileInputStream.close();
        } catch (Exception e){
            throw new RuntimeException("Error while closing "+e.getMessage());
        }

        return holdingDataRows;
    }

    @Override
    public List<MutualFundStockHolding> process(File file, ExcelDownloaderAttributes attributes) {
//        XSSFWorkbook xssfWorkbook = null;
//        HSSFWorkbook hssfWorkbook = null;
//        FileInputStream fileInputStream = null;
//
//        boolean isXlsFile = false;
//        try {
//            Map<Workbook,FileInputStream>  workMap = initializeWorkbook(file);
//            for (var entry : workMap.entrySet()){
//                var key = entry.getKey();
//                fileInputStream = entry.getValue();
//
//                if (key instanceof  XSSFWorkbook) {
//                    xssfWorkbook = (XSSFWorkbook) key;
//                } else if (key instanceof HSSFWorkbook) {
//                    hssfWorkbook = (HSSFWorkbook) key;
//                    isXlsFile = true;
//                }
//              //  workbook = (XSSFWorkbook) entry.getKey();
//              //  fileInputStream = entry.getValue();
//            }
//        } catch (Exception e){
//            throw new RuntimeException("Invalid file tpe"+e.getMessage());
//        }
//
//        XSSFSheet xssfSheet = null;
//        HSSFSheet hssfSheet = null;
//
//
//
//        if (StringUtils.hasLength(attributes.getSheetName())) {
//
//            if (!isXlsFile) {
//                xssfSheet = xssfWorkbook.getSheet(attributes.getSheetName());
//            } else {
//                hssfSheet = hssfWorkbook.getSheet(attributes.getSheetName());
//            }
//
//          //  sheet = workbook.getSheet(attributes.getSheetName());
//        } else {
//            //Get first/desired sheet from the workbook
//            hssfSheet = hssfWorkbook.getSheetAt(0);
//        }
//
//
//        //Iterate through each rows one by one
//        Iterator<Row> rowIterator = sheet.iterator();
//
//        List<Row> holdingDataRows = new ArrayList<>();
//
//        while (rowIterator.hasNext()) {
//
//            Row row = rowIterator.next();
//
//            //For each row, iterate through all the columns
//            Iterator<Cell> cellIterator = row.cellIterator();
//
//            while (cellIterator.hasNext()) {
//
//                Cell cell = cellIterator.next();
//
//                if (CellType.STRING.equals(cell.getCellType())) {
//                    if (cell.getStringCellValue() != null &&
//                            cell.getStringCellValue().toUpperCase().startsWith("INE")) {
//                        holdingDataRows.add(row);
//                    }
//                }
//
//            }
//        }
//
//        try {
//            fileInputStream.close();
//        } catch (Exception e){
//            throw new RuntimeException("Error while closing "+e.getMessage());
//        }

        boolean isXlsFileType = XLS_FILE_EXTENSION.equalsIgnoreCase(attributes.getExtension());

        List<Row> holdingDataRows;
        if (isXlsFileType) {
            holdingDataRows = getHoldings(file, attributes.getSheetName(),
                    HSSFWorkbook.class, HSSFSheet.class);
        } else {
            holdingDataRows = getHoldings(file, attributes.getSheetName(),
                    XSSFWorkbook.class, XSSFSheet.class);
        }


        List<MutualFundStockHolding> stockHoldingList = new ArrayList<>();

        if (!attributes.isPickupBySystem()) {
            for (Row row : holdingDataRows) {

                try {

                    stockHoldingList.add(MutualFundStockHolding.builder()
                            .isinCode(row.getCell(attributes.getSheetColumnMapper().getIsin()).getStringCellValue())
                            .stockName(row.getCell(attributes.getSheetColumnMapper().getStockName()).getStringCellValue())
                            .industry(row.getCell(attributes.getSheetColumnMapper().getIndustry()).getStringCellValue())
                            .quantity((long) row.getCell(attributes.getSheetColumnMapper().getQuantity()).getNumericCellValue())
                            .marketValue(row.getCell(attributes.getSheetColumnMapper().getMarketValue()).getNumericCellValue())
                            .netAssetPerc(row.getCell(attributes.getSheetColumnMapper().getNetAssetPerc()).getNumericCellValue())
                            .build());
                } catch (Exception e) {
                    //e.printStackTrace();
                    throw e;
                }
            }

        } else {
            MutualFundStockHolding.MutualFundStockHoldingBuilder builder = MutualFundStockHolding.builder();
            for (Row row : holdingDataRows) {
                Iterator<Cell> cellIterator = row.cellIterator();
                int cellNum = 0;
                boolean quantityFilled = false;
                boolean marketValueFilled = false;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();

                    if (cellNum == 0) {
                        cellNum++;
                        continue;
                    }

                    switch (cell.getCellType()) {
                        case STRING:

                            if (cell.getStringCellValue() != null &&
                                    cell.getStringCellValue().toUpperCase().startsWith("INE")) {
                                builder.isinCode(cell.getStringCellValue());
                            } else if (!cell.getStringCellValue().contains(".") ||
                                    cell.getStringCellValue().length() > 4) {
                                if (cell.getStringCellValue().toUpperCase().endsWith("LTD") ||
                                        cell.getStringCellValue().toUpperCase().endsWith("LIMITED")) {
                                    builder.stockName(cell.getStringCellValue());
                                } else {
                                    builder.industry(cell.getStringCellValue());
                                }
                            }
                            break;
                        case NUMERIC:

                            if (quantityFilled && marketValueFilled) {
                                continue;
                            }

                            if (!quantityFilled) {
                                builder.quantity((long) cell.getNumericCellValue());
                                quantityFilled = !quantityFilled;
                            } else if (!marketValueFilled) {
                                builder.marketValue(cell.getNumericCellValue());
                                marketValueFilled = !marketValueFilled;
                            }
                    }

                }

                stockHoldingList.add(builder.build());
            }
        }

        if (file.delete()) {
            System.out.println("File delete");
        } else {
            System.out.println("File not deleted");
        }
        return stockHoldingList;
    }
}
