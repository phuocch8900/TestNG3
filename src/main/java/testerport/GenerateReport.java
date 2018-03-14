package testerport;


import object.PropertyLoader;
import object.TestResult;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenerateReport implements IReporter {
    private Logger logger = Logger.getLogger(GenerateReport.class);
    private Map<String, String> testSheet;
    private Map<String, List<TestResult>> testResults;
    private XSSFWorkbook testSpec;
    private String specFile;
    private CellStyle cellStyle;

    private static final String PASSED = "OK";
    private static final String FAILED = "NG";
    private static final String SKIPPED = "NT";

    private static final String RESULT_HEADER = "Result";
    private PropertyLoader propertyLoader = PropertyLoader.initialize();

    public GenerateReport(String input) throws IOException {
        specFile = input;

        testSheet = new HashMap<>();
        testSheet.put("power.PowerCode", "power");

        try (FileInputStream testSpecFile = new FileInputStream(new File(specFile))) {
            testSpec = new XSSFWorkbook(testSpecFile);
        }

        cellStyle = testSpec.createCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);

        testResults = new HashMap<>();
        testResults.put("power", new ArrayList<>());

    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

        //Iterating over each suite included in the test
        for (ISuite suite : suites) {

            //Getting the results for the said suite
            Map suiteResults = suite.getResults();
            for (Object sr : suiteResults.values()) {
                ITestContext testContext = ((ISuiteResult) sr).getTestContext();
                this.generate(testContext);
            }
        }
    }

    private void generate(ITestContext testContext) {
        try (FileOutputStream fout = new FileOutputStream(specFile)) {

            // List testcase results map by test class
            this.mapResultByTestName(testContext.getPassedTests().getAllResults(), PASSED);
            this.mapResultByTestName(testContext.getFailedTests().getAllResults(), FAILED);
            this.mapResultByTestName(testContext.getSkippedTests().getAllResults(), SKIPPED);

            // Update test spec
            for (Map.Entry<String, List<TestResult>> entityTestSheet : testResults.entrySet()) {
                List<TestResult> listResult = entityTestSheet.getValue();

                List<TestResult> listExpectedResult;
                listExpectedResult = listResult.stream().filter(distinctByKey(TestResult::getTestId)).collect(Collectors.toList());

                if (listResult.size() != listExpectedResult.size()) {
                    for (TestResult result : listExpectedResult) {
                        TestResult resultCheckFail = new TestResult(result.getTestId(), FAILED);

                        if (listResult.contains(resultCheckFail)) {
                            result.setStatus(FAILED);
                            continue;
                        }
                        TestResult resultCheckSkip = new TestResult(result.getTestId(), SKIPPED);

                        if (listResult.contains(resultCheckSkip)) {
                            result.setStatus(SKIPPED);
                        }
                    }
                }

                this.updateTestResult(entityTestSheet.getKey(), listExpectedResult);
            }


            // Formula to summary testcase (total number of passed, failed and skipped cases)
            XSSFFormulaEvaluator.evaluateAllFormulaCells(testSpec);
            testSpec.write(fout);
            logger.info(String.format("%s has been updated.", specFile));
        } catch (IOException e) {
            logger.error("Unexpected exception: ", e);
        }

    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private Cell getCellByData(Sheet sheet, String data) {

        for (Row currentRow : sheet) {

            for (Cell currentCell : currentRow) {
                if (currentCell.getCellType() == Cell.CELL_TYPE_STRING && currentCell.getStringCellValue().equalsIgnoreCase(data)) {
                    return currentCell;
                }
            }
        }
        return null;
    }

    private void updateTestResult(String sheetName, List<TestResult> results) throws MalformedInputException {

        // Sort list to update correct data in report
        results.sort(Comparator.comparingInt(TestResult::getTestId));

        Sheet classSheet = testSpec.getSheet(sheetName);

        if (classSheet == null) {
            return;
        }

        Cell resultHeader = getCellByData(classSheet, RESULT_HEADER);

        if (resultHeader == null) {
            throw new MalformedInputException(1);
        }

        int rowNum = resultHeader.getRowIndex() + 1;
        for (TestResult result : results) {

            Row row = classSheet.getRow(rowNum);
            Cell cell = row.createCell(resultHeader.getColumnIndex());
            cell.setCellStyle(cellStyle);
            cell.setCellValue(result.getStatus());

            rowNum += getMergedNumber(cell);
        }
    }

    private int getMergedNumber(Cell cell) {

        Sheet sheet = cell.getSheet();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {

            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {

                return range.getLastRow() - range.getFirstRow() + 1;
            }
        }
        return 1;
    }

    private void mapResultByTestName(Set<ITestResult> results, String status) {
        String testName;
        TestResult testResult;
        for (ITestResult result : results) {
            testResult = new TestResult(this.getTestId(result.getName()), status);
            String sheet = testSheet.get(result.getInstanceName());
            if (sheet != null) {
                testResults.get(sheet).add(testResult);
            }
        }
    }

    //get Id
    private int getTestId(String testName) {
        Pattern p = Pattern.compile("[0-9]+$");
        Matcher m = p.matcher(testName);
        if (m.find()) {
            return Integer.parseInt(m.group());
        }
        return -1;
    }
}
