package za.ac.cput.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Vue Router runs in history mode, so a direct visit or refresh on a client-side route
 * (e.g. /admin/customers) arrives at the server as a real GET request. Spring only has actual
 * files for /assets/**, /favicon.ico etc. (all of which have a dot in the last segment) - so
 * this forwards any dot-less path to index.html and lets Vue Router take over from there.
 * Real API/controller mappings (e.g. /api/customers) still win over this because Spring picks
 * the most specific matching pattern per request, not the first one registered.
 */
@Controller
public class SpaFallbackController {

    // Spring 6's PathPattern parser rejects "**" followed by more pattern data (e.g. "/**/{path}"),
    // so each route depth is spelled out explicitly instead - this app's deepest route is 2
    // segments (e.g. /customer/appointments), the 3rd level is just headroom.
    @GetMapping(value = {
            "/{path:[^.]*}",
            "/*/{path:[^.]*}",
            "/*/*/{path:[^.]*}"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
