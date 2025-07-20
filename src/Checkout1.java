/*
 * Names: Shankar Subramaniam, Nehan Armin, Divsheen Kaur, Rithvik Yarra, Sahiti Rayaprolu, and Tu Nguyen.
 * Date: July 20, 2025
 *
 * Description:
 * This program simulates a grocery store checkout system using three different models.
 * It runs each model for two hours with a 5 checkout
 * stations and a fixed customer arrival rate. Customers have varying numbers of items (10-35) and
 * payment times (15-45), and each checkout station processes customers one at a time.
 *
 * The three models are:
 *    Model 1: One line for customers, with n checkout stations. Customers go to the next available station.
 *    Model 2: n lines for customers, with one checkout station per line. Customers go to the line with the
 *             fewest number of customers.
 *    Model 3: n lines for customers, with one checkout station per line. Customers go to a randomly chosen line.
 *
 * After each simulation, statistics are displayed, including total customers served, average
 * wait time, and the maximum queue length observed.
 */

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * A checkout system model
 */
public class Checkout {
    // A random generator
    private static final Random RAND = new Random();

    // enable debug logging, if set to true
    // logs every second
    private static final boolean ENABLE_DEBUG_LOGGING = false;

    // model parameters
    final static int MODEL_RUN_TIME = 7200;  // run time
    final static int CUSTOMER_ARRIVAL_TIME_IN_SEC  = 30; // customer inter arrival time
    final static int MIN_ITEMS_TO_CHECKOUT = 1; // min items a customer checks out
    final static int MAX_ITEMS_TO_CHECKOUT = 20; // max items a customer checks out
    final static int CHECKOUT_STATION_COUNT = 5;       // checkout count
    // time it takes to check out a single item (min and max)
    private static final int CHECKOUT_DURATION_PER_ITEM_SECOND_MIN = 8;
    private static final int CHECKOUT_DURATION_PER_ITEM_SECOND_MAX = 10;
    // time it takes to pay (min and max)
    private static final int PAY_DURATION_SECOND_MIN = 10;
    private static final int PAY_DURATION_SECOND_MAX = 30;

    /**
     * Class representing a customer
     */
    private static class Customer {
        final long id;                          // customer ID / nth costumer
        final int timeOfArrival;                // arrival time of the customer
        final int timeSpentAtCheckoutInSec;     // time spent at a checkout

        Customer(final long id, final int timeOfArrival, final int numItemsToPurchase,
                 final int checkoutDurationPerItemInSec, final int paymentDurationInSec) {
            this.id = id;
            this.timeOfArrival = timeOfArrival;
            this.timeSpentAtCheckoutInSec = paymentDurationInSec + numItemsToPurchase * checkoutDurationPerItemInSec;
        }

        /**
         * @return total time spent to check out
         */
        int computeTimeSpentAtCheckInSecond() {
            return this.timeSpentAtCheckoutInSec;
        }
    }

    private static String printWaitingQueues(final List<Deque<Customer>> waitingQueues) {
        final StringBuilder sb = new StringBuilder().append("=[");
        boolean isFirst = true;
        for (final Queue<Customer> queue : waitingQueues) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(",");
            }
            sb.append(queue.size());
        }
        return sb.append("]").toString();
    }

    private static String printStations(final ArrayList<Integer> checkoutCompletionTime, final int now) {
        final StringBuilder sb = new StringBuilder().append("=[");
        boolean isFirst = true;
        for (final Integer completionTime : checkoutCompletionTime) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(",");
            }
            sb.append(completionTime == null ? "F" : (completionTime - now));
        }
        return sb.append("]").toString();

    }

    /**
     * Model 1
     * customer waiting queue, N checkout
     * Assuming:
     * 1. if one+ checkout stations are free, a customer always choose the station with the lower index
     *
     * @param runTime                          runtime of the simulation in sec
     * @param customerInterArrivalTimeInSec    inter arrival time of customers (avg)
     * @param numCheckoutStations              number of checkout kiosks
     * @param minNumItems                      minimum number of items to check out
     * @param maxNumItems                      maximum number of items to check out
     */
    private static void model1QueueNCheckout(final int runTime, final int customerInterArrivalTimeInSec,
                                             final int numCheckoutStations,
                                             final int minNumItems, final int maxNumItems) {
        // ----------------------------- model variables -----------------------------
        // chance of a customer arrival in a given second
        final double customerArrivalChance = 1.0 / customerInterArrivalTimeInSec;
        // customer waits in this queue
        final Deque<Customer> waitingQueue = new LinkedList<>();
        // represents checkout stations (the customer there)
        final ArrayList<Customer> stations = new ArrayList<>(numCheckoutStations);
        // customer checkout start/completion time; a null value means free
        final ArrayList<Integer> checkoutCompletionTime = new ArrayList<>(numCheckoutStations);
        for (int i = 0; i < numCheckoutStations; ++i) {
            stations.add(null);             // no customer at checkout
            checkoutCompletionTime.add(null);
        }

        // ----------------------------- stats variables -----------------------------
        long customerCount = 0;                  // count number customers arrived to the store
        long customerMovedToCheckoutCont = 0;    // count number of customers moved to checkout after waiting in the queue
        long customerMovingOutCount = 0;         // count number of customers leaving the store
        long maxCustomerWaitingCount = 0;        // tracks max number of customers waiting in the waiting queue
        long maxCustomerInStoreCount = 0;        // tracks max number of customers in the store
        long weightedCustomersInQueue = 0;       // sum up number of customers waiting in the queue each sec
        long weightedCustomersInStore = 0;       // sum up number of customers waiting in the store each sec
        long weightedCheckoutBusyTime = 0;       // sum up number of busy stations each sec
        long weightedWaitingTime = 0;            // sum up the time spent in the waiting queue by all customers
        long weightedTimeInStore = 0;            // sum up the time spent in the store by all customers

        // ----------------------------- run model -----------------------------
        for (int now = 0; now < runTime; ++now) {
            // ========== let a customer arrive ==========
            if (RAND.nextDouble() < customerArrivalChance) {
                ++customerCount;

                // create the customer
                final Customer newCustomer = new Customer(customerCount, now,
                        minNumItems + RAND.nextInt(maxNumItems - minNumItems),
                        CHECKOUT_DURATION_PER_ITEM_SECOND_MIN + RAND.nextInt(CHECKOUT_DURATION_PER_ITEM_SECOND_MAX - CHECKOUT_DURATION_PER_ITEM_SECOND_MIN),
                        PAY_DURATION_SECOND_MIN + RAND.nextInt(PAY_DURATION_SECOND_MAX - PAY_DURATION_SECOND_MIN));

                waitingQueue.addFirst(newCustomer); // add the customer to the queue

                if (ENABLE_DEBUG_LOGGING) {
                    System.out.println("[Arrive Event] " + now + ":"
                            + " customer " + newCustomer.id + " arrived");
                }
            }

            // ========== let the customers (who are done) leave first and free up the checkout ==========
            for (int i = 0; i < checkoutCompletionTime.size(); ++i) {
                final Integer completionTime = checkoutCompletionTime.get(i);
                if (completionTime == null) {
                    continue; // station is free
                }

                if (completionTime != now) { // this customer at the station not leave now
                    continue;
                }

                final Customer leavingCustomer = stations.get(i);

                // collect event specific stats
                final int waitTimeInStore = now - leavingCustomer.timeOfArrival;
                weightedTimeInStore += waitTimeInStore;
                ++customerMovingOutCount;

                // set the values noting that the station is free
                stations.set(i, null);
                checkoutCompletionTime.set(i, null);

                if (ENABLE_DEBUG_LOGGING) {
                    System.out.println("[Leave Event] " + now + ":"
                            + " customer " + leavingCustomer.id
                            + " is leaving checkout " + i
                            + ", total store time " + waitTimeInStore);
                }
            }

            // ========== let a customer move from the waiting queue to the checkout station ==========
            for (int i = 0; i < checkoutCompletionTime.size() && !waitingQueue.isEmpty(); ++i) {
                final Integer completionTime = checkoutCompletionTime.get(i);
                if (completionTime != null) {
                    continue; // station in occupied
                }

                final Customer movingCustomer = waitingQueue.removeLast(); // get the customer waiting the longest

                // make the station busy with checkout completion time
                stations.set(i, movingCustomer);
                checkoutCompletionTime.set(i, movingCustomer.computeTimeSpentAtCheckInSecond() + now);

                ++customerMovedToCheckoutCont;
                final int waitTimeInQueue = now - movingCustomer.timeOfArrival;
                weightedWaitingTime += waitTimeInQueue;

                if (ENABLE_DEBUG_LOGGING) {
                    System.out.println("[Move Event] " + now + ":"
                            + " customer " + movingCustomer.id
                            + " is moving to checkout " + i
                            + ", waiting time " + waitTimeInQueue);
                }
            }

            // ========== collect reporting data ==========
            final int currentWaitingQueueSize = waitingQueue.size();
            weightedCustomersInQueue += currentWaitingQueueSize;
            maxCustomerWaitingCount = Math.max(maxCustomerWaitingCount, currentWaitingQueueSize);

            // compute busy checkout count
            int busyCheckout = 0;
            for (int i = 0; i < checkoutCompletionTime.size(); ++i) {
                if (checkoutCompletionTime.get(i) != null) {
                    ++busyCheckout;
                }
            }
            weightedCustomersInStore += busyCheckout + currentWaitingQueueSize;
            weightedCheckoutBusyTime += busyCheckout;
            maxCustomerInStoreCount = Math.max(maxCustomerInStoreCount, busyCheckout + currentWaitingQueueSize);

            // log summery if verbose logging or state change logging is enabled
            if (ENABLE_DEBUG_LOGGING) {
                System.out.println("[Summery] " + now + ":"
                        + " queue size " + printWaitingQueues(List.of(waitingQueue))
                        + ", checkout " + printStations(checkoutCompletionTime, now));
            }
        }

        // ----------------------------- model reporting -----------------------------
        if (ENABLE_DEBUG_LOGGING) {
            System.out.println();
        }

        System.out.printf("Customer arrived: %.2f/hr\n", (3600.0 * customerCount / runTime));
        System.out.printf("Customer left: %.2f/hr\n", (3600.0 * customerMovingOutCount / runTime));
        System.out.printf("Customer moved to checkout: %.2f/hr\n", (3600.0 * customerMovedToCheckoutCont / runTime));

        System.out.printf("Avg customers in the queue: %.2f\n", (1.0 * weightedCustomersInQueue / runTime));
        System.out.printf("Avg customers in the store: %.2f\n", (1.0 * weightedCustomersInStore / runTime));
        System.out.printf("Wait time in queue: %.2f sec\n", (1.0 * weightedWaitingTime / customerMovedToCheckoutCont));
        System.out.printf("Wait time in store: %.2f sec\n", (1.0 * weightedTimeInStore / customerMovingOutCount));

        System.out.printf("Checkout was busy: %.2f%% of the time\n", (100.0 * weightedCheckoutBusyTime / runTime) / numCheckoutStations);

        System.out.printf("Max number of customers in the queue: %d\n", maxCustomerWaitingCount);
        System.out.printf("Max number of customers in the store: %d\n", maxCustomerInStoreCount);
        System.out.println();
    }

    /**
     * Model 2
     * Model N customer waiting queue, N checkout; where a customer chooses the smallest queue
     * Assuming:
     * 1. if one+ queue are of smallest size, a customer always choose the queue with the lower index
     *
     * @param runTime                          runtime of the simulation in sec
     * @param customerInterArrivalTimeInSec    inter arrival time of customers (avg)
     * @param numCheckoutStations              number of checkout kiosks/queues
     * @param minNumItems                      minimum number of items to check out
     * @param maxNumItems                      maximum number of items to check out
     */
    private static void modelNQueuePickSmallestNCheckout(final int runTime, final int customerInterArrivalTimeInSec,
                                                         final int numCheckoutStations,
                                                         final int minNumItems, final int maxNumItems) {
        // ----------------------------- model variables -----------------------------
        // chance of a customer arrival in a given second
        final double customerArrivalChance = 1.0 / customerInterArrivalTimeInSec;
        // customer waits in these queues
        final ArrayList<Deque<Customer>> waitingQueues = new ArrayList<>(numCheckoutStations);
        // represents checkout stations (the customer there)
        final ArrayList<Customer> stations = new ArrayList<>(numCheckoutStations);
        // customer checkout start/completion time; a null value means free
        final ArrayList<Integer> checkoutCompletionTime = new ArrayList<>(numCheckoutStations);
        for (int i = 0; i < numCheckoutStations; ++i) {
            waitingQueues.add(new LinkedList<>());
            stations.add(null);             // no customer at checkout
            checkoutCompletionTime.add(null);
        }

        // ----------------------------- stats variables -----------------------------
        long customerCount = 0;                  // count number customers arrived to the store
        long customerMovedToCheckoutCont = 0;    // count number of customers moved to checkout after waiting in the queue
        long customerMovingOutCount = 0;         // count number of customers leaving the store
        long maxCustomerWaitingCount = 0;        // tracks max number of customers waiting in the waiting queue
        long maxCustomerInStoreCount = 0;        // tracks max number of customers in the store
        long weightedCustomersInQueue = 0;       // sum up number of customers waiting in the queue each sec
        long weightedCustomersInStore = 0;       // sum up number of customers waiting in the store each sec
        long weightedCheckoutBusyTime = 0;       // sum up number of busy stations each sec
        long weightedWaitingTime = 0;            // sum up the time spent in the waiting queue by all customers
        long weightedTimeInStore = 0;            // sum up the time spent in the store by all customers

        // ----------------------------- run model -----------------------------
        for (int now = 0; now < runTime; ++now) {
            // ========== let a customer arrive ==========
            if (RAND.nextDouble() < customerArrivalChance) {
                ++customerCount;

                // create the customer
                final Customer newCustomer = new Customer(customerCount, now,
                        minNumItems + RAND.nextInt(maxNumItems - minNumItems),
                        CHECKOUT_DURATION_PER_ITEM_SECOND_MIN + RAND.nextInt(CHECKOUT_DURATION_PER_ITEM_SECOND_MAX - CHECKOUT_DURATION_PER_ITEM_SECOND_MIN),
                        PAY_DURATION_SECOND_MIN + RAND.nextInt(PAY_DURATION_SECOND_MAX - PAY_DURATION_SECOND_MIN));

                // find the smallest queue
                int smallestQueue = 0;
                for (int j = 1; j < numCheckoutStations; ++j) {
                    if (waitingQueues.get(smallestQueue).size() > waitingQueues.get(j).size()) {
                        smallestQueue = j;
                    }
                }

                waitingQueues.get(smallestQueue).addFirst(newCustomer); // add the customer to the queue

                if (ENABLE_DEBUG_LOGGING) {
                    System.out.println("[Arrive Event] " + now + ":"
                            + " customer " + newCustomer.id + " arrived at queue " + smallestQueue);
                }
            }

            // ========== let the customers (who are done) leave first and free up the checkout ==========
            for (int i = 0; i < checkoutCompletionTime.size(); ++i) {
                final Integer completionTime = checkoutCompletionTime.get(i);
                if (completionTime == null) {
                    continue; // station is free
                }

                if (completionTime != now) { // this customer at the station not leave now
                    continue;
                }

                final Customer leavingCustomer = stations.get(i);

                // collect event specific stats
                final int waitTimeInStore = now - leavingCustomer.timeOfArrival;
                weightedTimeInStore += waitTimeInStore;
                ++customerMovingOutCount;

                // set the values noting that the station is free
                stations.set(i, null);
                checkoutCompletionTime.set(i, null);

                if (ENABLE_DEBUG_LOGGING) {
                    System.out.println("[Leave Event] " + now + ":"
                            + " customer " + leavingCustomer.id
                            + " is leaving checkout " + i
                            + ", total store time " + waitTimeInStore);
                }
            }

            // ========== let a customer move from the waiting queue to the checkout station ==========
            for (int i = 0; i < checkoutCompletionTime.size(); ++i) {
                final Integer completionTime = checkoutCompletionTime.get(i);
                if (completionTime != null) {
                    continue; // station in occupied
                }
                if (waitingQueues.get(i).isEmpty()) {
                    continue; // queue is empty none can be moved to checkout
                }

                final Customer movingCustomer = waitingQueues.get(i).removeLast(); // get the customer waiting the longest

                // make the station busy with checkout completion time
                stations.set(i, movingCustomer);
                checkoutCompletionTime.set(i, movingCustomer.computeTimeSpentAtCheckInSecond() + now);

                ++customerMovedToCheckoutCont;
                final int waitTimeInQueue = now - movingCustomer.timeOfArrival;
                weightedWaitingTime += waitTimeInQueue;

                if (ENABLE_DEBUG_LOGGING) {
                    System.out.println("[Move Event] " + now + ":"
                            + " customer " + movingCustomer.id
                            + " is moving to checkout " + i
                            + ", waiting time " + waitTimeInQueue);
                }
            }

            // ========== collect reporting data ==========
            // compute waiting queue size
            int currentWaitingQueueSize = 0;
            for (final Deque<Customer> waitingQueue : waitingQueues) {
                currentWaitingQueueSize += waitingQueue.size();
            }
            weightedCustomersInQueue += currentWaitingQueueSize;
            maxCustomerWaitingCount = Math.max(maxCustomerWaitingCount, currentWaitingQueueSize);

            // compute busy checkout count
            int busyCheckout = 0;
            for (int i = 0; i < checkoutCompletionTime.size(); ++i) {
                if (checkoutCompletionTime.get(i) != null) {
                    ++busyCheckout;
                }
            }
            weightedCustomersInStore += busyCheckout + currentWaitingQueueSize;
            weightedCheckoutBusyTime += busyCheckout;
            maxCustomerInStoreCount = Math.max(maxCustomerInStoreCount, busyCheckout + currentWaitingQueueSize);

            // log summery if verbose logging or state change logging is enabled
            if (ENABLE_DEBUG_LOGGING) {
                System.out.println("[Summery] " + now + ":"
                        + " queue size " + printWaitingQueues(waitingQueues)
                        + ", checkout " + printStations(checkoutCompletionTime, now));
            }
        }

        // ----------------------------- model reporting -----------------------------
        if (ENABLE_DEBUG_LOGGING) {
            System.out.println();
        }

        System.out.printf("Customer arrived: %.2f/hr\n", (3600.0 * customerCount / runTime));
        System.out.printf("Customer left: %.2f/hr\n", (3600.0 * customerMovingOutCount / runTime));
        System.out.printf("Customer moved to checkout: %.2f/hr\n", (3600.0 * customerMovedToCheckoutCont / runTime));

        System.out.printf("Avg customers in the queue: %.2f\n", (1.0 * weightedCustomersInQueue / runTime));
        System.out.printf("Avg customers in the store: %.2f\n", (1.0 * weightedCustomersInStore / runTime));
        System.out.printf("Wait time in queue: %.2f sec\n", (1.0 * weightedWaitingTime / customerMovedToCheckoutCont));
        System.out.printf("Wait time in store: %.2f sec\n", (1.0 * weightedTimeInStore / customerMovingOutCount));

        System.out.printf("Checkout was busy: %.2f%% of the time\n", (100.0 * weightedCheckoutBusyTime / runTime) / numCheckoutStations);

        System.out.printf("Max number of customers in the queue: %d\n", maxCustomerWaitingCount);
        System.out.printf("Max number of customers in the store: %d\n", maxCustomerInStoreCount);
        System.out.println();
    }

    /**
     * Model 3
     * Model N customer waiting queue, N checkout; where a customer chooses a random queue
     *
     * @param runTime                          runtime of the simulation in sec
     * @param customerInterArrivalTimeInSec    inter arrival time of customers (avg)
     * @param numCheckoutStations              number of checkout kiosks/queues
     * @param minNumItems                      minimum number of items to check out
     * @param maxNumItems                      maximum number of items to check out
     */
    private static void modelNQueuePickRandomtNCheckout(final int runTime,
                                                        final int customerInterArrivalTimeInSec,
                                                        final int numCheckoutStations,
                                                        final int minNumItems, final int maxNumItems) {
        // ----------------------------- model variables -----------------------------
        // chance of a customer arrival in a given second
        final double customerArrivalChance = 1.0 / customerInterArrivalTimeInSec;
        // customer waits in these queues
        final ArrayList<Deque<Customer>> waitingQueues = new ArrayList<>();
        // represents checkout stations (the customer there)
        final ArrayList<Customer> stations = new ArrayList<>(numCheckoutStations);
        // customer checkout start/completion time; a null value means free
        final ArrayList<Integer> checkoutCompletionTime = new ArrayList<>(numCheckoutStations);
        for (int i = 0; i < numCheckoutStations; ++i) {
            waitingQueues.add(new LinkedList<>());
            stations.add(null);             // no customer at checkout
            checkoutCompletionTime.add(null);
        }

        // ----------------------------- stats variables -----------------------------
        long customerCount = 0;                  // count number customers arrived to the store
        long customerMovedToCheckoutCont = 0;    // count number of customers moved to checkout after waiting in the queue
        long customerMovingOutCount = 0;         // count number of customers leaving the store
        long maxCustomerWaitingCount = 0;        // tracks max number of customers waiting in the waiting queue
        long maxCustomerInStoreCount = 0;        // tracks max number of customers in the store
        long weightedCustomersInQueue = 0;       // sum up number of customers waiting in the queue each sec
        long weightedCustomersInStore = 0;       // sum up number of customers waiting in the store each sec
        long weightedCheckoutBusyTime = 0;       // sum up number of busy stations each sec
        long weightedWaitingTime = 0;            // sum up the time spent in the waiting queue by all customers
        long weightedTimeInStore = 0;            // sum up the time spent in the store by all customers

        // ----------------------------- run model -----------------------------
        for (int now = 0; now < runTime; ++now) {
            // ========== let a customer arrive ==========
            if (RAND.nextDouble() < customerArrivalChance) {
                ++customerCount;

                // create the customer
                final Customer newCustomer = new Customer(customerCount, now,
                        minNumItems + RAND.nextInt(maxNumItems - minNumItems),
                        CHECKOUT_DURATION_PER_ITEM_SECOND_MIN + RAND.nextInt(CHECKOUT_DURATION_PER_ITEM_SECOND_MAX - CHECKOUT_DURATION_PER_ITEM_SECOND_MIN),
                        PAY_DURATION_SECOND_MIN + RAND.nextInt(PAY_DURATION_SECOND_MAX - PAY_DURATION_SECOND_MIN));

                // find a random queue
                final int randomQueue = RAND.nextInt(numCheckoutStations);

                waitingQueues.get(randomQueue).addFirst(newCustomer); // add the customer to the queue

                if (ENABLE_DEBUG_LOGGING) {
                    System.out.println("[Arrive Event] " + now + ":"
                            + " customer " + newCustomer.id + " arrived at queue " + randomQueue);
                }
            }

            // ========== let the customers (who are done) leave first and free up the checkout ==========
            for (int i = 0; i < checkoutCompletionTime.size(); ++i) {
                final Integer completionTime = checkoutCompletionTime.get(i);
                if (completionTime == null) {
                    continue; // station is free
                }

                if (completionTime != now) { // this customer at the station not leave now
                    continue;
                }

                final Customer leavingCustomer = stations.get(i);

                // collect event specific stats
                final int waitTimeInStore = now - leavingCustomer.timeOfArrival;
                weightedTimeInStore += waitTimeInStore;
                ++customerMovingOutCount;

                // set the values noting that the station is free
                stations.set(i, null);
                checkoutCompletionTime.set(i, null);

                if (ENABLE_DEBUG_LOGGING) {
                    System.out.println("[Leave Event] " + now + ":"
                            + " customer " + leavingCustomer.id
                            + " is leaving checkout " + i
                            + ", total store time " + waitTimeInStore);
                }
            }

            // ========== let a customer move from the waiting queue to the checkout station ==========
            for (int i = 0; i < checkoutCompletionTime.size(); ++i) {
                final Integer completionTime = checkoutCompletionTime.get(i);
                if (completionTime != null) {
                    continue; // station in occupied
                }
                if (waitingQueues.get(i).isEmpty()) {
                    continue; // queue is empty none can be moved to checkout
                }

                final Customer movingCustomer = waitingQueues.get(i).removeLast(); // get the customer waiting the longest

                // make the station busy with checkout completion time
                stations.set(i, movingCustomer);
                checkoutCompletionTime.set(i, movingCustomer.computeTimeSpentAtCheckInSecond() + now);

                ++customerMovedToCheckoutCont;
                final int waitTimeInQueue = now - movingCustomer.timeOfArrival;
                weightedWaitingTime += waitTimeInQueue;

                if (ENABLE_DEBUG_LOGGING) {
                    System.out.println("[Move Event] " + now + ":"
                            + " customer " + movingCustomer.id
                            + " is moving to checkout " + i
                            + ", waiting time " + waitTimeInQueue);
                }
            }

            // ========== collect reporting data ==========
            // compute waiting queue size
            int currentWaitingQueueSize = 0;
            for (final Deque<Customer> waitingQueue : waitingQueues) {
                currentWaitingQueueSize += waitingQueue.size();
            }
            weightedCustomersInQueue += currentWaitingQueueSize;
            maxCustomerWaitingCount = Math.max(maxCustomerWaitingCount, currentWaitingQueueSize);

            // compute busy checkout count
            int busyCheckout = 0;
            for (int i = 0; i < checkoutCompletionTime.size(); ++i) {
                if (checkoutCompletionTime.get(i) != null) {
                    ++busyCheckout;
                }
            }
            weightedCustomersInStore += busyCheckout + currentWaitingQueueSize;
            weightedCheckoutBusyTime += busyCheckout;
            maxCustomerInStoreCount = Math.max(maxCustomerInStoreCount, busyCheckout + currentWaitingQueueSize);

            // log summery if verbose logging or state change logging is enabled
            if (ENABLE_DEBUG_LOGGING) {
                System.out.println("[Summery] " + now + ":"
                        + " queue size " + printWaitingQueues(waitingQueues)
                        + ", checkout " + printStations(checkoutCompletionTime, now));
            }
        }

        // ----------------------------- model reporting -----------------------------
        if (ENABLE_DEBUG_LOGGING) {
            System.out.println();
        }

        System.out.printf("Customer arrived: %.2f/hr\n", (3600.0 * customerCount / runTime));
        System.out.printf("Customer left: %.2f/hr\n", (3600.0 * customerMovingOutCount / runTime));
        System.out.printf("Customer moved to checkout: %.2f/hr\n", (3600.0 * customerMovedToCheckoutCont / runTime));

        System.out.printf("Avg customers in the queue: %.2f\n", (1.0 * weightedCustomersInQueue / runTime));
        System.out.printf("Avg customers in the store: %.2f\n", (1.0 * weightedCustomersInStore / runTime));
        System.out.printf("Wait time in queue: %.2f sec\n", (1.0 * weightedWaitingTime / customerMovedToCheckoutCont));
        System.out.printf("Wait time in store: %.2f sec\n", (1.0 * weightedTimeInStore / customerMovingOutCount));

        System.out.printf("Checkout was busy: %.2f%% of the time\n", (100.0 * weightedCheckoutBusyTime / runTime) / numCheckoutStations);

        System.out.printf("Max number of customers in the queue: %d\n", maxCustomerWaitingCount);
        System.out.printf("Max number of customers in the store: %d\n", maxCustomerInStoreCount);
        System.out.println();
    }

    public static void main(final String[] args) {
        System.out.printf("Model 1: 1 queue, %d checkouts\n", CHECKOUT_STATION_COUNT);
        model1QueueNCheckout(MODEL_RUN_TIME,
                CUSTOMER_ARRIVAL_TIME_IN_SEC,
                CHECKOUT_STATION_COUNT,
                MIN_ITEMS_TO_CHECKOUT, MAX_ITEMS_TO_CHECKOUT);

        System.out.printf("Model 2: N queue (customer picks the smallest), %d checkouts\n", CHECKOUT_STATION_COUNT);
        modelNQueuePickSmallestNCheckout(MODEL_RUN_TIME,
                CUSTOMER_ARRIVAL_TIME_IN_SEC,
                CHECKOUT_STATION_COUNT,
                MIN_ITEMS_TO_CHECKOUT, MAX_ITEMS_TO_CHECKOUT);

        System.out.printf("Model 3: N queue (customer picks random), %d checkouts\n", CHECKOUT_STATION_COUNT);
        modelNQueuePickRandomtNCheckout(MODEL_RUN_TIME,
                CUSTOMER_ARRIVAL_TIME_IN_SEC,
                CHECKOUT_STATION_COUNT,
                MIN_ITEMS_TO_CHECKOUT, MAX_ITEMS_TO_CHECKOUT);
    }
}


