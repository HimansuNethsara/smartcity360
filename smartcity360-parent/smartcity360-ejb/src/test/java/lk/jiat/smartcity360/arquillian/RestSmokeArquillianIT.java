package lk.jiat.smartcity360.arquillian;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("arquillian")
@RunAsClient
@ExtendWith(ArquillianExtension.class)
public class RestSmokeArquillianIT {

    @Deployment(testable = false)
    public static WebArchive probe() {
        return ShrinkWrap.create(WebArchive.class, "sc360-probe.war")
                .addAsWebResource(new StringAsset("probe"), "index.html");
    }

    private static final String BASE = System.getProperty("sc360.base", "http://localhost:8080/smartcity360");
    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");


    /** Looks a setting up as -D property, then as environment variable, then in sc360-test.properties. */
    private static String setting(String name) {
        String v = System.getProperty(name);
        if (v == null || v.isBlank()) {
            v = System.getenv(name.toUpperCase().replace('.', '_'));
        }
        if (v == null || v.isBlank()) {
            try (java.io.InputStream in = RestSmokeArquillianIT.class.getResourceAsStream("/sc360-test.properties")) {
                if (in != null) {
                    java.util.Properties props = new java.util.Properties();
                    props.load(in);
                    v = props.getProperty(name);
                }
            } catch (java.io.IOException ignored) {
                // no file: the optional tests simply skip
            }
        }
        return v == null || v.isBlank() ? null : v.trim();
    }

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + path)).timeout(Duration.ofSeconds(15)).GET();
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path)).timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private String login(String user, String pass) throws Exception {
        HttpResponse<String> r = post("/api/auth/login", "{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}");
        assertEquals(200, r.statusCode(), "login failed: " + r.body());
        Matcher m = TOKEN.matcher(r.body());
        assertTrue(m.find(), "no token in login response");
        return m.group(1);
    }

    // ------------------------------------------------------------------ no credentials needed

    @Test
    @DisplayName("The public pages load")
    void publicPages() throws Exception {
        assertEquals(200, get("/login.jsp", null).statusCode());
        assertEquals(200, get("/index.jsp", null).statusCode());
    }

    @Test
    @DisplayName("Protected API calls without a token are refused with 401")
    void apiNeedsLogin() throws Exception {
        for (String path : new String[]{"/api/meters", "/api/zones/tree", "/api/complaints", "/api/notifications", "/api/dashboard/summary"}) {
            HttpResponse<String> r = get(path, null);
            assertEquals(401, r.statusCode(), path);
            assertTrue(r.body().contains("unauthenticated"), path + " -> " + r.body());
        }
    }

    @Test
    @DisplayName("A forged or garbage token is refused")
    void badToken() throws Exception {
        assertEquals(401, get("/api/meters", "not.a.token").statusCode());
    }

    @Test
    @DisplayName("Login with missing details is a 400; wrong details is a 401 with a generic message")
    void loginErrors() throws Exception {
        HttpResponse<String> empty = post("/api/auth/login", "{\"username\":\"\",\"password\":\"\"}");
        assertEquals(400, empty.statusCode());

        HttpResponse<String> wrong = post("/api/auth/login",
                "{\"username\":\"nobody-" + System.nanoTime() + "\",\"password\":\"Wrong1234\"}");
        assertEquals(401, wrong.statusCode());
        assertTrue(wrong.body().contains("bad_credentials"));
        assertFalse(wrong.body().toLowerCase().contains("does not exist"), "must not reveal whether the user exists");
    }

    @Test
    @DisplayName("Malformed JSON gets a clean 400 and every response carries a request id")
    void malformedJsonAndRequestId() throws Exception {
        HttpResponse<String> r = post("/api/auth/login", "{this is not json");
        assertEquals(400, r.statusCode());
        assertTrue(r.headers().firstValue("X-Request-Id").isPresent(), "X-Request-Id header expected");
    }

    // ------------------------------------------------------------------ optional: needs credentials

    @Test
    @DisplayName("A signed-in user can read their profile, dashboard and notification count")
    void signedInReads() throws Exception {
        String user = setting("sc360.test.user");
        String pass = setting("sc360.test.pass");
        assumeTrue(user != null && pass != null, "pass -Dsc360.test.user and -Dsc360.test.pass to run this test");

        String token = login(user, pass);
        HttpResponse<String> me = get("/api/auth/me", token);
        assertEquals(200, me.statusCode());
        assertTrue(me.body().contains(user));
        assertEquals(200, get("/api/dashboard/summary", token).statusCode());
        assertEquals(200, get("/api/notifications/unread-count", token).statusCode());
    }

    @Test
    @DisplayName("A citizen is blocked from staff-only endpoints (403) but can still read their own complaints")
    void citizenIsBlocked() throws Exception {
        String user = setting("sc360.test.citizen.user");
        String pass = setting("sc360.test.citizen.pass");
        assumeTrue(user != null && pass != null,
                "pass -Dsc360.test.citizen.user and -Dsc360.test.citizen.pass to run this test");

        String token = login(user, pass);
        assertEquals(403, get("/api/zones/tree", token).statusCode());
        assertEquals(403, get("/api/meters", token).statusCode());
        assertEquals(200, get("/api/complaints", token).statusCode(), "citizens can see their own complaints");
    }
}