package com.investmentcalc;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for saving investment calculation results to a text file
 * Uses Apache Commons IO library for file operations
 */
public class TextFileSaver {
    
    private static final String DEFAULT_FILENAME = "investment_results.txt";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * Saves the investment calculation results to a text file
     * @param result The InvestmentResult object containing calculation data
     * @param filename The name of the file to save (optional, uses default if null)
     * @return true if save was successful, false otherwise
     */
    public static boolean saveResultsToFile(InvestmentResult result, String filename) {
        if (result == null) {
            return false;
        }
        
        String targetFilename = (filename != null && !filename.isEmpty()) ? filename : DEFAULT_FILENAME;
        File file = new File(targetFilename);
        
        try {
            String content = formatResultsForFile(result);
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving results to file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves the results with default filename
     */
    public static boolean saveResultsToFile(InvestmentResult result) {
        return saveResultsToFile(result, null);
    }
    
    /**
     * Formats the investment results into a readable text format
     */
    private static String formatResultsForFile(InvestmentResult result) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        
        sb.append("=" .repeat(60)).append("\n");
        sb.append("INVESTMENT CALCULATION RESULTS\n");
        sb.append("=" .repeat(60)).append("\n\n");
        
        sb.append("Generated on: ").append(sdf.format(new Date())).append("\n\n");
        
        sb.append("INPUT PARAMETERS:\n");
        sb.append("-" .repeat(40)).append("\n");
        sb.append(String.format("Starting Amount:        $%,.2f\n", result.getStartingAmount()));
        sb.append(String.format("Investment Period:      %d years\n", result.getYears()));
        sb.append(String.format("Annual Return Rate:     %.2f%%\n", result.getAnnualReturnRate()));
        sb.append(String.format("Compounding Frequency:  %s\n", result.getCompoundingFrequency()));
        sb.append("\n");
        
        sb.append("FINAL RESULTS:\n");
        sb.append("-" .repeat(40)).append("\n");
        sb.append(String.format("End Balance:           $%,.2f\n", result.getEndBalance()));
        sb.append(String.format("Total Contributions:   $%,.2f\n", result.getTotalContributions()));
        sb.append(String.format("Total Interest Earned: $%,.2f\n", result.getTotalInterest()));
        
        BigDecimal totalReturn = result.getTotalInterest()
            .divide(result.getTotalContributions(), 4, BigDecimal.ROUND_HALF_UP)
            .multiply(new BigDecimal("100"));
        sb.append(String.format("Total Return:          %.2f%%\n", totalReturn));
        sb.append("\n");
        
        // Add yearly summary if available
        if (result.getYearlyData() != null && !result.getYearlyData().isEmpty()) {
            sb.append("YEARLY SUMMARY:\n");
            sb.append("-" .repeat(40)).append("\n");
            sb.append(String.format("%-6s | %-15s | %-15s\n", "Year", "Balance", "Interest"));
            sb.append("-" .repeat(40)).append("\n");
            
            for (YearlyData yearly : result.getYearlyData()) {
                sb.append(String.format("%-6d | $%,-14.2f | $%,-14.2f\n",
                    yearly.getYear(),
                    yearly.getEndBalance(),
                    yearly.getInterestEarned()));
            }
        }
        
        sb.append("\n");
        sb.append("=" .repeat(60)).append("\n");
        sb.append("End of Report\n");
        sb.append("=" .repeat(60)).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Appends results to existing file
     */
    public static boolean appendResultsToFile(InvestmentResult result, String filename) {
        if (result == null) {
            return false;
        }
        
        String targetFilename = (filename != null && !filename.isEmpty()) ? filename : DEFAULT_FILENAME;
        File file = new File(targetFilename);
        
        try {
            String content = "\n\n" + formatResultsForFile(result);
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8, true);
            return true;
        } catch (IOException e) {
            System.err.println("Error appending results to file: " + e.getMessage());
            return false;
        }
    }
}
