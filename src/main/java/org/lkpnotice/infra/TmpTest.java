package org.lkpnotice.infra;

/**
 * Created by jpliu on 2020/5/26.
 */
public class TmpTest {
    public static final String SELECT_WITH_ID = "SELECT z.zoneid, ";
    public static final String QUERY_BODY =
            "z.affiliateid, " +
                    "a.agencyid, " +
                    "z.zonename, " +
                    "z.cost_type, " +
                    "z.cost, " +
                    "z.technology_cost_type, " +
                    "z.technology_cost, " +
                    "z.zone_roll_type_id, " +
                    "z.device_id, " +
                    "ag.country, " +
                    "z.width, " +
                    "z.height, " +
                    "i.label, " +
                    "z.supports_vast_redirect, " +
                    "z.supports_vpaid, " +
                    "z.supports_flash, " +
                    "z.supports_javascript, " +
                    "z.skippable, " +
                    "z.ssl_only, " +
                    "z.play_type, " +
                    "z.sound, " +
                    "d.label, " +
                    "zrt.roll_type_key, " +
                    "gc.country_id, " +
                    "a.openrtb_pricing_model, " +
                    "z.integration_type_id, " +
                    "ag.is_test_market_place " +
                    "FROM zones z " +
                    "LEFT JOIN integration_type i ON i.id = z.integration_type_id " +
                    "LEFT JOIN device d ON d.id = z.device_id " +
                    "LEFT JOIN affiliates a ON z.affiliateid = a.affiliateid " +
                    "LEFT JOIN agency ag ON a.agencyid = ag.agencyid " +
                    "LEFT JOIN zone_roll_types zrt ON zrt.zone_roll_type_id = z.zone_roll_type_id " +
                    "LEFT JOIN geo_countries gc ON ag.country = gc.country_iso ";

    static  String query = "SELECT z.zoneid,"
            + " z.zonename,"
            + " z.min_fill_rate,"
            + " z.chain,"
            + " z.capping as zone_capping,"
            + " z.capping_implementation,"
            + " z.block as zone_block,"
            + " z.delivery as zoneType,"
            + " z.inv_per_req,"
            + " z.append as zone_append,"
            + " z.prepend as zone_prepend,"
            + " z.affiliateid,"
            + " z.width as zone_width,"
            + " z.height as zone_height,"
            + " z.extensions_xml as zone_extensions_xml,"
            + " z.max_ad_duration AS zone_max_banner_duration,"
            + " s.max_ad_duration AS site_max_banner_duration,"
            + " p.max_ad_duration AS publisher_max_banner_duration,"
            + " z.zone_roll_type_id,"
            + " z.supports_vast_redirect,"
            + " z.supports_vpaid,"
            + " z.supports_flash,"
            + " z.supports_javascript,"
            + " z.device_id,"
            + " z.forced_profile_id,"
            + " z.enabled,"
            + " s.category_id as category,"
            + " s.publisher_id,"
            + " s.website,"
            + " s.openrtb_pricing_model,"
            + " (1 - IFNULL(p.adex_commision_percent, 0) / 100) AS publisher_ad_ex_share,"
            + " CASE " +
            "        WHEN z.zone_roll_type_id = 2 AND openrtb_pricing_model <> 'sharing' THEN openrtb_preroll" +
            "        WHEN z.zone_roll_type_id = 3 AND openrtb_pricing_model <> 'sharing' THEN openrtb_midroll" +
            "        WHEN z.zone_roll_type_id = 4 AND openrtb_pricing_model <> 'sharing' THEN openrtb_postroll" +
            "        WHEN z.zone_roll_type_id = 5 AND openrtb_pricing_model <> 'sharing' THEN openrtb_outstream" +
            "        WHEN z.zone_roll_type_id = 2 AND openrtb_pricing_model = 'sharing' THEN IFNULL(openrtb_preroll, 0) / 100" +
            "        WHEN z.zone_roll_type_id = 3 AND openrtb_pricing_model = 'sharing' THEN IFNULL(openrtb_midroll, 0) / 100" +
            "        WHEN z.zone_roll_type_id = 4 AND openrtb_pricing_model = 'sharing' THEN IFNULL(openrtb_postroll, 0) / 100" +
            "        WHEN z.zone_roll_type_id = 5 AND openrtb_pricing_model = 'sharing' THEN IFNULL(openrtb_outstream, 0) / 100" +
            "        ELSE NULL" +
            "    END AS openrtb_pricing,"
            + " IF(s.device_id in (4,5), s.show_application_bundle, s.show_domain) AS show_generic,"
            + " p.auction_type,"
            + " p.floor_price_sell_side AS floor_price_sell_side_publisher,"
            + " s.floor_price_sell_side AS floor_price_sell_side_site,"
            + " z.floor_price_sell_side AS floor_price_sell_side_zone,"
            + " p.floor_price_buy_side AS floor_price_buy_side_publisher,"
            + " s.floor_price_buy_side AS floor_price_buy_side_site,"
            + " z.floor_price_buy_side AS floor_price_buy_side_zone,"
            + " s.agencyid,"
            + " bg.id AS buyer_group_id,"
            + " p.passback_url AS publisher_passback_url,"
            + " s.passback_url AS site_passback_url,"
            + " z.passback_url AS zone_passback_url,"
            + " z.play_type AS zone_play_type,"
            + " z.sound AS zone_sound,"
            + " z.rtb_allowed_mime_types,"
            + " p.multiroll_blocking,"
            + " s.application_name,"
            + " s.application_bundle,"
            + " s.application_version,"
            + " s.application_paid,"
            + " s.application_store_url,"
            + " z.vtr25,"
            + " z.vtr50,"
            + " z.vtr75,"
            + " z.vtr100,"
            + " z.viewability,"
            + " z.integration_type_id,"
            + " z.overlay,"
            + " z.skip_offset_value,"
            + " z.skip_offset_type,"
            + " z.livestream,"
            + " z.ssl_only,"
            + " z.no_identification,"
            + " z.check_ad_approval"
            + " FROM "
            + "  zones z"
            + "  LEFT JOIN affiliates s ON s.affiliateid = z.affiliateid"
            + "  LEFT JOIN publisher p ON p.id = s.publisher_id"
            + "  LEFT JOIN bidder_group bg on bg.publisher_id = p.id"
            + " GROUP BY z.zoneid";
    public static void main(String[] args){
        System.out.println(SELECT_WITH_ID + QUERY_BODY);

        System.out.println(query);
    }
}
