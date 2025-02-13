package deliverable;

import deliverable.control.ExecutionFlow;

public class App {
    public static void main(String[] args) throws Exception {
        ExecutionFlow.analyzeProject("bookkeeper");
        ExecutionFlow.analyzeProject("zookeeper");

    }
}
