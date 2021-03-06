package nl.twente.bms.model.conf;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import nl.twente.bms.algo.struct.StationGraph;
import nl.twente.bms.algo.struct.TimeExpandedGraph;
import nl.twente.bms.model.elem.Driver;
import nl.twente.bms.model.elem.Offer;
import nl.twente.bms.utils.ExcelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * The class to record driver config
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class DriverConfig {
    private static final Logger logger = LoggerFactory.getLogger(DriverConfig.class);

    private int nextOfferId;
//    private IntObjectMap<Driver> driverMap;
    private ObjectArrayList<Driver> driverList;
    private IntObjectMap<Offer> offerMap;
    private TimeExpandedGraph timeExpandedGraph;

    private int[] driverIndices;


    public DriverConfig(int numDrivers, double detourInput, ExcelReader excelReader,
                        StationGraph stationGraph, boolean isRandom, int[] driverIndicesIn) {
        nextOfferId = 0;

        double detour = detourInput;
        double speed = Double.parseDouble(excelReader.xlsread("Input", 1, 4));
        double delay = Double.parseDouble(excelReader.xlsread("Input", 1, 17));
        int hold = Integer.parseInt(excelReader.xlsread("Input", 1, 18));

        logger.info("detour: {}", detour);
        logger.info("speed: {} km/h", speed);
        logger.info("hold: {} mins", hold);

        String[] idStrArray;
        String[] startStationArray;
        String[] endStationArray;
        String[] earliestDepartureArray;
        String[] latestArrivalArray;
        String[] capacityArray;

        if(!isRandom){
            idStrArray = excelReader.xlsread("Input", 3, 1, numDrivers);
            startStationArray = excelReader.xlsread("Input", 4, 1, numDrivers);
            endStationArray = excelReader.xlsread("Input", 5, 1, numDrivers);
            earliestDepartureArray = excelReader.xlsread("Input", 7, 1, numDrivers);
            latestArrivalArray = excelReader.xlsread("Input", 8, 1, numDrivers);
            capacityArray = excelReader.xlsread("Input", 10, 1, numDrivers);

            driverIndices = IntStream.rangeClosed(1, numDrivers).toArray();
        }
        else{
            Random rand = new Random(System.currentTimeMillis());
            idStrArray = new String[numDrivers];
            startStationArray = new String[numDrivers];
            endStationArray = new String[numDrivers];
            earliestDepartureArray = new String[numDrivers];
            latestArrivalArray = new String[numDrivers];
            capacityArray = new String[numDrivers];

            IntSet generated = new IntOpenHashSet();
            while (generated.size() < numDrivers)
            {
                Integer next = rand.nextInt(1000) + 1;
                generated.add(next);
            }

            if(driverIndicesIn != null){
                driverIndices = driverIndicesIn;
            }
            else{
                driverIndices = generated.toArray();
            }
            //driverIndices = generated.toArray();
//            driverIndices = new int[]{768,8,350,144,428,180,622,815,388,951,28,980,828,47,266};
//            driverIndices = new int[]{784,956,592,379,443,572,359,434,898,299,873,244,808,625,512};
//            driverIndices = new int[]{95,283,663,398,436,905,78,263,427,834,789,744,238,917,117};
//            driverIndices = new int[]{513,391,172,829,551,910,919,627,966,519,217,132,608,757,698};
//            driverIndices = new int[]{938,683,440,2,819,177,844,461,387,840,453,162,289,613,750};
//            driverIndices = new int[]{211,136,147,316,71,953,301,367,930,123,124,514,459,120,414};
//            driverIndices = new int[]{706,796,394,237,477,560,165,734,412,15,740,280,221,991,755};
//            driverIndices = new int[]{211,12,148,994,6,507,535,655,174,696,770,746,334,893,757};

            for(int i = 0; i < numDrivers; i++){
                idStrArray[i] = excelReader.xlsread("Input", 3, driverIndices[i]);
                startStationArray[i] = excelReader.xlsread("Input", 4, driverIndices[i]);
                endStationArray[i] = excelReader.xlsread("Input", 5, driverIndices[i]);
                earliestDepartureArray[i] = excelReader.xlsread("Input", 7, driverIndices[i]);
                latestArrivalArray[i] = excelReader.xlsread("Input", 8, driverIndices[i]);
                capacityArray[i] = excelReader.xlsread("Input", 10, driverIndices[i]);
            }
        }

        timeExpandedGraph = new TimeExpandedGraph(stationGraph, this);

        //index driver object in driver map
//        driverMap = new IntObjectOpenHashMap<>(numDrivers);
        driverList = new ObjectArrayList<>(numDrivers);
        offerMap = new IntObjectOpenHashMap<>(numDrivers);

        for (int i = 0; i < numDrivers; i++) {
            int source = Integer.parseInt(startStationArray[i]);
            int target = Integer.parseInt(endStationArray[i]);
            int shortestPathDistance = stationGraph.getShortestDistance(source, target);
            Driver driver = new Driver(Integer.parseInt(idStrArray[i]),
                    source, target,detour, delay,
                    Integer.parseInt(earliestDepartureArray[i]),
                    Integer.parseInt(latestArrivalArray[i]),
                    shortestPathDistance, hold, speed,
                    Integer.parseInt(capacityArray[i]));
//            driverMap.put(Integer.parseInt(idStrArray[i]), driver);
            driverList.add(driver);
        }
        buildTimeExpandedGraph(stationGraph);
    }

    public void shuffleAndRebuildTimeExpandedGraph(StationGraph stationGraph, IntSet assignedDriverIdSet){
        nextOfferId = 0;
        offerMap.clear();
        for (ObjectCursor<Driver> driverCursor: driverList) {
            driverCursor.value.reset();
            if(!assignedDriverIdSet.contains(driverCursor.value.getId())){
                driverCursor.value.shuffle();
            }
        }
//        timeExpandedGraph.clear();
        buildTimeExpandedGraph(stationGraph);
    }

    private void buildTimeExpandedGraph(StationGraph stationGraph){
        timeExpandedGraph = new TimeExpandedGraph(stationGraph, this);
        for (ObjectCursor<Driver> driverCursor: driverList) {
            int currentId = getNextOfferId();
            Offer offer = driverCursor.value.createInitOffer(currentId, stationGraph);
            offerMap.put(currentId, offer);
            timeExpandedGraph.addOffer(offer);
            logger.info(offer.toString());
        }
//        timeExpandedGraph.display();
    }

    public int getNextOfferId() {
        return nextOfferId++;
    }

    public TimeExpandedGraph getTimeExpandedGraph() {
        return timeExpandedGraph;
    }

    public Offer getOfferById(int offerId) {
        return offerMap.get(offerId);
    }
    public int getDriverIdByOfferId(int offerId) {
        return offerMap.get(offerId).getDriverId();
    }

    public void addOffer(Offer offer) {
        offerMap.put(offer.getId(), offer);
    }

//    public IntObjectMap<Driver> getDriverMap() {
//        return driverMap;
//    }
    public ObjectArrayList<Driver> getDriverList(){
        return driverList;
    }

    public int[] getDriverIndices() {
        return driverIndices;
    }
}
