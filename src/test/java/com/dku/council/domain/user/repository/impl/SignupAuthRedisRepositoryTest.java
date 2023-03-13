package com.dku.council.domain.user.repository.impl;

import com.dku.council.domain.user.model.SignupAuth;
import com.dku.council.global.config.redis.RedisKeys;
import com.dku.council.util.ClockUtil;
import com.dku.council.util.FullIntegrationTest;
import com.dku.council.util.base.AbstractContainerRedisTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//@FullIntegrationTest
class SignupAuthRedisRepositoryTest extends AbstractContainerRedisTest {

    @Autowired
    private SignupAuthRedisRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${app.auth.signup-expiration-minutes}")
    private Long expires;

    @Autowired
    private ObjectMapper objectMapper;

    private Instant now;
    private final Clock clock = ClockUtil.create();
    private final String token = "12345";
    private final String auth = "myauth";


    @BeforeEach
    public void setup(){
        now = Instant.now(clock);
    }

    @Test
    @DisplayName("Auth 저장이 잘 되는가")
    void setAuthPayload() throws JsonProcessingException {
        // given
        String key = repository.makeEntryKey(token, auth);
        ClockUtil.create();

        // when
        repository.setAuthPayload(token, auth, "MyData", now);

        // then
        Object o = redisTemplate.opsForHash().get(RedisKeys.SIGNUP_AUTH_KEY, key);
        assertThat(getAuth(o).getValue()).isEqualTo("MyData");
    }

    @Test
    @DisplayName("Auth 중복 저장시 덮어쓰기")
    void setAuthPayloadTwice() throws JsonProcessingException {
        // given
        String key = repository.makeEntryKey(token, auth);

        // when
        repository.setAuthPayload(token, auth, "MyData", now);
        repository.setAuthPayload(token, auth, "MyData22", now);

        // then
        Object o = redisTemplate.opsForHash().get(RedisKeys.SIGNUP_AUTH_KEY, key);
        assertThat(getAuth(o).getValue()).isEqualTo("MyData22");
    }

    @Test
    @DisplayName("Auth 잘 가져와지는가")
    void getAuthPayload() {
        // given
        repository.setAuthPayload(token, auth, "MyData", now);

        // when
        Optional<String> data = repository.getAuthPayload(token, auth, String.class, now);

        // then
        assertThat(data.orElseThrow()).isEqualTo("MyData");
    }

    @Test
    @DisplayName("만료된 경우 안가져와지는가")
    void getAuthPayloadWhenExpires() {
        // given
        repository.setAuthPayload(token, auth, "MyData", now);

        // when
        Optional<String> data = repository.getAuthPayload(token, auth, String.class,
                now.plus(expires + 1, ChronoUnit.MINUTES));

        // then
        assertThat(data.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Auth가 잘 삭제되는가")
    void deleteAuthPayload() {
        // given
        repository.setAuthPayload(token, auth, "MyData", now);

        // when
        boolean result = repository.deleteAuthPayload(token, auth);

        // then
        Long size = redisTemplate.opsForHash().size(RedisKeys.SIGNUP_AUTH_KEY);
        assertThat(size).isEqualTo(0);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Auth가 없을 때 삭제 안되야 함")
    void deleteAuthPayloadWhenNotFound() {
        // when
        boolean result = repository.deleteAuthPayload(token, auth);

        // then
        assertThat(result).isFalse();
    }

    private SignupAuth getAuth(Object o) throws JsonProcessingException {
        String str = (String) o;
        return objectMapper.readValue(str, SignupAuth.class);
    }
}