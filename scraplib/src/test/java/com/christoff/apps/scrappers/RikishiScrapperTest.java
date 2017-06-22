package com.christoff.apps.scrappers;

import com.christoff.apps.sumolambda.rikishisread.domain.Rikishi;
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
public class RikishiScrapperTest {

    private RikishiScrapper tested;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void setup(){
        tested = new RikishiScrapper();
        tested.setBaseUrl("http://localhost:8080/");
    }

    @Test
    public void should_parse_captured_page() throws IOException {
        tested.setListUrl("list");
        // Mock
        URL url = Resources.getResource("rikishilist.html");
        String body = Resources.toString(url, Charsets.UTF_8);
        stubFor(get(urlEqualTo("/list"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        // DO
        List<IdAndUrl> result = tested.select();

        assertEquals("Should have two results",3,result.size());
        assertEquals(new IdAndUrl(8948,"http://localhost:8080/Rikishi.aspx?r=8948"),result.get(0));

    }

    @Test
    public void should_scrap_hakuho() throws IOException, ParseException {
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
        should_scrap_expected_rikishi("hakuho.html",hakuho);
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
        should_scrap_expected_rikishi("tochiozan.html",tochiozan);
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
        should_scrap_expected_rikishi("harumafuji.html",harumafuji);
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
        Rikishi result  = (Rikishi) tested.getDetail(new IdAndUrl(42,"http://localhost:8080/Rikishi.aspx?r=42"));

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
        should_scrap_expected_rikishi("terunofuji.html",terunofuji);
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
        should_scrap_expected_rikishi("terunofuji_not_born.html",terunofuji);
    }

    private void should_scrap_expected_rikishi(String resourceName, Rikishi expected) throws IOException, ParseException {
        // Mock
        URL url = Resources.getResource(resourceName);
        String body = Resources.toString(url, Charsets.UTF_8);
        stubFor(get(urlEqualTo("/Rikishi.aspx?r=" + expected.getId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        // DO
        Rikishi result  = (Rikishi) tested.getDetail(new IdAndUrl(expected.getId(),"http://localhost:8080/Rikishi.aspx?r=" + expected.getId()));

        assertNotNull(result);
        assertEquals(expected.getId(),result.getId());
        assertEquals(expected.getRealName(),result.getRealName());
        if (expected.getBirthDate()==null){
            assertNull(result.getBirthDate());
        } else assertEquals(expected.getBirthDate().toString(), result.getBirthDate().toString());
        assertEquals("Heya",expected.getHeya(),result.getHeya());
        assertEquals("Name",expected.getName(),result.getName());
        assertEquals(expected.getShusshin(),result.getShusshin());
        assertEquals(expected.getHeight(),result.getHeight());
        assertEquals(expected.getWeight(),result.getWeight(),0);
        assertEquals(expected.getRank(),result.getRank());
    }

    @Test
    public void should_be_able_to_download_image() throws IOException, ParseException {
        // Mock
        URL url = Resources.getResource("42.jpg");
        String body = Resources.toString(url, Charsets.UTF_8);
        stubFor(get(urlEqualTo("/pic/42.jpg"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        // DO
        tested.setImageUrl("pic/");
        byte[] image = tested.getIllustration(new IdAndUrl(42,"http://localhost:8080/Rikishi.aspx?r=42"));

        assertNotNull(image);
        assertTrue(image.length > 0);

    }

    @Test
    public void should_return_empty_on_missing_image() throws IOException, ParseException {
        // DO
        tested.setImageUrl("pic/");
        byte[] image = tested.getIllustration(new IdAndUrl(666,"http://localhost:8080/Rikishi.aspx?r=666"));
        // test
        assertNotNull(image);
        assertEquals("Image size", 0, image.length);
    }


}
