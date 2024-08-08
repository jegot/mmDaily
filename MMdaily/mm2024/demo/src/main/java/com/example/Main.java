package com.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.pdmodel.PDPage;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String csvFileName = "C:/Users/jgolling/Desktop/programming/new.csv";
        String folderPath = "Z:/DPForms/7-23-24 claimsCertified/";

        // Use try-with-resources to ensure FileWriter and PrintWriter are closed
        try (FileWriter fileWriter = new FileWriter(csvFileName);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            // Write header line to the CSV file
            writeHeader(printWriter);

            File folder = new File(folderPath);
            File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

            if (listOfFiles != null) {
                for (File pdfFile : listOfFiles) {
                    processPdf(pdfFile, printWriter);
                }
            } else {
                System.out.println("No PDF files found in the directory: " + folderPath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private static void writeHeader(PrintWriter printWriter) {
        String[] headers = {"file name", "policy number", "name", "street3", "street2", "street1", "csz"};
        StringBuilder headerLine = new StringBuilder();
        for (String header : headers) {
            headerLine.append("\"").append(header).append("\",");
        }
        if (headerLine.length() > 0) {
            headerLine.setLength(headerLine.length() - 1); // Remove trailing comma
        }
        printWriter.println(headerLine.toString());
    }




    private static void processPdf(File pdfFile, PrintWriter printWriter) {
        String fileNameString = pdfFile.getName();
        String[] fileNameParts = fileNameString.split(" |\\-|\\#");
        String policyNum = "";

        if (fileNameParts.length > 2) {
            for (int i = 0; i < fileNameParts.length; i++) {
                if (fileNameParts[i].endsWith(",") || fileNameParts[i - 1].endsWith(",")) {
                    policyNum = policyNum + " " + fileNameParts[i];
                }
            }
        } else {
            policyNum = fileNameParts[0];
        }

        List<String> record = new ArrayList<>();
        record.add(fileNameString);
        record.add(policyNum);

        try (PDDocument document = PDDocument.load(pdfFile)) {
            if (document.getNumberOfPages() > 0) {
                
                
                PDPage firstPage = document.getPage(0);

                // Define the rectangle region for the address block
                Rectangle2D addressBlock = new Rectangle2D.Float(10, 110, 300, 150);

                // Create a PDFTextStripperByArea instance
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.addRegion("addressBlock", addressBlock);

                // Extract text from the defined region on the first page
                stripper.extractRegions(firstPage);
                String addressText = stripper.getTextForRegion("addressBlock");

                String[] addressStrings = addressText.split("\\r?\\n|\\r");

                //placeholders for record
                String name = "", street3 = "", street2 = "", street1 = "", csz = "";
                List<String> addressLines = new ArrayList<>();


                for (String s : addressStrings) {
                    if (!(s.isBlank() || s==null || s.contains("***"))) {
                        addressLines.add(s.trim());
                    }
                }

                // If there are any address lines, assign the last one to csz if it ends with four digits
                if (!addressLines.isEmpty()) {
                    int lastLineIndex = addressLines.size() - 1;
                    if (endsWithFourDigits(addressLines.get(lastLineIndex))) {
                        csz = addressLines.remove(lastLineIndex);
                    }

                    // Assign remaining lines to name and street fields
                    if (!addressLines.isEmpty()) {
                        name = addressLines.remove(0);  // First line is the name
                    }
                    if (!addressLines.isEmpty()) {
                        street3 = addressLines.size() >= 3 ? addressLines.remove(addressLines.size() - 3) : "";
                        street2 = addressLines.size() >= 2 ? addressLines.remove(addressLines.size() - 2) : "";
                        street1 = addressLines.size() >= 1 ? addressLines.remove(addressLines.size() - 1) : "";
                    }
                }

                // Add fields to the record
                record.add(name);
                record.add(street3);
                record.add(street2);
                record.add(street1);
                record.add(csz);

                // Print the extracted text
                for (int r = 0; r < record.size(); r++) {
                    System.out.println("Extracted Address Text: " + record.get(r));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Write to CSV
        writeToCsv(record, printWriter);
    }

    private static boolean endsWithFourDigits(String text) {
        return Pattern.matches(".*\\d{4}$", text);
    }

    private static void writeToCsv(List<String> record, PrintWriter printWriter) {
        StringBuilder csvLine = new StringBuilder();

        for (String field : record) {
            csvLine.append("\"").append(field.replace("\"", "\"\"")).append("\",");
        }
        if (csvLine.length() > 0) {
            csvLine.setLength(csvLine.length() - 1); // Remove trailing comma
        }

        printWriter.println(csvLine.toString());
    }
}
