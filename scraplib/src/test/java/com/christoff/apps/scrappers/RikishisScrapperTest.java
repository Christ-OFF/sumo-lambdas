package com.christoff.apps.scrappers;

import com.christoff.apps.sumo.lambda.domain.Rikishi;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Created by christophe on 30.04.17.
 */
public class RikishisScrapperTest {

    /**
     * for the test I have ONE picture, could have more...
     */
    private static final String RIKISHI_PICTURE = "42.jpg";
    public static final String FAKE_PUBLISH_TOPIC = "FAKE_PUBLISH_TOPIC";

    private RikishisScrapper tested;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void setup(){
        RikishisScrapParameters params = new RikishisScrapParameters.Builder(FAKE_PUBLISH_TOPIC).withBaseUrl("http://localhost:8080/").build();
        tested = new RikishisScrapper(params);
    }

    @Test
    public void should_parse_captured_page() throws IOException {
        // Given
        URL url = Resources.getResource("rikishilist.html");
        String body = Resources.toString(url, Charsets.UTF_8);
        stubFor(get(urlEqualTo("/" + RikishisScrapParameters.DEFAULT_LIST_QUERY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        // When
        List<Integer> result = tested.select();
        // Then
        assertEquals("Should have two results",3,result.size());
        assertEquals("Should retrieve the first rikishi", 8948, result.get(0).intValue());
    }

    @Test
    public void should_scrap_hakuho() throws IOException, ParseException {
        // Given
        Rikishi hakuho = new Rikishi();
        hakuho.setId(42);
        hakuho.setRealName("MÖNKHBAT Davaajargal");
        LocalDate birthDate = LocalDate.of(1985,3,11);
        hakuho.setBirthDate(birthDate.format(DateTimeFormatter.ISO_DATE));
        hakuho.setHeya("Miyagino");
        hakuho.setName("Hakuho Sho");
        hakuho.setShusshin("Mongolia, Ulan-Bator");
        hakuho.setHeight(192);
        hakuho.setWeight(152.9);
        hakuho.setRank("Y2w");
        // When + Then
        should_scrap_expected_rikishi("hakuho.html", RIKISHI_PICTURE,hakuho);
    }

    @Test
    public void should_scrap_tochiozan() throws IOException, ParseException {
        Rikishi tochiozan = new Rikishi();
        tochiozan.setId(42);
        tochiozan.setRealName("KAGEYAMA Yuichiro");
        LocalDate birthDate = LocalDate.of(1987,3,9);
        tochiozan.setBirthDate(birthDate.format(DateTimeFormatter.ISO_DATE));
        tochiozan.setHeya("Kasugano");
        tochiozan.setName("Tochiozan Yuichiro");
        tochiozan.setShusshin("Kochi-ken, Susaki-shi - Kochi-ken, Aki-shi");
        tochiozan.setHeight(188);
        tochiozan.setWeight(150.6);
        tochiozan.setRank("M4e");
        should_scrap_expected_rikishi("tochiozan.html", RIKISHI_PICTURE, tochiozan);
    }

    @Test
    public void should_scrap_harumafuji() throws IOException, ParseException {
        Rikishi harumafuji = new Rikishi();
        harumafuji.setId(42);
        harumafuji.setRealName("DAVAANYAM Byambadorj");
        LocalDate birthDate = LocalDate.of(1984,4,14);
        harumafuji.setBirthDate(birthDate.format(DateTimeFormatter.ISO_DATE));
        harumafuji.setHeya("Isegahama");
        harumafuji.setName("Harumafuji Kohei");
        harumafuji.setShusshin("Mongolia, Ulan-Bator - Mongolia, Gobi-Altai");
        harumafuji.setHeight(186);
        harumafuji.setWeight(133);
        harumafuji.setRank("Y2e");
        should_scrap_expected_rikishi("harumafuji.html", RIKISHI_PICTURE, harumafuji);
    }

    @Test
    public void should_exclude_beginner_hakuho() throws IOException, ParseException {
        // Mock
        URL url = Resources.getResource("hakuho_beginner.html");
        String body = Resources.toString(url, Charsets.UTF_8);
        stubFor(get(urlEqualTo("/Rikishi.aspx?r=42"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        // DO
        Rikishi result = (Rikishi) tested.getDetail(42);

        assertNull(result);
    }

    @Test
    public void should_scrap_terunofuji() throws IOException, ParseException {
        Rikishi terunofuji = new Rikishi();
        terunofuji.setId(69);
        terunofuji.setRealName("GANERDENE Gantulga");
        LocalDate birthDate = LocalDate.of(1991,11,29);
        terunofuji.setBirthDate(birthDate.format(DateTimeFormatter.ISO_DATE));
        terunofuji.setHeya("Isegahama");
        terunofuji.setName("Terunofuji Haruo");
        terunofuji.setShusshin("Mongolia, Ulan-Bator");
        terunofuji.setHeight(192);
        terunofuji.setWeight(158.5);
        terunofuji.setRank("O1e");
        should_scrap_expected_rikishi("terunofuji.html", RIKISHI_PICTURE, terunofuji);
    }

    @Test
    public void should_null_birhtdate_on_invalid() throws IOException, ParseException {
        Rikishi terunofuji = new Rikishi();
        terunofuji.setId(69);
        terunofuji.setRealName("GANERDENE Gantulga");
        terunofuji.setBirthDate(null);
        terunofuji.setHeya("Isegahama");
        terunofuji.setName("Terunofuji Haruo");
        terunofuji.setShusshin("Mongolia, Ulan-Bator");
        terunofuji.setHeight(192);
        terunofuji.setWeight(158.5);
        terunofuji.setRank("O1e");
        should_scrap_expected_rikishi("terunofuji_not_born.html", RIKISHI_PICTURE, terunofuji);
    }

    private void should_scrap_expected_rikishi(String rikishiDefinitition, String rikishiPicture, Rikishi expected) throws IOException {
        // Given
        URL url = Resources.getResource(rikishiDefinitition);
        String body = Resources.toString(url, Charsets.UTF_8);
        stubFor(get(urlEqualTo("/" + RikishisScrapParameters.DEFAULT_RIKISHI_QUERY + expected.getId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        // When
        Rikishi result = (Rikishi) tested.getDetail(expected.getId());
        // Then
        assertNotNull(result);
        assertEquals(expected.getId(),result.getId());
        assertEquals(expected.getRealName(),result.getRealName());
        if (expected.getBirthDate()==null){
            assertNull(result.getBirthDate());
        } else {
            assertEquals(expected.getBirthDate(), result.getBirthDate());
        }
        assertEquals("Heya",expected.getHeya(),result.getHeya());
        assertEquals("Name",expected.getName(),result.getName());
        assertEquals(expected.getShusshin(),result.getShusshin());
        assertEquals(expected.getHeight(),result.getHeight());
        assertEquals(expected.getWeight(),result.getWeight(),0);
        assertEquals(expected.getRank(),result.getRank());
    }

}
