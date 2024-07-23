package com.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        String csvFileName = "D:/Andrews/MMdaily/new.csv";

        
        File pdfFile = new File("D:/Andrews/MMdaily/sampleFiles/7-2-24 concorr_request/6475600, 6958825, 8609121-MMA0682.pdf");
        //File pdfFile = new File("C:/Users/jgolling/Desktop/programming/examplemassDailyfiles/7-2-24 claimsCertified/254484454A mm77481.pdf");
        //File pdfFile = new File("Z:/DPForms/Complete/03-01-24 PS Bills Certified/21280531 mm55710.pdf");

        //policy number is first numbers in file name before the space
        //example: 12346789 mm1234.pdf  123456789 would be the policy num.

        String fileNameString = pdfFile.getName();
        String[] fileNameParts = fileNameString.split(" |\\-|\\#");
        String policyNum = "";

        if(fileNameParts.length > 2) {

            for(int i = 0; i<fileNameParts.length; i++ ) {
                
                if(fileNameParts[i].endsWith(",") || fileNameParts[i-1].endsWith(","))
                    policyNum = policyNum + fileNameParts[i];
            }
        }
        else {
            policyNum = fileNameParts[0];
        }

        List<String> record = new ArrayList<String>();
        record.add(fileNameString);
        record.add(policyNum);
       


        //bills, claims, & grace need ID field pulled in
            
        try(PDDocument document = PDDocument.load(pdfFile)) {

             if (document.getNumberOfPages() > 0) {
                // Get the first page
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
                

                for(String s : addressStrings) {

                    if (!(s.isBlank() || s == null)) {
                        record.add(s);
                    }
                }

                // Print the extracted text
                for(int r = 0; r<record.size(); r++) {
                    System.out.println("Extracted Address Text: " + record.get(r));

                    }

                }

            } 
            
        catch (Exception e) { e.printStackTrace(); }
        
        writeToCsv(record, csvFileName);

        }






        private static void writeToCsv(List<String> record, String fileName) {
            try (FileWriter fileWriter = new FileWriter(fileName);
                 PrintWriter printWriter = new PrintWriter(fileWriter)) {
    
                StringBuilder csvLine = new StringBuilder();
                for (String field : record) {
                    csvLine.append("\"").append(field.replace("\"", "\"\"")).append("\",");
                }
                if (csvLine.length() > 0) {
                    csvLine.setLength(csvLine.length() - 1); // Remove trailing comma
                }
    
                printWriter.println(csvLine.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }





        //end of file
    }

