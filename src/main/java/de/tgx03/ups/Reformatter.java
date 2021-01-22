package de.tgx03.ups;

import de.tgx03.ConsoleReader;
import de.tgx03.MultiHashMap;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Reformatter {

    private static final ArrayList<Country> countries = new ArrayList<>(200);
    private static final MultiHashMap<Short, String> standard = new MultiHashMap<>(5, 8);
    private static final MultiHashMap<Short, String> expedited = new MultiHashMap<>(30, 8);
    private static final MultiHashMap<Short, String> express = new MultiHashMap<>(20, 16);

    private static AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws IOException, InterruptedException {
        read();
        sort();
        write();
    }

    private static void read() throws IOException {
        System.out.println("Input file:");
        String fileName = ConsoleReader.readLine();
        Workbook readInput = WorkbookFactory.create(new File(fileName));
        Sheet sheet = readInput.getSheetAt(0);
        for (Row currentRow : sheet) {
            if (currentRow.getCell(0).getStringCellValue().equals("")) {
                break;
            } else if (!currentRow.getCell(0).getStringCellValue().contains("**")) {
                String countryName = currentRow.getCell(0).getStringCellValue();
                short expressID = (short) currentRow.getCell(1).getNumericCellValue();
                short standardID;
                if (currentRow.getCell(2) != null) {
                    standardID = (short) currentRow.getCell(2).getNumericCellValue();
                    Country country = new Country(countryName, expressID, standardID, true, false);
                    countries.add(country);
                } else if (currentRow.getCell(3) != null) {
                    standardID = (short) currentRow.getCell(3).getNumericCellValue();
                    Country country = new Country(countryName, expressID, standardID, true, true);
                    countries.add(country);
                } else {
                    Country country = new Country(countryName, expressID, (short) -1, false, false);
                    countries.add(country);
                }
            }
        }
    }

    private static void sort() {
        for (Country current : countries) {
            express.put(current.expressID, current.name);
            if (current.expedited) {
                expedited.put(current.standardID, current.name);
            } else if (current.standard) {
                standard.put(current.standardID, current.name);
            }
        }
    }

    private static void write() throws IOException, InterruptedException {
        Workbook output = WorkbookFactory.create(false);
        Sheet express = output.createSheet("Express");
        Thread expressWriter = new Thread(new Writer(express, Reformatter.express));
        expressWriter.start();
        Sheet standard = output.createSheet("Standard");
        Thread standardWriter = new Thread(new Writer(standard, Reformatter.standard));
        standardWriter.start();
        Sheet expedited = output.createSheet("Expedited");
        Thread expeditedWriter = new Thread(new Writer(expedited, Reformatter.expedited));
        expeditedWriter.start();

        expressWriter.join();
        standardWriter.join();
        expeditedWriter.join();

        System.out.println("Output File:");
        String outputFile = ConsoleReader.readLine();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        output.write(outputStream);
    }

    private static class Writer implements Runnable {

        private final MultiHashMap<Short, String> map;
        private final Sheet sheet;
        private final ArrayList<ArrayList<String>> values = new ArrayList<>(10);

        public Writer(Sheet sheet, MultiHashMap<Short, String> map) {
            this.sheet = sheet;
            this.map = map;
        }


        @Override
        public void run() {
            createList();
            sort();
            writeMapToSheet();
        }

        private void createList() {
            Set<Map.Entry<Short, String>> set = map.entrySet();
            HashMap<Short, Integer> idMappedToColumn = new HashMap<>(12);
            for (Map.Entry<Short, String> entry : set) {
                if (!idMappedToColumn.containsKey(entry.getKey())) {
                    idMappedToColumn.put(entry.getKey(), idMappedToColumn.size());
                    values.add(new ArrayList<>(12));
                    values.get(idMappedToColumn.get(entry.getKey())).add(entry.getKey().toString());
                }
                values.get(idMappedToColumn.get(entry.getKey())).add(entry.getValue());
            }
        }

        private void sort() {
            values.sort(Comparator.comparingInt(o -> Integer.parseInt(o.get(0))));
            for (ArrayList<String> list : values) {
                list.sort(Comparator.naturalOrder());
            }
        }

        private void writeMapToSheet() {
            for (int i = 0; i < values.size(); i++) {
                List<String> current = values.get(i);
                for (int j = 0; j < current.size(); j++) {
                    if (sheet.getRow(j) == null) {
                        sheet.createRow(j);
                    }
                    sheet.getRow(j).createCell(i).setCellValue(current.get(j));
                }
            }
        }
    }
}
