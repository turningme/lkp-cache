package org.lkpnotice.infra.metrics.seReport;


public enum ReportItems {

    LOG_AD_SERVER_EVENT_COUNT("LogAdServerEvent count"),
    AD_ID_COUNT("AdId count"),

    RAW_LOG_OPERATION("RawLog operations"),
    RAW_LOG_INSERT("Number of insert into raw_log"),
    RAW_LOG_UPDATE("Number of update into raw_log"),

    STATS_LOG_OPERATION("Operations"),
    STATS_LOG_INSERT("Number of inserts into stats"),
    STATS_LOG_UPDATE("Number of update into stats"),

    AGGREG("Number of aggregation commands"),
    DISAGGREG("Number of disaggration commands"),
    MONGO_INSERT_COUNT("Trying insert to mongo (duplicates included) count");

    private  final String label;

    public String getLabel() {
        return label;
    }

    private ReportItems(String label) {
        this.label = label;
    }

}
