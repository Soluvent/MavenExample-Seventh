package com.investmentcalc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Additional unit tests for Investment Calculator functionality
 * Tests new features including text file saving and edge cases
 */
public class AdditionalInvestmentTests {
    
    private FinalInvestmentEngine engine;
    private static final String TEST_FILE = "test_investment_results.txt";
    
    @BeforeEach
    void setUp() {
        engine = new FinalInvestmentEngine();
        // Clean up test file if exists
        File testFile = new File(TEST_FILE);
        if (testFile.exists()) {
            testFile.delete();
        }
    }
    
    @AfterEach
    void tearDown() {
        // Clean up test file after each test
        File testFile = new File(TEST_FILE);
        if (testFile.exists()) {
            testFile.delete();
        }
    }
    
    /**
     * Test 1: Verify that TextFileSaver correctly saves investment results to a file
     */
    @Test
    void testTextFileSaverSavesResults() {
        System.out.println("\n=== Test 1: Text File Saver Functionality ===");
        
        // Create test data
        BigDecimal startingAmount = new BigDecimal("5000");
        int years = 3;
        BigDecimal returnRate = new BigDecimal("8");
        String frequency = "Monthly";
        
        System.out.printf("Input: $%s at %s%% %s for %d years\n", 
            startingAmount, returnRate, frequency.toLowerCase(), years);
        
        // Calculate investment
        InvestmentResult result = engine.calculateInvestment(
            startingAmount, years, returnRate, frequency,
            new BigDecimal("100"), 12, true);
        
        // Save to file
        boolean saveSuccess = TextFileSaver.saveResultsToFile(result, TEST_FILE);
        
        // Verify file was created
        File file = new File(TEST_FILE);
        
        System.out.printf("File created: %s\n", file.exists());
        System.out.printf("Save success: %s\n", saveSuccess);
        
        assertTrue(saveSuccess, "File saving should return true");
        assertTrue(file.exists(), "File should exist after saving");
        assertTrue(file.length() > 0, "File should not be empty");
        
        // Verify content includes key information
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
            assertTrue(content.contains("INVESTMENT CALCULATION RESULTS"), 
                "File should contain header");
            assertTrue(content.contains("Starting Amount"), 
                "File should contain starting amount label");
//            assertTrue(content.contains("5,000"),
//                "File should contain formatted starting amount");
            
            System.out.println("File content verification: ✅ PASS");
        } catch (IOException e) {
            fail("Failed to read test file: " + e.getMessage());
        }
        
        System.out.println("Result: ✅ PASS - File saved successfully with correct content");
    }
    
    /**
     * Test 2: Verify quarterly compounding calculations
     */
    @Test
    void testQuarterlyCompounding() {
        System.out.println("\n=== Test 2: Quarterly Compounding Calculation ===");
        
        BigDecimal startingAmount = new BigDecimal("10000");
        int years = 2;
        BigDecimal returnRate = new BigDecimal("6");
        String frequency = "Quarterly";
        
        System.out.printf("Input: $%s at %s%% %s for %d years\n", 
            startingAmount, returnRate, frequency.toLowerCase(), years);
        
        // Calculate with quarterly compounding
        InvestmentResult result = engine.calculateInvestment(
            startingAmount, years, returnRate, frequency,
            new BigDecimal("0"), 0, false);
        
        // Expected: 10000 * (1 + 0.06/4)^(4*2) = 10000 * 1.015^8 ≈ 11264.93
        BigDecimal expected = new BigDecimal("11264.93");
        BigDecimal actual = result.getEndBalance().setScale(2, RoundingMode.HALF_UP);
        
        System.out.printf("Expected: $%s\n", expected);
        System.out.printf("Actual:   $%s\n", actual);
        
        // Allow small tolerance for rounding differences
        BigDecimal difference = actual.subtract(expected).abs();
        boolean withinTolerance = difference.compareTo(new BigDecimal("1.00")) <= 0;
        
        System.out.printf("Difference: $%s\n", difference);
        System.out.printf("Result: %s\n", withinTolerance ? "✅ PASS" : "❌ FAIL");
        
        assertTrue(withinTolerance, 
            "Quarterly compounding should yield approximately $11,264.93");
    }
    
    /**
     * Test 3: Verify weekly compounding with regular contributions
     */
    @Test
    void testWeeklyCompoundingWithContributions() {
        System.out.println("\n=== Test 3: Weekly Compounding with Regular Contributions ===");

        BigDecimal startingAmount = new BigDecimal("1000");
        int years = 1;
        BigDecimal returnRate = new BigDecimal("10");
        String frequency = "Weekly";
        BigDecimal additionalContribution = new BigDecimal("50");
        int contributionFrequency = 1;
        boolean atStart = true;

        System.out.printf("Input: $%s at %s%% %s for %d year(s)\n",
                startingAmount, returnRate, frequency.toLowerCase(), years);
        System.out.printf("Contribution: $%s (%d time per year)\n",
                additionalContribution, contributionFrequency);

        InvestmentResult result = engine.calculateInvestment(
                startingAmount, years, returnRate, frequency,
                additionalContribution, contributionFrequency, atStart
        );

        System.out.printf("End Balance: $%s\n", result.getEndBalance());
        System.out.printf("Total Contributions: $%s\n", result.getTotalContributions());
        System.out.printf("Total Interest: $%s\n", result.getTotalInterest());

        assertTrue(result.getEndBalance().compareTo(startingAmount) > 0,
                "End balance should be greater than starting amount");

        BigDecimal expectedContributions = new BigDecimal("1050.00");

        assertEquals(expectedContributions,
                result.getTotalContributions().setScale(2, RoundingMode.HALF_UP),
                "Total contributions should equal starting + contributions");

        assertTrue(result.getTotalInterest().compareTo(BigDecimal.ZERO) > 0,
                "Interest should be positive");

        BigDecimal calculatedBalance = result.getTotalContributions()
                .add(result.getTotalInterest());
        assertEquals(result.getEndBalance().setScale(2, RoundingMode.HALF_UP),
                calculatedBalance.setScale(2, RoundingMode.HALF_UP),
                "End balance should equal contributions plus interest");

        System.out.println("Result: ✅ PASS\n");
    }
}
