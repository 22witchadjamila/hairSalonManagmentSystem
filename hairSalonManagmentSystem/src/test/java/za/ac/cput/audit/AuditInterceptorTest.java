package za.ac.cput.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditInterceptor - what gets recorded")
class AuditInterceptorTest {

    @Mock AuditService audit;
    @InjectMocks AuditInterceptor interceptor;

    private void run(String method, String path, int status) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(status);
        interceptor.afterCompletion(request, response, new Object(), null);
    }

    @Test
    @DisplayName("a successful change is recorded with a readable label and SUCCESS")
    void recordsSuccessfulChange() {
        run("POST", "/api/payments/P1/refund", 200);
        verify(audit).record(any(), eq("Refunded a payment"), eq(200), eq("SUCCESS"), isNull());
    }

    @Test
    @DisplayName("a refused change is recorded as DENIED (401/403) or FAILED (anything else)")
    void recordsRefusals() {
        run("DELETE", "/api/users/U1", 403);
        verify(audit).record(any(), eq("Deleted a login"), eq(403), eq("DENIED"), isNull());
        run("POST", "/api/payments/record", 400);
        verify(audit).record(any(), eq("Recorded a payment"), eq(400), eq("FAILED"), isNull());
    }

    @Test
    @DisplayName("reads are not recorded - they would drown the log")
    void ignoresReads() {
        run("GET", "/api/payments", 200);
        verifyNoInteractions(audit);
    }

    @Test
    @DisplayName("sign-in, sign-up and sign-out are left to the code that knows the username")
    void skipsAuthEndpoints() {
        run("POST", "/api/auth/login", 200);
        run("POST", "/api/auth/register", 201);
        run("POST", "/api/auth/logout", 204);
        verify(audit, never()).record(any(), any(), org.mockito.ArgumentMatchers.anyInt(), any(), any());
    }

    @Test
    @DisplayName("changing your own password IS recorded (it is not one of the skipped three)")
    void recordsPasswordChange() {
        run("POST", "/api/auth/change-password", 204);
        verify(audit).record(any(), eq("Changed own password"), eq(204), eq("SUCCESS"), isNull());
    }
}
