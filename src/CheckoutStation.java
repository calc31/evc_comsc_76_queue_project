public class CheckoutStation {
    private Customer currentCustomer;
    private int secondsRemaining;

    public boolean isAvailable() {
        return currentCustomer == null;
    }

    public void assignCustomer(Customer customer, int currentSecond) {
        this.currentCustomer = customer;
        customer.setStartCheckoutSecond(currentSecond);
        this.secondsRemaining = customer.getTotalCheckoutTime();
    }

    public void tick() {
        if (currentCustomer != null) {
            secondsRemaining--;
            if (secondsRemaining <= 0) {
                currentCustomer = null;
            }
        }
    }

    public boolean isBusy() {
        return currentCustomer != null;
    }
}