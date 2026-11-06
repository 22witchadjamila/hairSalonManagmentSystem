package za.ac.cput.config;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Ends other open sessions for a username - used when a password is changed or reset, so someone
 * who had the old password (or a stolen session) is signed out rather than left in.
 */
@Component
public class SessionControl {

    private final SessionRegistry registry;

    public SessionControl(SessionRegistry registry) {
        this.registry = registry;
    }

    /** @param exceptSessionId the caller's own session, which stays signed in; null ends all of them */
    public void expireSessionsOf(String username, String exceptSessionId) {
        for (Object principal : registry.getAllPrincipals()) {
            if (!(principal instanceof UserDetails details) || !details.getUsername().equals(username)) continue;
            for (SessionInformation session : registry.getAllSessions(principal, false)) {
                if (!session.getSessionId().equals(exceptSessionId)) session.expireNow();
            }
        }
    }
}
