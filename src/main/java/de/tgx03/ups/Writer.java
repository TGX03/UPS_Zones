package de.tgx03.ups;

import de.tgx03.MultiHashMap;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.*;

/**
 * A class that takes the Lists from the parent holding the Country ids
 * and converts them into a sheet
 */
class Writer implements Runnable {

    private final MultiHashMap<Short, String> map;
    private final Sheet sheet;
    private final ArrayList<ArrayList<String>> values = new ArrayList<>(10);

    /**
     * Initializes a new writer with its corresponding source and target
     *
     * @param sheet The sheet to write to
     * @param map   The map to use the data from
     */
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

    /**
     * Convert the bare list into a 2D list sorted by the country ID
     */
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

    /**
     * Put the country IDs in ascending order and sort the lists in alphabetical order
     */
    private void sort() {
        values.sort(Comparator.comparingInt(o -> Integer.parseInt(o.get(0))));
        for (ArrayList<String> list : values) {
            list.sort(Comparator.naturalOrder());
        }
    }

    /**
     * Write the created and sorted lists to the spreadsheet
     */
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
