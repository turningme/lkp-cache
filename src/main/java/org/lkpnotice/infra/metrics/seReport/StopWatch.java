package org.lkpnotice.infra.metrics.seReport;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class StopWatch implements IStopWatch {

    private static Logger log = LoggerFactory.getLogger(StopWatch.class);

    private long beginDate = 0;
    private String name = null;
    private long elapsedTime = 0;
    private TreeMap<ReportItems, Long> eventNameCount = new TreeMap<ReportItems, Long>();
    private boolean stoped = true;
    private boolean used = false;
    private boolean paused = false;

    public StopWatch(String name) {
        beginDate = System.nanoTime();
        this.name = name;
    }

    @Override
    public void addEventCount(ReportItems eventCount, int count) {
        Long previousCount = eventNameCount.get(eventCount);
        if (previousCount == null) {
            eventNameCount.put(eventCount, new Long(count));
        } else {
            eventNameCount.put(eventCount, previousCount + count);
        }
    }


    @Override
    public void start() {
        stoped = false;
        used = true;
        paused = false;
        beginDate = System.nanoTime();
    }


    @Override
    public boolean isUsed() {
        return used;
    }


    @Override
    public long getElapsedTime() {

        return elapsedTime;
    }


    @Override
    public long pause() {
        if (paused == false) {
            long now = System.nanoTime();
            elapsedTime += now - beginDate;
            //multiple pause must add 0 time
            beginDate = now;
            paused = true;
        }
        return elapsedTime;

    }


    @Override
    public long stop() {
        if (stoped == false) {
            stoped = true;
            pause();
        }
        return elapsedTime;
    }

    @Override
    public void log(int level, boolean last) {
        long elapsedTime = 0;
        if (stoped == true) {
            elapsedTime = this.elapsedTime;
        } else {
            //get an instant cliche
            elapsedTime = pause();
        }
        String indent = "";
        for (int i = 0 ; i < level ; i++) {
            if (i + 1 < level) {
                indent += "|     ";
            } else {
                indent += "+---";
            }
        }
        log.info(indent + "{} for \"{}\"", DateUtil.nanoToString(elapsedTime), name);
        for (Entry<ReportItems, Long> eventCount : eventNameCount.entrySet()) {
            ReportItems eName = eventCount.getKey();
            Long eCount = eventCount.getValue();
            float ecountPerSec = 0;
            if (elapsedTime != 0) {
                ecountPerSec = ((float) eCount / (float) elapsedTime) * 1000000000L;
                log.info(indent + ">>>{} {} ({}/s)", new Object[] { eCount, eName.getLabel(), ecountPerSec });
            }
        }

    }

    @Override
    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public boolean isPaused() {
        return paused;
    }

}