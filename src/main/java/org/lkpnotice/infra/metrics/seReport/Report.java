package org.lkpnotice.infra.metrics.seReport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Report implements IReport {
    private static final Logger log = LoggerFactory.getLogger(Report.class);

    private static final Map<String, IReport> reports = new HashMap<String, IReport>();

    public static boolean REPORT_ENABLED = true;

    public static final IReport mainLiteStatsSpent = Report.get("Main lite-stats");
    public static final IReport visitorAggregationSpent = Report.get("All intermediate aggregation java+mongo");
    public static final IReport mongoSpentTime = visitorAggregationSpent.getSubReport("Intermediate aggregation mongo spent time");

    public static final IReport writeIntermediateToFile = visitorAggregationSpent.getSubReport("write intermediate to file");

    public static final IReport cleanVisitorsJava = visitorAggregationSpent.getSubReport("clean unique visitors java");
    public static final IReport cleanVisitorsMongo = visitorAggregationSpent.getSubReport("clean unique visitors mongo");

    public static final IReport countVisitorsAll = visitorAggregationSpent.getSubReport("count unique visitors part");
    public static final IReport countVisitorsJava = visitorAggregationSpent.getSubReport("count unique visitors java part");
    public static final IReport countVisitorsMongo = visitorAggregationSpent.getSubReport("count unique visitors mongo part");
    public static final IReport dailyCollections = visitorAggregationSpent.getSubReport("daily's collections");
    public static final IReport monthlyCollections = visitorAggregationSpent.getSubReport("monthly's collections");
    public static final IReport campaignsCollections = visitorAggregationSpent.getSubReport("campaign's collections");

    public static final IReport reportCounting = Report.get("Time spent to count into Report class");

    private static Integer batchid = null;

    private IStopWatch stopWatch = null;
    private boolean started = false;

    private Map<String, IReport> subReports;


    public static IReport get(String reportName) {
        IReport report = reports.get(reportName);
        if (report == null) {
            if (REPORT_ENABLED) {
                report = new Report(reportName);
            } else {
                report = new EmptyReport();
            }
            reports.put(reportName, report);
        }

        return report;
    }

    public static void setReportEnable(boolean enable) {
        Report.REPORT_ENABLED = enable;
    }

    public Report(String title) {
        super();
        stopWatch = new StopWatch(title);
        subReports = new HashMap<String, IReport>();
    }


    @Override
    public IReport getSubReport(String reportName) {
        IReport report = subReports.get(reportName);
        if (report == null) {
            if (REPORT_ENABLED) {
                report = new Report(reportName);
            } else {
                report = new EmptyReport();
            }
            subReports.put(reportName, report);
        }
        return report;
    }


    public static void setBatchid(Integer batchid) {
        Report.batchid = batchid;
    }


    @Override
    public void addEventCount(ReportItems eReportCount, int count) {
        reportCounting.start();
        stopWatch.addEventCount(eReportCount, count);
        reportCounting.pause();
    }


    @Override
    public void start() {
        started = true;
        stopWatch.start();
    }

    @Override
    public void startIfPaused() {
        if ( stopWatch.isPaused() ) {
            start();
        }
    }

    @Override
    public long pause() {
        return stopWatch.pause();
    }


    @Override
    public long stop() {
        return stopWatch.stop();
    }


    @Override
    public long getElapsedTime() {
        return stopWatch.getElapsedTime();
    }


    @Override
    public void logInternal(int level, boolean last) {
        if (started || hasChildStarted(subReports)) {
            stopWatch.log(level, last);
        }
    }

    private boolean hasChildStarted(Map<String, IReport> children) {
        for (IReport r : children.values()) {
            if (r.isStarted() || hasChildStarted(r.getSubReports())) {
                return true;
            }
        }
        return false;
    }

    static final Comparator<IReport> TIME_COMPARE = new Comparator<IReport>() {
        @Override
        public int compare(IReport report1, IReport report2) {
            Long reportElapsedTime1 = report1.getStopWatch().getElapsedTime();
            Long reportElapsedTime2 = report2.getStopWatch().getElapsedTime();
            return reportElapsedTime2.compareTo(reportElapsedTime1);
        }
    };


    public static void log() {
        if (Report.REPORT_ENABLED) {
            Report.mainLiteStatsSpent.stop();
            long allTime = Report.mainLiteStatsSpent.getElapsedTime();

            SimpleDateFormat humanDateFormater = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            if (batchid != null) {
                long batchms = Long.parseLong("" + batchid + "000");
                Date batchDate = new Date();
                batchDate.setTime(batchms);
                humanDateFormater.setTimeZone(TimeZone.getDefault());
                String strBatchDate = humanDateFormater.format(batchDate.getTime());

                log.info("launch date | batchId {} | batch date {} | duration {}", new Object[] { batchid, strBatchDate, DateUtil.nanoToString(allTime) });

            } else {
                log.info("duration {} launch date ", DateUtil.nanoToString(allTime));
            }

            logMap(reports, 0);
        }
    }

    public static void logMap(Map<String, IReport> map, int level) {
        Collection<IReport> reportsCol = map.values();
        for (IReport r : reportsCol) {
            if (r.getSubReports().size() > 0 ) {
                long elapsed = r.getElapsedTime();
                for (IReport subReport : r.getSubReports().values()) {
                    elapsed -= subReport.getElapsedTime();
                }
                if (elapsed > 0) {
                    IReport unknowTimeReport = r.getSubReport("Time spent and not measured");
                    unknowTimeReport.getStopWatch().setElapsedTime(elapsed);
                    unknowTimeReport.setStarted(true);
                }
            }
        }

        List<IReport> reportsList = new ArrayList<IReport>(reportsCol);
        Collections.sort(reportsList, TIME_COMPARE);

        boolean last = false;
        int number = 0;
        for (IReport r : reportsList) {
            if (number == reportsList.size() - 1) {
                last = true;
            }
            if (r.getStopWatch().isUsed()) {
                r.stop();
            }
            r.logInternal(level, last);
            if (r.getSubReports().size() > 0 ) {
                logMap(r.getSubReports(), level + 1);
            }
            number++;
        }
    }


    @Override
    public Map<String, IReport> getSubReports() {
        return subReports;
    }


    @Override
    public String getName() {
        return stopWatch.getName();
    }


    @Override
    public boolean isStarted() {
        return started;
    }


    @Override
    public void setStarted(boolean started) {
        this.started = started;
    }

    @Override
    public IStopWatch getStopWatch() {
        return stopWatch;
    }

}
