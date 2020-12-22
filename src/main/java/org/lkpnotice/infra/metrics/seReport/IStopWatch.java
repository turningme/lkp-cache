package org.lkpnotice.infra.metrics.seReport;

public interface IStopWatch {

    /**
     * Sum the count for that event
     *
     * @param eventCount
     * @param count
     */
    public abstract void addEventCount(ReportItems eventCount, int count);

    /**
     * take a snap shot of the start time
     */
    public abstract void start();

    public abstract boolean isUsed();

    /**
     * Get last stop or pause elapsed time, it doesnt take a snap shot
     *
     * @return
     */
    public abstract long getElapsedTime();

    /**
     * pause the stop-watch and keep the previous elapsed time
     *
     * @return the elapsed time in millisecond
     */
    public abstract long pause();

    /**
     * return a time interval , and output the counter
     *
     * @return {@link TimeInterval}
     */
    public abstract long stop();

    /**
     * push all log information to log.info
     */
    public abstract void log(int level, boolean last);

    public abstract void setElapsedTime(long elapsedTime);

    public abstract String getName();

    public abstract boolean isPaused();

}