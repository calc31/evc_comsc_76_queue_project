import java.util.Random;

public class Checkout {
    public static void main(String[] args) {
        Checkout simulation = new Checkout();
        simulation.Model1();
        //simulation.Model2();
        //simulation.Model3();
    }

    public void Model1() {
        final int simulation_duration = 2 * 60 * 60; // two hours in seconds
        final int num_stations = 5; // change this to experiment

        Queue<Customer> customerLine = new Queue<>();

        CheckoutStation[] stations = new CheckoutStation[num_stations];
        for (int i = 0; i < num_stations; i++) {
            stations[i] = new CheckoutStation();
        }
    }

      public void Model2() {
            final int simulationDuration = 2 * 60 * 60; // two hours in seconds
            final int numStations = 5;
            final int customerArrivalRate = 25; // new customer every 25 seconds

            CheckoutStation[] stations = new CheckoutStation[numStations];
            for (int i = 0; i < numStations; i++) {
                stations[i] = new CheckoutStation();
            }

            StatisticsTracker tracker = new StatisticsTracker();
            Random random = new Random();

            int nextArrivalTime = 0;

            for (int currentSecond = 0; currentSecond < simulationDuration; currentSecond++) {
                // New customer arrives exactly every 25 seconds
                if (currentSecond >= nextArrivalTime) {
                    Customer customer = new Customer(currentSecond);

                    // Choose the station with the fewest customers waiting or being served
                    CheckoutStation targetStation = stations[0];
                    int minQueue = targetStation.isAvailable() ? 0 : 1;

                    for (int i = 1; i < numStations; i++) {
                        int queueSize = stations[i].isAvailable() ? 0 : 1;
                        if (queueSize < minQueue) {
                            targetStation = stations[i];
                            minQueue = queueSize;
                        }
                    }

                    if (targetStation.isAvailable()) {
                        targetStation.assignCustomer(customer, currentSecond);
                        tracker.recordCustomer(customer);
                    } else {
                        // all stations are busy; randomly pick one to enqueue into shared waiting queue
                        // for this simple model, we'll skip actual queueing to keep it direct,
                        // or you can add a waiting queue if desired
                    }

                    nextArrivalTime += customerArrivalRate;
                }

                // Tick all stations
                for (CheckoutStation station : stations) {
                    station.tick();
            }
            

            // Final statistics
            System.out.println("=== Model 2: Each Station with Own Line (next available) ===");
            System.out.println("Total customers served: " + tracker.getTotalCustomersServed());
            System.out.printf("Average wait time: %.2f seconds\n", tracker.getAverageWaitTime());
            System.out.println("Maximum queue length: " + tracker.getMaxQueueLength());
    
    }

    public void Model3() {
    final int simulation_duration = 2 * 60 * 60; // two hours in seconds
    final int num_stations = 5; // adjust to test different numbers of stations

    Queue<Customer> sharedQueue = new Queue<>();
    CheckoutStation[] stations = new CheckoutStation[num_stations];
    for (int i = 0; i < num_stations; i++) {
        stations[i] = new CheckoutStation();
    }

    StatisticsTracker tracker = new StatisticsTracker();
    Random random = new Random();

    // 2. Simulation loop
    for (int currentSecond = 0; currentSecond < simulation_duration; currentSecond++) {
        // New customer arrives with 1 in 3 chance
        if (random.nextInt(3) == 0) {
            sharedQueue.enqueue(new Customer(currentSecond));
        }

        // Assign customers to available stations
        for (int i = 0; i < num_stations; i++) {
            if (stations[i].isAvailable() && !sharedQueue.isEmpty()) {
                Customer customer = sharedQueue.dequeue();
                stations[i].assignCustomer(customer, currentSecond);
                tracker.recordCustomer(customer);
            }
        }

        // Tick all stations
        for (int i = 0; i < num_stations; i++) {
            stations[i].tick();
        }

        // Update max queue length
        tracker.updateMaxQueue(sharedQueue.size());
    }

    // 3. Final statistics
    System.out.println("=== Model 3: Shared Queue for All Stations ===");
    System.out.println("Total customers served: " + tracker.getTotalCustomersServed());
    System.out.printf("Average wait time: %.2f seconds\n", tracker.getAverageWaitTime());
    System.out.println("Maximum queue length: " + tracker.getMaxQueueLength());

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
