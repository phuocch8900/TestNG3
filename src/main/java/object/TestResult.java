package object;

public class TestResult {
    private int testId;
    private String status;

    public TestResult(int testId, String status) {
        this.testId = testId;
        this.status = status;
    }

    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof TestResult) {
            TestResult result = (TestResult) obj;
            if (this.getTestId() == result.getTestId() && this.getStatus().equals(result.getStatus())) {
                return true;
            }
        }
        return false;
    }
}
