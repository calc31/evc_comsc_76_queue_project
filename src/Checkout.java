import java.util.Random;

public class Checkout {
    public static void main(String[] args) {
        Checkout simulation = new Checkout();
        simulation.Model1();
        System.out.print("\n");
        simulation.Model2();
        System.out.print("\n");
        simulation.Model3();
    }

    public void Model1() {
        final int simulation_duration = 2 * 60 * 60; // two hours in seconds
        final int num_stations = 5; // change this to experiment
        final int new_customer_arrival_rate = 30; // new customer every 30 seconds

        // Create an empty queue
        Queue<Customer> customer_line = new Queue<>();

        // Create checkout stations
        CheckoutStation[] stations = new CheckoutStation[num_stations];
        for (int i = 0; i < num_stations; i++) {
            stations[i] = new CheckoutStation();
        }

        StatisticsTracker stats = new StatisticsTracker();
        int next_customer_arrival_time = 0;

        // Run the simulation, tick by tick
        for (int currentSecond = 0; currentSecond < simulation_duration; currentSecond++) {

            // Add a new customer to the queue at a certain frequency
            if (currentSecond >= next_customer_arrival_time) {
                Customer newCustomer = new Customer(currentSecond);
                newCustomer.setQueueEntrySecond(currentSecond);
                customer_line.enqueue(newCustomer);
                next_customer_arrival_time += new_customer_arrival_rate;
            }

            // Update max queue length for stats
            stats.updateMaxQueue(customer_line.size());

            // Assign waiting customers to available stations
            for (CheckoutStation station : stations) {
                if (station.isAvailable() && !customer_line.isEmpty()) {
                    Customer nextCustomer = customer_line.dequeue();
                    station.assignCustomer(nextCustomer, currentSecond);
                    stats.recordCustomer(nextCustomer);
                }
            }

            // Advance time in each station
            for (CheckoutStation station : stations) {
                station.tick();
            }
        }

        // Report results
        System.out.println("=== Model 1: One customer line; n checkout stations. " +
                "Customers go to next available station.");
        System.out.println("Total customers served: " + stats.getTotalCustomersServed());
        System.out.println("Maximum queue length: " + stats.getMaxQueueLength());
        System.out.printf("Average wait time: %.2f seconds%n", stats.getAverageWaitTime());
        // System.out.printf("Average wait time: %.2f minutes%n", stats.getAverageWaitTime() / 60.0);
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
        Queue<Customer> waitingQueue = new Queue<>();
        int nextArrivalTime = 0;

        for (int currentSecond = 0; currentSecond < simulationDuration; currentSecond++) {
            // New customer arrives exactly every 25 seconds
            if (currentSecond >= nextArrivalTime) {
                Customer customer = new Customer(currentSecond);

                // Choose the station with the fewest customers waiting or being served
                CheckoutStation targetStation = null;

                for (int i = 0; i < numStations; i++) {
                    if (stations[i].isAvailable()) {
                        targetStation = stations[i];
                        break;
                    }
                }

                if (targetStation != null) {
                    targetStation.assignCustomer(customer, currentSecond);
                    tracker.recordCustomer(customer);
                } else {
                    waitingQueue.enqueue(customer); // enqueue if all stations are busy
                }

                nextArrivalTime += customerArrivalRate;
            }

        // Assign customers from waiting queue to available stations
        for (int i = 0; i < numStations; i++) {
            if (stations[i].isAvailable() && !waitingQueue.isEmpty()) {
                Customer customer = waitingQueue.dequeue();
                stations[i].assignCustomer(customer, currentSecond);
                tracker.recordCustomer(customer);
            }
        }

        // Tick all stations
        for (CheckoutStation station : stations) {
            station.tick();
        }

        // Track max queue length
        tracker.updateMaxQueue(waitingQueue.size());
    }

    // Final statistics
    System.out.println("=== Model 2: Each Station with Own Line (shared wait queue added) ===");
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
