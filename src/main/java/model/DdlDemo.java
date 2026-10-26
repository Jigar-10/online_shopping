package model;

public class DdlDemo {
    private Integer demoId;
    private String demoName;
    private Integer demoValue;

    public DdlDemo() {}

    public DdlDemo(Integer demoId, String demoName, Integer demoValue) {
        this.demoId = demoId;
        this.demoName = demoName;
        this.demoValue = demoValue;
    }

    public Integer getDemoId() {
        return demoId;
    }

    public void setDemoId(Integer demoId) {
        this.demoId = demoId;
    }

    public String getDemoName() {
        return demoName;
    }

    public void setDemoName(String demoName) {
        this.demoName = demoName;
    }

    public Integer getDemoValue() {
        return demoValue;
    }

    public void setDemoValue(Integer demoValue) {
        this.demoValue = demoValue;
    }

    @Override
    public String toString() {
        return "Demo #" + demoId + " - " + demoName;
    }
}
