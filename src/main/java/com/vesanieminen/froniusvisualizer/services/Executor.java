package com.vesanieminen.froniusvisualizer.services;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.vesanieminen.froniusvisualizer.services.PakastinSpotService.getAndWriteToFile2YearData;
import static com.vesanieminen.froniusvisualizer.util.Utils.getSecondsToNextEvenHour;
import static com.vesanieminen.froniusvisualizer.util.Utils.getSecondsToNext_13_50;

@Slf4j
public class Executor {

    static {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
        executorService.schedule(Executor::updateAll, 0, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(Executor::updatePrices, getSecondsToNextEvenHour(), TimeUnit.HOURS.toSeconds(1), TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(Executor::updateFingridData, getSecondsToNextEvenHour() + 120, TimeUnit.HOURS.toSeconds(1), TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(Executor::updateNordpool_13_50, getSecondsToNext_13_50(), TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    private static void updateAll() {
        log.info("Started updateAll");
        updatePrices();
        updateFingridData();
        log.info("Ended updateAll");
    }

    public static void updateNordpool_13_50() {
        log.info("Started update Nordpool at 13:50");
        final var startTime = System.currentTimeMillis();
        NordpoolSpotService.updateNordpoolData(true);
        log.info("Ended update Nordpool at 13:50 in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    }

    public static void updatePrices() {
        log.info("Started updatePrices");
        final var startTime = System.currentTimeMillis();
        NordpoolSpotService.updateNordpoolData(false);
        updatePakastinData();
        SpotHintaService.updateData();
        log.info("Ended updatePrices in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    }

    private static void updateFingridData() {
        log.info("Started updateFingridData");
        final var startTime = System.currentTimeMillis();
        try {
            FingridService.updateRealtimeData();
            TimeUnit.MILLISECONDS.sleep(500);
            FingridService.updateProductionEstimateData();
            TimeUnit.MILLISECONDS.sleep(500);
            FingridService.updateConsumptionEstimateData();
            TimeUnit.MILLISECONDS.sleep(500);
            FingridService.updateWindEstimateData();
        } catch (InterruptedException e) {
            log.error("Could not update Fingrid data");
        }
        log.info("Ended updateFingridData in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    }

    private static void updatePakastinData() {
        //getAndWriteToFile();
        getAndWriteToFile2YearData();
    }


    public static void init() {
        // NOP
    }

}
