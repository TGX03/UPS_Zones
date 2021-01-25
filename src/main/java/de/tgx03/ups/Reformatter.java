package de.tgx03.ups;

import de.tgx03.ConsoleReader;
import de.tgx03.MultiHashMap;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reformats a given UPS zone table in xlsx format by the zones of the countries
 * Only the country list must remain, with the country name in the first, the express ID in the second,
 * the standard ID in the third and the expedited ID being in the fourth row.
 * The tool automatically detects based on that whether a country is standard or expedited
 */
public class Reformatter {

    private static final ArrayList<Country> countries = new ArrayList<>(200);
    private static final MultiHashMap<Short, String> standard = new MultiHashMap<>(5, 8);
    private static final MultiHashMap<Short, String> expedited = new MultiHashMap<>(30, 8);
    private static final MultiHashMap<Short, String> express = new MultiHashMap<>(20, 16);

    private static AtomicInteger counter = new AtomicInteger();

    /**
     * Runs the tool
     * @param args ignored
     * @throws IOException Happens when there's an error reading the table or writing the output
     * @throws InterruptedException In case something weird happens with threading
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        read();
        sort();
        write();
    }

    /**
     * Prompts the user to enter the location of the file and then reads it
     * @throws IOException When the file couldn't be read
     */
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

    /**
     * Sorts the countries in their corresponding categories (express, standard and expedited)
     */
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

    /**
     * Creates a new excel spreadsheet consisting of 3 sheets for express, expedited and standard
     * @throws IOException When the output file could not be written to
     * @throws InterruptedException In case the threads do something weird while creating the sheets
     */
    private static void write() throws IOException, InterruptedException {
        Workbook output = WorkbookFactory.create(false);
        Sheet express = output.createSheet("Express");
        Thread expressWriter = new Thread(new Writer(express, Reformatter.express), "Express");
        expressWriter.start();
        Sheet standard = output.createSheet("Standard");
        Thread standardWriter = new Thread(new Writer(standard, Reformatter.standard), "Standard");
        standardWriter.start();
        Sheet expedited = output.createSheet("Expedited");
        Thread expeditedWriter = new Thread(new Writer(expedited, Reformatter.expedited), "Expedited");
        expeditedWriter.start();

        expressWriter.join();
        standardWriter.join();
        expeditedWriter.join();

        System.out.println("Output File:");
        String outputFile = ConsoleReader.readLine();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        output.write(outputStream);
    }

    /**
     * A class representing a country and its corresponding zone IDs
     */
    private static class Country {

        public final String name;
        public final short expressID;
        public final short standardID;
        public final boolean standard;
        public final boolean expedited;

        /**
         * @param name The name of this country
         * @param express Its ID for express shipping
         * @param standard Its ID for standard or expedited shipping
         * @param standardAvailable Whether standard shipping is available
         * @param expedited Whether standard is actually expedited
         */
        public Country (String name, short express, short standard, boolean standardAvailable, boolean expedited) {
            this.name = name;
            this.expressID = express;
            this.standardID = standard;
            this.standard = standardAvailable;
            this.expedited = expedited;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
