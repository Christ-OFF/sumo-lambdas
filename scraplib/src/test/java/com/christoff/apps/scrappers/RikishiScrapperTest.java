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
import java.util.Base64;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Created by christophe on 30.04.17.
 */
public class RikishiScrapperTest {

    /**
     * The mocked path under which the mock picture will be returned
     */
    public static final String PIC_URL = "pic/";
    /**
     * for the test I have ONE picture, could have more...
     */
    public static final String RIKISHI_PICTURE = "42.jpg";
    /**
     * The mocked but like real url under which a riskishi html is returned
     */
    public static final String RIKISHI_URL = "Rikishi.aspx?r=";
    public static final String LIST_URL = "list";

    private RikishiScrapper tested;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void setup(){
        tested = new RikishiScrapper();
        tested.setBaseUrl("http://localhost:8080/");
        tested.setImageUrl(PIC_URL);
    }

    @Test
    public void should_parse_captured_page() throws IOException {
        // Given
        tested.setListUrl(LIST_URL);
        URL url = Resources.getResource("rikishilist.html");
        String body = Resources.toString(url, Charsets.UTF_8);
        stubFor(get(urlEqualTo("/" + LIST_URL))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        // When
        List<IdAndUrl> result = tested.select();
        // Then
        assertEquals("Should have two results",3,result.size());
        assertEquals(new IdAndUrl(8948,"http://localhost:8080/Rikishi.aspx?r=8948"),result.get(0));
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
        Rikishi result  = (Rikishi) tested.getDetail(new IdAndUrl(42,"http://localhost:8080/Rikishi.aspx?r=42"), new byte[0]);

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
        stubFor(get(urlEqualTo("/" + RIKISHI_URL + expected.getId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(body)));
        URL urlPicture = Resources.getResource(rikishiPicture);
        byte[] bodyPicture = Resources.toByteArray(urlPicture);
        byte[] bodyPictureBase64 = Base64.getEncoder().encode(bodyPicture);
        stubFor(get(urlEqualTo( "/" + PIC_URL + expected.getId() + ".jpg"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(bodyPicture)));
        // When
        Rikishi result  = (Rikishi) tested.getDetail(new IdAndUrl(expected.getId(),"http://localhost:8080/Rikishi.aspx?r=" + expected.getId()),new byte[0]);
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
        assertNotNull(result.getPicture());
        assertArrayEquals(bodyPictureBase64,result.getPicture().array());
    }

    @Test
    public void should_fallback_to_default_on_missing_image() throws IOException, ParseException {
        // Given
        URL url = Resources.getResource("hakuho.html");
        String body = Resources.toString(url, Charsets.UTF_8);
        stubFor(get(urlEqualTo("/" + RIKISHI_URL + "666"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(body)));
        stubFor(get(urlEqualTo( "/" + PIC_URL + "666.jpg")).willReturn(aResponse().withStatus(404)));
        URL urlDefaultPicture = Resources.getResource("42.jpg");
        byte[] defaultPicture = Resources.toByteArray(urlDefaultPicture);
        byte[] defaultPictureBase64 = Base64.getEncoder().encode(defaultPicture);
        // When
        Rikishi result  = (Rikishi) tested.getDetail(new IdAndUrl(666,"http://localhost:8080/Rikishi.aspx?r=" + 666),defaultPicture);
        // Then
        assertNotNull(result);
        assertNotNull(result.getPicture());
        assertArrayEquals(defaultPictureBase64,result.getPicture().array());
    }


}
