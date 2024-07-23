package com.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {


        File pdfFile = new File("C:/Users/jgolling/Desktop/programming/examplemassDailyfiles/7-2-24 claimsCertified/254484454A mm77481.pdf");
        //File pdfFile = new File("Z:/DPForms/Complete/03-01-24 PS Bills Certified/21280531 mm55710.pdf");

        //policy number is first numbers in file name before the space
        //example: 12346789 mm1234.pdf  123456789 would be the policy num.

        String fileNameString = pdfFile.getName();
        String[] fileNameParts = fileNameString.split(" ");
        String policyNum = "";

        if(fileNameParts.length > 2) {
            for(String part :  fileNameParts) {
                if(part.endsWith(","))
                    policyNum = policyNum + part;
            }
        }
        else {
            policyNum = fileNameParts[0];
        }
        
        System.out.println("Policy Number: " + policyNum);

       


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

                // Print the extracted text
                System.out.println("Extracted Address Text: " + addressText);

                }

            } 
            
        catch (Exception e) { e.printStackTrace(); }
        

        //document.close();


        

        }



        //end of file
    }
