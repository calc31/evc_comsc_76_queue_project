import java.util.Random;

public class Checkout {
    public static void main(String[] args) {
        // Your code here
    }
}

class Customer {
    // private int arrivalSecond;
    private final int numItems;
    private int paymentTime;
    private int queueEntrySecond;
    private int startCheckoutSecond;

    public Customer(int queueEntrySecond) {
        Random random = new Random();
        this.queueEntrySecond = queueEntrySecond;
        this.numItems = random.nextInt(1, 36);         // 1–35 items
        this.paymentTime = random.nextInt(15, 34);     // 15–35 seconds
    }

    /*public int getArrivalSecond() {
        return this.arrivalSecond;
    }*/

    public int getNumItems() {
        return this.numItems;
    }

    public int getPaymentTime() {
        return this.paymentTime;
    }

    public void setQueueEntrySecond(int second) {
        this.queueEntrySecond = second;
    }

    public void setStartCheckoutSecond(int second) {
        this.startCheckoutSecond = second;
    }

    public int getWaitTime() {
        return this.startCheckoutSecond - this.queueEntrySecond;
    }

    public int getTotalCheckoutTime() {
        Random random = new Random();
        int scanTime = 0;
        for (int i = 0; i < this.numItems; i++) {
            scanTime += random.nextInt(3) + 4;  // 4–6 seconds per item
        }
        return scanTime + this.paymentTime;
    }
}

class CheckoutStation {
    private Customer currentCustomer;
    private int secondsRemaining;

    public boolean isAvailable() {
        return this.currentCustomer == null;
    }

    public void assignCustomer(Customer customer, int currentSecond) {
        this.currentCustomer = customer;
        customer.setStartCheckoutSecond(currentSecond);
        this.secondsRemaining = customer.getTotalCheckoutTime();
    }

    public void tick() {
        if (this.currentCustomer != null) {
            this.secondsRemaining--;
            if (secondsRemaining <= 0) {
                this.currentCustomer = null;
            }
        }
    }

    /*public boolean isBusy() {
        return currentCustomer != null;
    }*/
}

class StatisticsTracker {
    private int totalCustomersServed = 0;
    private double totalWaitTimeSeconds = 0;
    private int maxQueueLength = 0;

    public void recordCustomer(Customer newCustomer) {
        this.totalCustomersServed++;
        this.totalWaitTimeSeconds += newCustomer.getWaitTime();
    }

    public void updateMaxQueue(int currentQueueSize) {
        if (currentQueueSize > this.maxQueueLength) {
            this.maxQueueLength = currentQueueSize;
        }
    }

    public int getTotalCustomersServed() {
        return this.totalCustomersServed;
    }

    public double getAverageWaitTime() {
        if (this.totalCustomersServed == 0) return 0;
        return this.totalWaitTimeSeconds / this.totalCustomersServed;
    }

    public int getMaxQueueLength() {
        return this.maxQueueLength;
    }
}
