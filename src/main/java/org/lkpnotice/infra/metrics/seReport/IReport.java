package org.lkpnotice.infra.metrics.seReport;

import java.util.Map;

public interface IReport {

    public abstract IReport getSubReport(String reportName);

    /**
     * Register {@link ReportItems} if not done, and sum the count on it
     *
     * @param eReportCount
     * @param count
     */
    public abstract void addEventCount(ReportItems eReportCount, int count);

    /**
     * Start the internal stopWatch
     */
    public abstract void start();

    public abstract void startIfPaused();

    /**
     * pause the internal stopWatch
     *
     * @return
     */
    public abstract long pause();

    /**
     * get the elapsed time at last stop or pause
     *
     * @return
     */
    public abstract long getElapsedTime();

    public abstract Map<String, IReport> getSubReports();

    public abstract String getName();

    public abstract boolean isStarted();

    public abstract void setStarted(boolean started);

    public abstract IStopWatch getStopWatch();

    public abstract long stop();

    void logInternal(int level, boolean last);

}