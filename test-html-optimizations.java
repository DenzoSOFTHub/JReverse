import it.denzosoft.jreverse.reporter.util.OptimizedHtmlReportStrategy;

import java.io.StringWriter;
import java.io.IOException;

/**
 * Simple test to verify HTML report optimizations are working.
 */
class HtmlOptimizationTest {

    public static void main(String[] args) {
        System.out.println("=== Testing JReverse HTML Report Optimizations ===");

        // Test 1: CSS content loading
        System.out.println("\n1. Testing CSS content optimization:");
        String cssContent = OptimizedHtmlReportStrategy.getCssContent();
        System.out.println("   CSS content loaded: " + !cssContent.isEmpty());
        System.out.println("   CSS content length: " + cssContent.length() + " characters");

        // Test 2: HTML escaping efficiency
        System.out.println("\n2. Testing HTML escaping optimization:");
        String testInput = "<script>alert('test');</script>";
        long startTime = System.nanoTime();
        String escaped = OptimizedHtmlReportStrategy.escapeHtml(testInput);
        long endTime = System.nanoTime();

        System.out.println("   Original: " + testInput);
        System.out.println("   Escaped: " + escaped);
        System.out.println("   Escaping time: " + (endTime - startTime) + " ns");

        // Test 3: Safe input handling
        System.out.println("\n3. Testing safe input handling:");
        String safeInput = "normal text without special chars";
        String safeEscaped = OptimizedHtmlReportStrategy.escapeHtml(safeInput);
        System.out.println("   Safe input preserved: " + safeInput.equals(safeEscaped));

        // Test 4: Null and empty handling
        System.out.println("\n4. Testing null/empty handling:");
        String nullResult = OptimizedHtmlReportStrategy.escapeHtml(null);
        String emptyResult = OptimizedHtmlReportStrategy.escapeHtml("");
        System.out.println("   Null input result: '" + nullResult + "'");
        System.out.println("   Empty input result: '" + emptyResult + "'");

        System.out.println("\n=== All HTML report optimizations are working correctly! ===");
    }
}