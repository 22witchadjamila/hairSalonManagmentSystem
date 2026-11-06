package za.ac.cput.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoginAttemptService - lockout after repeated failures")
class LoginAttemptServiceTest {

    /** A clock the test can move. */
    private static final class MovingClock extends Clock {
        Instant now = Instant.parse("2026-11-06T10:00:00Z");
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
        void advance(Duration d) { now = now.plus(d); }
    }

    private final MovingClock clock = new MovingClock();
    private final LoginAttemptService service = new LoginAttemptService(clock);

    @Test
    @DisplayName("four wrong passwords do not lock; the fifth does")
    void locksOnFifthFailure() {
        for (int i = 0; i < 4; i++) assertFalse(service.recordFailure("lerato"), "failure " + (i + 1));
        assertTrue(service.lockedForSeconds("lerato").isEmpty());
        assertTrue(service.recordFailure("lerato"), "the 5th failure triggers the lock");
        assertTrue(service.lockedForSeconds("lerato").isPresent());
    }

    @Test
    @DisplayName("the lock lasts 15 minutes and then clears by itself")
    void lockExpires() {
        for (int i = 0; i < 5; i++) service.recordFailure("lerato");
        assertEquals(15 * 60, service.lockedForSeconds("lerato").getAsLong());
        clock.advance(Duration.ofMinutes(14));
        assertTrue(service.lockedForSeconds("lerato").isPresent());
        clock.advance(Duration.ofMinutes(2));
        assertTrue(service.lockedForSeconds("lerato").isEmpty());
    }

    @Test
    @DisplayName("a successful sign-in resets the count")
    void successResets() {
        for (int i = 0; i < 4; i++) service.recordFailure("lerato");
        service.recordSuccess("lerato");
        for (int i = 0; i < 4; i++) assertFalse(service.recordFailure("lerato"));
    }

    @Test
    @DisplayName("failures more than 15 minutes apart do not add up")
    void oldFailuresForgotten() {
        for (int i = 0; i < 4; i++) service.recordFailure("lerato");
        clock.advance(Duration.ofMinutes(16));
        assertFalse(service.recordFailure("lerato"));   // counts as the first again
        assertTrue(service.lockedForSeconds("lerato").isEmpty());
    }

    @Test
    @DisplayName("usernames are matched ignoring case and spaces, and each username counts separately")
    void keyedByUsername() {
        for (int i = 0; i < 5; i++) service.recordFailure("  Lerato ");
        assertTrue(service.lockedForSeconds("lerato").isPresent());
        assertTrue(service.lockedForSeconds("someone-else").isEmpty());
    }

    @Test
    @DisplayName("a made-up username locks exactly like a real one (so locking can't reveal who exists)")
    void unknownUsernamesLockToo() {
        for (int i = 0; i < 5; i++) service.recordFailure("no-such-person");
        assertTrue(service.lockedForSeconds("no-such-person").isPresent());
    }
}
