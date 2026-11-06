package za.ac.cput.config;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows down password guessing: five wrong passwords for one username within 15 minutes locks
 * that username for 15 minutes - even for the correct password - and a successful sign-in resets
 * the count. (The chapter's "protect the system from service interruptions" - an attacker can't
 * hammer the login endpoint with guesses.)
 *
 * Counted per USERNAME as typed, whether or not that account exists, so the lock behaves the
 * same for real and made-up names and can't be used to find out which usernames exist.
 *
 * Kept in memory on purpose: it is simple and has no table to maintain, and a restart clearing
 * all locks is acceptable for this application. The trade-off is that someone could lock a known
 * username out for 15 minutes by failing on purpose; it clears itself, and the audit log records
 * the lock.
 */
@Component
public class LoginAttemptService {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);
    static final Duration LOCK = Duration.ofMinutes(15);
    private static final int MAX_TRACKED = 10_000;

    private record State(int failures, Instant firstFailure, Instant lockedUntil) {}

    private final Map<String, State> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    /** Seconds left on a lock, or empty if the username can try. */
    public OptionalLong lockedForSeconds(String username) {
        State s = attempts.get(key(username));
        if (s == null || s.lockedUntil() == null) return OptionalLong.empty();
        long left = Duration.between(clock.instant(), s.lockedUntil()).getSeconds();
        if (left <= 0) {
            attempts.remove(key(username));
            return OptionalLong.empty();
        }
        return OptionalLong.of(left);
    }

    /** Records a wrong password. Returns true if THIS failure is the one that locks the account. */
    public boolean recordFailure(String username) {
        Instant now = clock.instant();
        if (attempts.size() >= MAX_TRACKED) purgeExpired(now);
        if (attempts.size() >= MAX_TRACKED) return false;   // full of live entries: stop tracking rather than grow forever
        boolean[] locked = {false};
        attempts.compute(key(username), (k, old) -> {
            boolean fresh = old == null || old.lockedUntil() != null
                    || Duration.between(old.firstFailure(), now).compareTo(WINDOW) > 0;
            int failures = fresh ? 1 : old.failures() + 1;
            Instant first = fresh ? now : old.firstFailure();
            if (failures >= MAX_FAILURES) {
                locked[0] = true;
                return new State(failures, first, now.plus(LOCK));
            }
            return new State(failures, first, null);
        });
        return locked[0];
    }

    public void recordSuccess(String username) {
        attempts.remove(key(username));
    }

    private void purgeExpired(Instant now) {
        for (Iterator<Map.Entry<String, State>> it = attempts.entrySet().iterator(); it.hasNext(); ) {
            State s = it.next().getValue();
            boolean lockOver = s.lockedUntil() != null && !s.lockedUntil().isAfter(now);
            boolean windowOver = s.lockedUntil() == null && Duration.between(s.firstFailure(), now).compareTo(WINDOW) > 0;
            if (lockOver || windowOver) it.remove();
        }
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
