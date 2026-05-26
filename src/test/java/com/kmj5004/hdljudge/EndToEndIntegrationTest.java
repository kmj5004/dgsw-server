package com.kmj5004.hdljudge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.kmj5004.hdljudge.common.enums.Language;
import com.kmj5004.hdljudge.common.enums.Role;
import com.kmj5004.hdljudge.domain.user.User;
import com.kmj5004.hdljudge.domain.user.UserRepository;
import com.kmj5004.hdljudge.judge.adapter.HdlAdapter;
import com.kmj5004.hdljudge.judge.adapter.ResourceLimits;
import com.kmj5004.hdljudge.judge.adapter.SimulationOutcome;
import com.kmj5004.hdljudge.judge.adapter.SynthesisOptions;
import com.kmj5004.hdljudge.judge.adapter.SynthesisOutcome;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;








@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("local")
@Import({TestcontainersConfiguration.class, EndToEndIntegrationTest.StubAdapterConfig.class})
class EndToEndIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;



    @Test
    void authFullFlowSignupLoginRefreshReplayLogout() {
        String email = uniqueEmail("auth");
        String password = "hunter2hunter2";


        ResponseEntity<JsonNode> signup = call(HttpMethod.POST, "/api/auth/signup",
            Map.of("email", email, "password", password), null);
        assertThat(signup.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(signup.getBody().path("data").path("role").asText()).isEqualTo("USER");


        Tokens login = login(email, password);
        assertThat(login.access).isNotBlank();
        assertThat(login.refresh).isNotBlank();


        ResponseEntity<JsonNode> refresh = call(HttpMethod.POST, "/api/auth/refresh",
            Map.of("refreshToken", login.refresh), null);
        assertThat(refresh.getStatusCode()).isEqualTo(HttpStatus.OK);
        String newAccess = refresh.getBody().path("data").path("accessToken").asText();
        String newRefresh = refresh.getBody().path("data").path("refreshToken").asText();
        assertThat(newAccess).isNotEqualTo(login.access);
        assertThat(newRefresh).isNotEqualTo(login.refresh);


        ResponseEntity<JsonNode> replay = call(HttpMethod.POST, "/api/auth/refresh",
            Map.of("refreshToken", login.refresh), null);
        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(replay.getBody().path("error").path("code").asText()).isEqualTo("INVALID_TOKEN");


        ResponseEntity<JsonNode> postReplay = call(HttpMethod.POST, "/api/auth/refresh",
            Map.of("refreshToken", newRefresh), null);
        assertThat(postReplay.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);


        Tokens fresh = login(email, password);
        ResponseEntity<JsonNode> logout = call(HttpMethod.POST, "/api/auth/logout", null, fresh.access);
        assertThat(logout.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);


        ResponseEntity<JsonNode> afterLogout = call(HttpMethod.POST, "/api/auth/refresh",
            Map.of("refreshToken", fresh.refresh), null);
        assertThat(afterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authValidationRejectsBlankAndShort() {
        ResponseEntity<JsonNode> blank = call(HttpMethod.POST, "/api/auth/signup",
            Map.of("email", "", "password", "hunter2hunter2"), null);
        assertThat(blank.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(blank.getBody().path("error").path("code").asText()).isEqualTo("INVALID_INPUT");

        ResponseEntity<JsonNode> shortPw = call(HttpMethod.POST, "/api/auth/signup",
            Map.of("email", uniqueEmail("v"), "password", "short"), null);
        assertThat(shortPw.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }



    @Test
    void challengeCrudAdminMatrix() {
        Tokens user = newUser("c-user");
        Tokens admin = newAdmin("c-admin");
        String slug = uniqueSlug("hex");


        ResponseEntity<JsonNode> userCreate = call(HttpMethod.POST, "/api/challenges",
            challengePayload(slug), user.access);
        assertThat(userCreate.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);


        ResponseEntity<JsonNode> created = call(HttpMethod.POST, "/api/challenges",
            challengePayload(slug), admin.access);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long id = created.getBody().path("data").path("id").asLong();


        ResponseEntity<JsonNode> dup = call(HttpMethod.POST, "/api/challenges",
            challengePayload(slug), admin.access);
        assertThat(dup.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(dup.getBody().path("error").path("code").asText()).isEqualTo("SLUG_ALREADY_EXISTS");


        ResponseEntity<JsonNode> detail = call(HttpMethod.GET, "/api/challenges/" + slug, null, null);
        assertThat(detail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detail.getBody().path("data").has("hiddenTestbench")).isFalse();


        ResponseEntity<JsonNode> userPut = call(HttpMethod.PUT, "/api/challenges/" + id,
            updatePayload(), user.access);
        assertThat(userPut.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);


        ResponseEntity<JsonNode> put = call(HttpMethod.PUT, "/api/challenges/" + id,
            updatePayload(), admin.access);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(put.getBody().path("data").path("difficulty").asText()).isEqualTo("MEDIUM");


        ResponseEntity<JsonNode> del = call(HttpMethod.DELETE, "/api/challenges/" + id, null, admin.access);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);


        ResponseEntity<JsonNode> gone = call(HttpMethod.GET, "/api/challenges/" + slug, null, null);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void challengeListFiltersByDifficultyAndTag() {
        Tokens admin = newAdmin("filter-admin");


        String slugA = uniqueSlug("filtera");
        String slugB = uniqueSlug("filterb");
        String tagA = "filter-tag-a-" + System.nanoTime();
        String tagB = "filter-tag-b-" + System.nanoTime();
        ObjectNode payloadA = challengePayload(slugA);
        payloadA.put("difficulty", "EASY");
        payloadA.putArray("tags").add(tagA);
        ObjectNode payloadB = challengePayload(slugB);
        payloadB.put("difficulty", "HARD");
        payloadB.putArray("tags").add(tagB);

        ResponseEntity<JsonNode> ca = call(HttpMethod.POST, "/api/challenges", payloadA, admin.access);
        assertThat(ca.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ResponseEntity<JsonNode> cb = call(HttpMethod.POST, "/api/challenges", payloadB, admin.access);
        assertThat(cb.getStatusCode()).isEqualTo(HttpStatus.CREATED);


        ResponseEntity<JsonNode> byTagA = call(HttpMethod.GET, "/api/challenges?tag=" + tagA, null, null);
        assertThat(byTagA.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(slugsOf(byTagA.getBody())).contains(slugA).doesNotContain(slugB);

        ResponseEntity<JsonNode> byTagB = call(HttpMethod.GET, "/api/challenges?tag=" + tagB, null, null);
        assertThat(slugsOf(byTagB.getBody())).contains(slugB).doesNotContain(slugA);


        ResponseEntity<JsonNode> tagAEasy = call(HttpMethod.GET, "/api/challenges?tag=" + tagA + "&difficulty=EASY", null, null);
        assertThat(slugsOf(tagAEasy.getBody())).contains(slugA);
        ResponseEntity<JsonNode> tagAHard = call(HttpMethod.GET, "/api/challenges?tag=" + tagA + "&difficulty=HARD", null, null);
        assertThat(slugsOf(tagAHard.getBody())).doesNotContain(slugA);
    }



    @Test
    void judgeCorrectSubmissionScores100() {
        Tokens user = newUser("judge-correct");
        Tokens admin = newAdmin("judge-correct-admin");
        String slug = uniqueSlug("jc");
        call(HttpMethod.POST, "/api/challenges", challengePayload(slug), admin.access);

        long submissionId = submitAndPoll(slug, "// MARK:GOOD\nmodule x; endmodule", user);
        ResponseEntity<JsonNode> detail = call(HttpMethod.GET, "/api/submissions/" + submissionId, null, user.access);
        assertThat(detail.getBody().path("data").path("status").asText()).isEqualTo("COMPLETED");
        assertThat(detail.getBody().path("data").path("score").asInt()).isEqualTo(100);
        assertThat(detail.getBody().path("data").path("runs").size()).isEqualTo(4);
    }

    @Test
    void judgePartialSubmissionScoresLessThan100() {
        Tokens user = newUser("judge-partial");
        Tokens admin = newAdmin("judge-partial-admin");
        String slug = uniqueSlug("jp");
        call(HttpMethod.POST, "/api/challenges", challengePayload(slug), admin.access);

        long submissionId = submitAndPoll(slug, "// MARK:BAD\nmodule x; endmodule", user);
        ResponseEntity<JsonNode> detail = call(HttpMethod.GET, "/api/submissions/" + submissionId, null, user.access);
        assertThat(detail.getBody().path("data").path("status").asText()).isEqualTo("COMPLETED");
        int score = detail.getBody().path("data").path("score").asInt();
        assertThat(score).isLessThan(100).isGreaterThanOrEqualTo(0);
    }

    @Test
    void judgeBrokenSubmissionEndsInError() {
        Tokens user = newUser("judge-broken");
        Tokens admin = newAdmin("judge-broken-admin");
        String slug = uniqueSlug("je");
        call(HttpMethod.POST, "/api/challenges", challengePayload(slug), admin.access);

        long submissionId = submitAndPoll(slug, "// MARK:BROKEN\nmodule x; endmodule", user);
        ResponseEntity<JsonNode> detail = call(HttpMethod.GET, "/api/submissions/" + submissionId, null, user.access);
        assertThat(detail.getBody().path("data").path("status").asText()).isEqualTo("ERROR");
        assertThat(detail.getBody().path("data").path("score").asInt()).isZero();
    }

    @Test
    void judgeOwnershipUserCannotReadOthersSubmissions() {
        Tokens owner = newUser("owner");
        Tokens stranger = newUser("stranger");
        Tokens admin = newAdmin("ownership-admin");
        String slug = uniqueSlug("own");
        call(HttpMethod.POST, "/api/challenges", challengePayload(slug), admin.access);

        long submissionId = submitAndPoll(slug, "// MARK:GOOD\nmodule x; endmodule", owner);

        ResponseEntity<JsonNode> sneak = call(HttpMethod.GET, "/api/submissions/" + submissionId, null, stranger.access);
        assertThat(sneak.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);


        ResponseEntity<JsonNode> adminRead = call(HttpMethod.GET, "/api/submissions/" + submissionId, null, admin.access);
        assertThat(adminRead.getStatusCode()).isEqualTo(HttpStatus.OK);
    }



    @Test
    void leaderboardRanksByScoreDescending() {
        Tokens admin = newAdmin("lb-admin");
        Tokens winner = newUser("lb-winner");
        Tokens loser = newUser("lb-loser");
        String slug = uniqueSlug("lb");
        call(HttpMethod.POST, "/api/challenges", challengePayload(slug), admin.access);


        submitAndPoll(slug, "// MARK:GOOD\nmodule x; endmodule", winner);
        submitAndPoll(slug, "// MARK:BAD\nmodule x; endmodule", loser);

        ResponseEntity<JsonNode> lb = call(HttpMethod.GET, "/api/challenges/" + slug + "/leaderboard", null, null);
        assertThat(lb.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode rows = lb.getBody().path("data");
        assertThat(rows.size()).isEqualTo(2);
        assertThat(rows.get(0).path("rank").asInt()).isEqualTo(1);
        assertThat(rows.get(0).path("bestScore").asInt()).isEqualTo(100);
        assertThat(rows.get(1).path("rank").asInt()).isEqualTo(2);
        assertThat(rows.get(1).path("bestScore").asInt()).isLessThan(100);
    }

    @Test
    void leaderboardOnUnknownChallengeReturns404() {
        ResponseEntity<JsonNode> r = call(HttpMethod.GET, "/api/challenges/no-such-slug/leaderboard", null, null);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(r.getBody().path("error").path("code").asText()).isEqualTo("CHALLENGE_NOT_FOUND");
    }



    @Test
    void playgroundSimulateRunsThroughAdapter() {
        Tokens user = newUser("pg-user");
        ResponseEntity<JsonNode> r = call(HttpMethod.POST, "/api/playground/simulate",
            Map.of("code", "// MARK:GOOD\nmodule x; endmodule", "testbench", "module tb; endmodule"),
            user.access);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().path("data").path("status").asText()).isEqualTo("OK");
        assertThat(r.getBody().path("data").path("stdout").asText()).contains("HDLJUDGE_VEC");
    }

    @Test
    void playgroundSimulateRequiresAuth() {
        ResponseEntity<JsonNode> r = call(HttpMethod.POST, "/api/playground/simulate",
            Map.of("code", "// MARK:GOOD\nmodule x; endmodule", "testbench", "module tb; endmodule"),
            null);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void playgroundSynthesizeReturnsSvgAndCachesByHash() {
        Tokens user = newUser("syn-user");
        String code = "module synth_demo; endmodule\n// hash-key:" + System.nanoTime();

        ResponseEntity<JsonNode> first = call(HttpMethod.POST, "/api/playground/synthesize",
            Map.of("code", code), user.access);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody().path("data").path("ok").asBoolean()).isTrue();
        assertThat(first.getBody().path("data").path("cached").asBoolean()).isFalse();
        assertThat(first.getBody().path("data").path("svg").asText()).contains("<svg");
        assertThat(first.getBody().path("data").path("gateCount").asInt()).isPositive();


        ResponseEntity<JsonNode> second = call(HttpMethod.POST, "/api/playground/synthesize",
            Map.of("code", code), user.access);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getBody().path("data").path("cached").asBoolean()).isTrue();
    }

    @Test
    void playgroundSynthesizeBrokenCodeReturnsOkFalse() {
        Tokens user = newUser("syn-broken");
        ResponseEntity<JsonNode> r = call(HttpMethod.POST, "/api/playground/synthesize",
            Map.of("code", "// MARK:BROKEN\nbad code"), user.access);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().path("data").path("ok").asBoolean()).isFalse();
    }



    private long submitAndPoll(String slug, String code, Tokens user) {
        ResponseEntity<JsonNode> submitted = call(HttpMethod.POST, "/api/submissions",
            Map.of("challengeSlug", slug, "code", code), user.access);
        assertThat(submitted.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long id = submitted.getBody().path("data").path("id").asLong();

        await().atMost(Duration.ofSeconds(15)).pollInterval(Duration.ofMillis(200)).until(() -> {
            ResponseEntity<JsonNode> r = call(HttpMethod.GET, "/api/submissions/" + id, null, user.access);
            String status = r.getBody().path("data").path("status").asText();
            return List.of("COMPLETED", "FAILED", "TIMEOUT", "ERROR").contains(status);
        });
        return id;
    }

    private Tokens login(String email, String password) {
        ResponseEntity<JsonNode> login = call(HttpMethod.POST, "/api/auth/login",
            Map.of("email", email, "password", password), null);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        return new Tokens(
            login.getBody().path("data").path("accessToken").asText(),
            login.getBody().path("data").path("refreshToken").asText()
        );
    }

    private Tokens newUser(String prefix) {
        String email = uniqueEmail(prefix);
        String pw = "hunter2hunter2";
        ResponseEntity<JsonNode> r = call(HttpMethod.POST, "/api/auth/signup",
            Map.of("email", email, "password", pw), null);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return login(email, pw);
    }

    @Transactional
    User promote(String email) {
        User u = users.findByEmail(email).orElseThrow();
        u.promoteToAdmin();
        return users.save(u);
    }

    private Tokens newAdmin(String prefix) {
        String email = uniqueEmail(prefix);
        String pw = "hunter2hunter2";
        call(HttpMethod.POST, "/api/auth/signup", Map.of("email", email, "password", pw), null);
        promote(email);
        return login(email, pw);
    }

    private ObjectNode challengePayload(String slug) {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("slug", slug);
        payload.put("title", "Half Adder");
        payload.put("description", "두 비트 합·캐리.");
        payload.put("language", Language.VERILOG.name());
        payload.put("skeleton", "module half_adder(input a, b, output sum, carry); endmodule\n");
        payload.put("hiddenTestbench", "module tb; endmodule\n");
        payload.put("timeLimitNs", 100_000_000L);
        payload.put("wallTimeLimitMs", 5_000);
        payload.put("memoryLimitMb", 256);
        payload.put("difficulty", "EASY");
        payload.putArray("tags").add("combinational");
        var vectors = payload.putArray("testVectors");
        for (int i = 1; i <= 4; i++) {
            int sum = (i == 2 || i == 3) ? 1 : 0;
            int carry = (i == 4) ? 1 : 0;
            ObjectNode tv = vectors.addObject();
            tv.put("ordering", i);
            tv.put("stimulusJson", "{\"i\":" + i + "}");
            tv.put("expectedJson", "{\"sum\":" + sum + ",\"carry\":" + carry + "}");
            tv.put("weight", 25);
        }
        return payload;
    }

    private ObjectNode updatePayload() {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("title", "Half Adder (revised)");
        payload.put("description", "revised");
        payload.put("skeleton", "module x; endmodule\n");
        payload.put("hiddenTestbench", "module tb; endmodule\n");
        payload.put("timeLimitNs", 200_000_000L);
        payload.put("wallTimeLimitMs", 6_000);
        payload.put("memoryLimitMb", 256);
        payload.put("difficulty", "MEDIUM");
        payload.putArray("tags").add("combinational");
        return payload;
    }

    private List<String> slugsOf(JsonNode body) {
        List<String> slugs = new java.util.ArrayList<>();
        for (JsonNode item : body.path("data").path("content")) {
            slugs.add(item.path("slug").asText());
        }
        return slugs;
    }

    private ResponseEntity<JsonNode> call(HttpMethod method, String path, Object body, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (accessToken != null) {
            headers.setBearerAuth(accessToken);
        }
        HttpEntity<Object> req = new HttpEntity<>(body, headers);
        return rest.exchange(path, method, req, JsonNode.class);
    }

    private static String uniqueEmail(String prefix) {
        return prefix + "-" + System.nanoTime() + "@example.com";
    }

    private static String uniqueSlug(String prefix) {
        return prefix + "-" + System.nanoTime();
    }

    record Tokens(String access, String refresh) {}

    @TestConfiguration
    static class StubAdapterConfig {

        @Bean
        @Primary
        HdlAdapter stubAdapter() {
            return new HdlAdapter() {
                @Override
                public Language language() {
                    return Language.VERILOG;
                }

                @Override
                public SimulationOutcome simulate(String userCode, String testbench, ResourceLimits limits) {
                    if (userCode.contains("MARK:BROKEN")) {
                        return new SimulationOutcome(SimulationOutcome.Status.ERROR, "", "compile failed", 1, 100);
                    }
                    boolean correct = !userCode.contains("MARK:BAD");
                    int[] sumTrue = {0, 1, 1, 0};
                    int[] carryTrue = {0, 0, 0, 1};
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 4; i++) {
                        int sum = correct ? sumTrue[i] : 0;
                        int carry = correct ? carryTrue[i] : 0;
                        sb.append(String.format(
                            "::HDLJUDGE_VEC::ordering=%d::output={\"sum\":%d,\"carry\":%d}%n",
                            i + 1, sum, carry));
                    }
                    return new SimulationOutcome(SimulationOutcome.Status.OK, sb.toString(), "", 0, 100);
                }

                @Override
                public SynthesisOutcome synthesize(String userCode, SynthesisOptions options) {
                    if (userCode.contains("MARK:BROKEN")) {
                        return SynthesisOutcome.failure("syntax error");
                    }
                    String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\">stub</svg>";
                    String json = "{\"modules\":{\"x\":{\"cells\":{}}}}";
                    return SynthesisOutcome.success(svg, json, 5, 1);
                }
            };
        }
    }
}
