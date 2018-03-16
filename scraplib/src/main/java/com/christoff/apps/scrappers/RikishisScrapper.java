package com.christoff.apps.scrappers;

import com.christoff.apps.sumo.lambda.domain.DomainObject;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import com.christoff.apps.utils.FilterRank;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by christophe on 30.04.17.
 */
public class RikishisScrapper implements Scrapper {

    /**
     * We can't afford to have a very long > 60sec http call
     * as the lambda itself will timeout !
     */
    private static final int TIMEOUT_MS = 30 * 1000;

    private static final String FAKE_HOST = "http://0.0.0.0/";
    /**
     * Let's define some request properties to pretend we are a browser
     */
    private static final String USER_AGENT = "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:59.0) Gecko/20100101 Firefox/59.0";
    private static final int LIST_RIKISHI_COLUMN = 0;
    private static final int DETAIL_RANK_COLUMN = 1;
    private static final int LIST_HIGHEST_RANK_COLUMN = 4;
    private static final int LIST_INTAI_COLUMN = 6;
    private static final String TABLE_LIST_BODY_SELECTOR = "body > div > div > table > tbody > tr > td.layoutright > table > tbody";
    private static final int DATA_NAME_COLUMN = 0;
    private static final String TABLE_RIKISHIDATA = "table.rikishidata:nth-child(1)";
    private static final int DATA_VALUE_COLUMN = 1;
    private static final String REAL_NAME = "Real Name";
    private static final String BIRTH_DATE = "Birth Date";
    private static final String HEYA = "Heya";
    private static final String SHIKONA = "Shikona";
    private static final String SHUSSHIN = "Shusshin";
    private static final String HEIGHT_AND_WEIGHT = "Height and Weight";
    private static final String BASHO_TABLE_SELECTOR = ".rikishi > tbody:nth-child(1)";
    private static final String TABLE_LINE_SELECTOR = "tr";
    private static final String TD = "td";
    private static final String TABLE_CELL_SELECTOR = TD;


    private static final Logger LOGGER = Logger.getLogger(RikishisScrapper.class);
    private static final String HTML_LINK = "a";
    private static final String HREF = "href";
    /**
     * JSoup when parsing &nbsp; outputs this unicode
     */
    private static final String NBSP_JSOUP = "\u00a0";
    /**
     * birthdate start with birthdate but contains age we neeed the date only
     */
    private final Pattern datePattern = Pattern.compile("([a-zA-Z]* \\d{1,2}, \\d{4}) (.*)");
    private final Pattern heightWeightPattern = Pattern.compile("(\\d+) cm (\\d+(\\.\\d{1,2})?) kg");
    private RikishisScrapParameters scrapParameters;

    /**
     * We cannot start the process without thos basic properties
     *
     * @param scrapParameters is where will search for stuff to scrap
     */
    public RikishisScrapper(RikishisScrapParameters scrapParameters) {
        this.scrapParameters = scrapParameters;
    }

    @Override
    public List<Integer> select() {
        LOGGER.info("Going to select " + scrapParameters.getFullListUrl());
        try {
            Document mainPage = Jsoup
                .connect(scrapParameters.getFullListUrl())
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get();
            Elements tableBody = mainPage.select(TABLE_LIST_BODY_SELECTOR);
            if (tableBody == null) {
                LOGGER.warn("Unable to find Rikishi table returning empty result");
                return new ArrayList<>(0);
            }
            Elements tableLines = tableBody.get(0).getElementsByTag(TABLE_LINE_SELECTOR);
            return tableLines
                .stream()
                .map( line -> line.getElementsByTag(TD))
                .filter(this::filterNotRetired)
                .filter(this::filterRank)
                .filter(this::filterLink)
                .map(this::extractLinkId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.warn("Error connecting to " + scrapParameters.getFullListUrl() + ". Returning empty list", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Returns true if Line has supported highest rank
     * @param cells TD of line of table
     * return true if rank is high enough
     */
    private boolean filterRank(Elements cells) {
        Element highestRankCell = cells.get(LIST_HIGHEST_RANK_COLUMN);
        return highestRankCell.hasText() && FilterRank.isRankToBeIncluded(highestRankCell.text());
    }

    /**
     * Returns true if Line has no intai (intai means retirement date)
     * @param cells TD of line of table
     * @return true if active
     */
    private boolean filterNotRetired(Elements cells) {
        Element intaiCell = cells.get(LIST_INTAI_COLUMN);
        String intaiText = intaiCell.text().replace(NBSP_JSOUP, "");
        return intaiText == null || intaiText.isEmpty();
    }

    /**
     * Return true if line has exactly one link in rikisshi column
     * @param cells TD of line of table
     * @return true if has one link to detail
     */
    private boolean filterLink(Elements cells) {
        Element rikishiCell = cells.get(LIST_RIKISHI_COLUMN);
        Elements rikishiLinks = rikishiCell.getElementsByTag(HTML_LINK);
        return rikishiLinks.size() == 1;
    }

    /**
     * Extract the rikisshi Id from the link
     * @param cells TD of line of table
     * @return id of link to detail
     */
    private Integer extractLinkId(Elements cells) {
        Element rikishiCell = cells.get(LIST_RIKISHI_COLUMN);
        Elements rikishiLinks = rikishiCell.getElementsByTag(HTML_LINK);
        return extractIdFromURL(rikishiLinks.attr(HREF));
    }

    /**
     * Helper method to extract id
     *
     * @param url the riskishi url with params
     * @return the id of the rikishi or Null
     */
    private @Nullable Integer extractIdFromURL(@NotNull String url) {
        URL aURL = null; // URL Need a protocol + a host
        try {
            aURL = new URL(FAKE_HOST + url);
            String query = aURL.getQuery();
            String[] queryArray = query.split("=");
            return Integer.parseInt(queryArray[1]);
        } catch (MalformedURLException e) {
            LOGGER.warn("A riskishi as a bad url " + url + " returning ");
            return null;
        }

    }

    @Override
    public DomainObject getDetail(Integer id) {
        String rikishiUrl = scrapParameters.getFullRikishiUrl() + id;
        try {
            LOGGER.info("Going to get Rikishi detail " + rikishiUrl);
            Document mainPage = Jsoup
                .connect(rikishiUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get();
            Elements rikishiData = mainPage.select(TABLE_RIKISHIDATA);
            if (rikishiData == null || rikishiData.size() != 1) {
                LOGGER.warn("No rikishi data found. Returning null");
                return null;
            }
            Elements tableLines = rikishiData.get(0).getElementsByTag(TABLE_LINE_SELECTOR);
            Rikishi result = new Rikishi();
            result.setId(id);
            for (Element line : tableLines) {
                Elements cells = line.getElementsByTag(TABLE_CELL_SELECTOR);
                if (cells.size() == 2) {
                    Element nameCell = cells.get(DATA_NAME_COLUMN);
                    Element valueCell = cells.get(DATA_VALUE_COLUMN);
                    switch (nameCell.text()) {
                        case REAL_NAME:
                            result.setRealName(valueCell.text());
                            break;
                        case BIRTH_DATE:
                            result.setBirthDate(handleBirthDate(valueCell.text()));
                            break;
                        case HEYA:
                            result.setHeya(getLastValue(valueCell.text()));
                            break;
                        case SHIKONA:
                            result.setSumoName(getLastValue(valueCell.text()));
                            break;
                        case SHUSSHIN:
                            result.setShusshin(valueCell.text());
                            break;
                        case HEIGHT_AND_WEIGHT:
                            handleHeightWeight(result, valueCell);
                            break;
                        default:
                            break;
                    }
                }
            }
            // The rank is more complex : it's the rank of the last basho !
            Element lastBasho = getLastLine(mainPage, BASHO_TABLE_SELECTOR);
            if (lastBasho != null) {
                Elements cells = lastBasho.getElementsByTag(TABLE_CELL_SELECTOR);
                if (cells != null && cells.size() == 6) {
                    result.setSumoRank(cells.get(DETAIL_RANK_COLUMN).text());
                }
            }
            // Rikishi is done . but we may exclude him
            if (FilterRank.isShortRankToBeIncluded(result.getSumoRank())) {
                return result;
            } else {
                LOGGER.warn("Excluding " + result.getId() + " because of rank");
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("Error connecting to " + rikishiUrl, e);
            return null;
        }
    }

    /**
     * Some string are string1 - string2 - string3
     * and we want the last one
     */
    private String getLastValue(String dashSeparated) {
        String[] values = dashSeparated.split(" - ");
        return values[values.length - 1];
    }

    /**
     * Return the last line of a table
     */
    private Element getLastLine(Document sourcePage, String cssSelector) {
        Elements table = sourcePage.select(cssSelector);
        if (table == null || table.size() != 1) {
            LOGGER.warn("No table data found for " + cssSelector + ". Returning null");
            return null;
        }
        Elements tableLines = table.get(0).getElementsByTag(TABLE_LINE_SELECTOR);
        return tableLines.last();
    }

    /**
     * Dedicated method to manage birthDate parsing
     *
     * @param valueCell the content of the html table cell
     */
    private String handleBirthDate(String valueCell) {
        if (valueCell != null && !valueCell.isEmpty()) {
            Matcher matcher = datePattern.matcher(valueCell);
            if (matcher.matches()) {
                String dateText = matcher.group(1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);
                LocalDate parsedDate = LocalDate.parse(dateText, formatter);
                return parsedDate.format(DateTimeFormatter.ISO_DATE);
            }
        }
        return null;
    }

    /**
     * Dedicated method to manage height and weight
     * TODO should refactor this. I hate method altering their params
     *
     * @param valueCell the HTML cell
     */
    private void handleHeightWeight(Rikishi result, Element valueCell) {
        Matcher matcher = heightWeightPattern.matcher(valueCell.text());
        if (matcher.matches()) {
            String height = matcher.group(1);
            String weight = matcher.group(2);
            result.setHeight(Integer.parseInt(height));
            result.setWeight(Double.parseDouble(weight));
        }
    }
}
