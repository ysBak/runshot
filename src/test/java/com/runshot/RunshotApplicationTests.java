package com.runshot;

import com.runshot.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import java.net.URI;
import java.net.http.*;
import java.util.regex.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RunshotApplicationTests {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private String field(String body, String name) {
        Matcher matcher = Pattern.compile("\"" + name + "\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
        assertTrue(matcher.find(), "Missing field " + name);
        return matcher.group(1);
    }
    private long id(String body) {
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
        assertTrue(matcher.find(), "Missing id");
        return Long.parseLong(matcher.group(1));
    }
    @Test void signupLoginAndOwnedRecordLifecycle() throws Exception {
        assertEquals(200, request("GET", "/", null, null).statusCode());
        assertEquals(401, request("GET", "/api/runs", null, null).statusCode());
        assertEquals(401, request("GET", "/api/runs", null, "invalid.jwt.token").statusCode());
        assertEquals(400, request("POST", "/api/users", null, null).statusCode());
        String signup = "{\"email\":\"Runner@example.com\",\"password\":\"ValidPass123!\",\"nickname\":\"러너\"}";
        var created = request("POST", "/api/users", signup, null);
        assertEquals(201, created.statusCode(), created.body());
        long userId = id(created.body());
        assertFalse(created.body().contains("password"));
        assertNotEquals("ValidPass123!", users.findById(userId).orElseThrow().getPassword());
        assertEquals(409, request("POST", "/api/users", signup, null).statusCode());
        assertEquals(401, request("POST", "/api/auth/login", "{\"email\":\"runner@example.com\",\"password\":\"wrong\"}", null).statusCode());
        var login = request("POST", "/api/auth/login", "{\"email\":\"RUNNER@example.com\",\"password\":\"ValidPass123!\"}", null);
        assertEquals(200, login.statusCode(), login.body());
        String token = field(login.body(), "accessToken");
        assertEquals(200, request("GET", "/api/auth/me", null, token).statusCode());
        assertEquals("[]", request("GET", "/api/runs", null, token).body());
        String run = "{\"distance\":6.5,\"runningTime\":2400,\"runningDate\":\"2026-01-01\",\"content\":\"오늘의 러닝 <script>\"}";
        var added = request("POST", "/api/runs", run, token);
        assertEquals(201, added.statusCode(), added.body());
        long runId = id(added.body());
        assertTrue(added.body().contains("\"userId\":" + userId));
        assertTrue(added.body().contains("\"pace\":369"));
        assertFalse(added.body().contains("password"));
        assertTrue(request("GET", "/api/runs", null, token).body().contains("\"id\":" + runId));
        assertEquals(400, request("POST", "/api/runs", "{\"distance\":0}", token).statusCode());
        assertEquals(400, request("PUT", "/api/runs/" + runId, "{}", token).statusCode());
        String second = "{\"email\":\"second@example.com\",\"password\":\"ValidPass123!\",\"nickname\":\"둘째\"}";
        assertEquals(201, request("POST", "/api/users", second, null).statusCode());
        String otherToken = field(request("POST", "/api/auth/login", "{\"email\":\"second@example.com\",\"password\":\"ValidPass123!\"}", null).body(), "accessToken");
        assertEquals("[]", request("GET", "/api/runs", null, otherToken).body());
        assertEquals(403, request("GET", "/api/runs/users/" + userId, null, otherToken).statusCode());
        assertEquals(404, request("GET", "/api/runs/" + runId, null, otherToken).statusCode());
        assertEquals(404, request("PUT", "/api/runs/" + runId, run, otherToken).statusCode());
        assertEquals(404, request("DELETE", "/api/runs/" + runId, null, otherToken).statusCode());
        assertEquals(403, request("POST", "/api/runs", run.replace("{", "{\"userId\":" + userId + ","), otherToken).statusCode());
        var updated = request("PUT", "/api/runs/" + runId, run.replace("6.5", "8.0"), token);
        assertEquals(200, updated.statusCode(), updated.body());
        assertTrue(updated.body().contains("\"pace\":300"));
        assertTrue(request("GET", "/api/runs/" + runId, null, token).body().contains("\"distance\":8.0"));
        assertEquals(204, request("DELETE", "/api/runs/" + runId, null, token).statusCode());
        assertEquals(404, request("GET", "/api/runs/" + runId, null, token).statusCode());
        assertEquals("[]", request("GET", "/api/runs", null, token).body());
    }
}
