public class StatisticsTracker {
    private int totalCustomersServed = 0;
    private int totalWaitTimeSeconds = 0;
    private int maxQueueLength = 0;

    public void recordCustomer(int waitTimeSeconds) {
        totalCustomersServed++;
        totalWaitTimeSeconds += waitTimeSeconds;
    }

    public void updateMaxQueue(int currentQueueSize) {
        if (currentQueueSize > maxQueueLength) {
            maxQueueLength = currentQueueSize;
        }
    }

    public int getTotalCustomersServed() {
        return totalCustomersServed;
    }

    public double getAverageWaitTime() {
        if (totalCustomersServed == 0) return 0;
        return (double) totalWaitTimeSeconds / totalCustomersServed;
    }

    public int getMaxQueueLength() {
        return maxQueueLength;
    }
}