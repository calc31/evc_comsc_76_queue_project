import java.util.Random;

public class Customer {
    private int arrivalSecond;
    private int numItems;
    private int paymentTime;
    private int queueEntrySecond;
    private int startCheckoutSecond;

    public Customer(int arrivalSecond) {
        Random random = new Random();
        this.arrivalSecond = arrivalSecond;
        this.numItems = random.nextInt(35) + 1;         // 1–35 items
        this.paymentTime = random.nextInt(21) + 15;     // 15–35 seconds
    }

    public int getArrivalSecond() {
        return arrivalSecond;
    }

    public int getNumItems() {
        return numItems;
    }

    public int getPaymentTime() {
        return paymentTime;
    }

    public void setQueueEntrySecond(int second) {
        this.queueEntrySecond = second;
    }

    public void setStartCheckoutSecond(int second) {
        this.startCheckoutSecond = second;
    }

    public int getWaitTime() {
        return startCheckoutSecond - queueEntrySecond;
    }

    public int getTotalCheckoutTime() {
        Random random = new Random();
        int scanTime = 0;
        for (int i = 0; i < numItems; i++) {
            scanTime += random.nextInt(3) + 4;  // 4–6 seconds per item
        }
        return scanTime + paymentTime;
    }
}