package testerport;

import java.io.IOException;

public class Report extends GenerateReport {
    private static final String TEST_SPEC = "testspec/powertest.xlsx";

    public Report() throws IOException {
        super(TEST_SPEC);
    }
}
