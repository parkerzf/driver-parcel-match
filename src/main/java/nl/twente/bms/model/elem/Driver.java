package nl.twente.bms.model.elem;


import nl.twente.bms.algo.struct.StationGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * The class to store the Drive object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class Driver {

    private int id;
    private int source;
    private int target;

    private double epsilon; // detour threshold
    private double gamma; // delay threshold

    private int departureTime;
    private int holdDuration; // longest hold parcel duration

    private double speed;

    private int capacity;

    private List<Offer> offers;

    public Driver(int id, int source, int target, double epsilon, double gamma,
                  int departureTime, int holdDuration, double speed, int capacity) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.epsilon = epsilon;
        this.gamma = gamma;
        this.departureTime = departureTime;
        this.holdDuration = holdDuration;
        this.speed = speed;
        this.capacity = capacity;

        this.offers = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public double getGamma() {
        return gamma;
    }

    public int getDepartureTime() {
        return departureTime;
    }

    public int getHoldDuration() {
        return holdDuration;
    }

    public double getSpeed() {
        return speed;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getDuration(int distance) {
        return (int) (distance * 60 / speed);
    }
    public int getDistance(int duration) {
        return (int) (speed * duration / 60);
    }

    public Offer createInitOffer(int offerId, StationGraph stationGraph) {
        Offer offer = new Offer(offerId, getSource(), getTarget(), getDepartureTime(),
                getCapacity(), this, stationGraph);
        return offer;
    }

    public void addOffer(Offer offer) {
        offers.add(offer);
    }

    public String toString() {
        return String.format("Driver[%d]: %d->%d, Detour: %.2f, Delay: %.2f, Depart: %d, Hold: %d, Speed: %.2f, Capacity: %d",
                id, source, target, epsilon, gamma, departureTime, holdDuration, speed, capacity);
    }


}
