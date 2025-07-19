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
        final int customerArrivalRate = 10; // new customer every 10 seconds

        // Each station has its own queue
        Queue<Customer>[] stationQueues = new Queue[numStations];
        CheckoutStation[] stations = new CheckoutStation[numStations];
        for (int i = 0; i < numStations; i++) {
            stationQueues[i] = new Queue<>();
            stations[i] = new CheckoutStation();
        }

        StatisticsTracker tracker = new StatisticsTracker();
        int nextArrivalTime = 0;

        for (int currentSecond = 0; currentSecond < simulationDuration; currentSecond++) {
            // New customer arrives at fixed intervals
            if (currentSecond >= nextArrivalTime) {
                Customer customer = new Customer(currentSecond);

                // Find the line with the fewest customers
                int shortestLineIndex = 0;
                int shortestSize = stationQueues[0].size();
                for (int i = 1; i < numStations; i++) {
                    int queueSize = stationQueues[i].size();
                    if (queueSize < shortestSize) {
                        shortestLineIndex = i;
                        shortestSize = queueSize;
                    }
                }

                // Add the customer to the shortest queue
                stationQueues[shortestLineIndex].enqueue(customer);

                // Update max queue length tracker
                tracker.updateMaxQueue(stationQueues[shortestLineIndex].size());

                nextArrivalTime += customerArrivalRate;
            }

            // For each station: if available, serve next customer from its queue
            for (int i = 0; i < numStations; i++) {
                if (stations[i].isAvailable() && !stationQueues[i].isEmpty()) {
                    Customer nextCustomer = stationQueues[i].dequeue();
                    stations[i].assignCustomer(nextCustomer, currentSecond);
                    tracker.recordCustomer(nextCustomer);
                }
            }

            // Tick each checkout station
            for (CheckoutStation station : stations) {
                station.tick();
            }
        }

        // Print final stats
        System.out.println("=== Model 2: Each Station Has Its Own Line; customers choose the shortest line ===");
        System.out.println("Total customers served: " + tracker.getTotalCustomersServed());
        System.out.printf("Average wait time: %.2f seconds%n", tracker.getAverageWaitTime());
        System.out.println("Maximum queue length observed in any line: " + tracker.getMaxQueueLength());
    }

    public void Model3() {
    final int simulation_duration = 2 * 60 * 60; // two hours in seconds
    final int num_stations = 5;
    final int new_customer_arrival_rate = 10;

    Queue<Customer>[] lines = new Queue[num_stations];
    CheckoutStation[] stations = new CheckoutStation[num_stations];
    StatisticsTracker tracker = new StatisticsTracker();
    Random random = new Random();

    for (int i = 0; i < num_stations; i++) {
        lines[i] = new Queue<>();
        stations[i] = new CheckoutStation();
    }

    for (int currentSecond = 0; currentSecond < simulation_duration; currentSecond++) {
        // Fixed-rate customer arrival
        if (currentSecond % new_customer_arrival_rate == 0) {
            Customer customer = new Customer(currentSecond);  // uses existing constructor
            int randomLine = random.nextInt(num_stations);    // randomly pick a line
            lines[randomLine].enqueue(customer);
        }

        // Assign customers from each line to their station
        for (int i = 0; i < num_stations; i++) {
            if (stations[i].isAvailable() && !lines[i].isEmpty()) {
                Customer nextCustomer = lines[i].dequeue();
                stations[i].assignCustomer(nextCustomer, currentSecond);
                tracker.recordCustomer(nextCustomer);
            }
        }

        // Tick each checkout station
        for (int i = 0; i < num_stations; i++) {
            stations[i].tick();
        }

        // Update max queue length tracker
        for (int i = 0; i < num_stations; i++) {
            tracker.updateMaxQueue(lines[i].size());
        }
    }

    // Output results
    System.out.println("=== Model 3: One Line Per Station ===");
    System.out.println("Total customers served: " + tracker.getTotalCustomersServed());
    System.out.printf("Average wait time: %.2f seconds\n", tracker.getAverageWaitTime());
    System.out.println("Max queue length in any line: " + tracker.getMaxQueueLength());
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
        this.numItems = random.nextInt(10, 36);         // 10–35 items
        this.paymentTime = random.nextInt(15, 46);     // 15–45 seconds
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
