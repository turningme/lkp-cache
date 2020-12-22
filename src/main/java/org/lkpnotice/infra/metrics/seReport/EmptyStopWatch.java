package org.lkpnotice.infra.metrics.seReport;

public class EmptyStopWatch implements IStopWatch {

    @Override
    public void addEventCount(ReportItems eventCount, int count) {
    }

    @Override
    public void start() {
    }

    @Override
    public boolean isUsed() {
        return false;
    }

    @Override
    public long getElapsedTime() {
        return 0;
    }

    @Override
    public long pause() {
        return 0;
    }

    @Override
    public long stop() {
        return 0;
    }

    @Override
    public void log(int level, boolean last) {
    }

    @Override
    public void setElapsedTime(long elapsedTime) {
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean isPaused() {
        return false;
    }

}
