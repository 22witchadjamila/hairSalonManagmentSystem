package za.ac.cput.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.config.PermissionPolicy;
import za.ac.cput.config.RoleSeeder;
import za.ac.cput.domain.Stylist;
import za.ac.cput.service.AuthService;
import za.ac.cput.service.SearchService;

import java.util.List;

/** The dashboard header search. Results depend on who is asking - see SearchService. */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService search;
    private final AuthService authService;
    private final PermissionPolicy perm;

    public SearchController(SearchService search, AuthService authService, PermissionPolicy perm) {
        this.search = search;
        this.authService = authService;
        this.perm = perm;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STYLIST') or @perm.canViewAnything()")
    public List<SearchService.Result> search(@RequestParam(name = "q", defaultValue = "") String q,
                                             Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + RoleSeeder.ADMIN).equals(a.getAuthority()));
        if (admin) return search.forAdmin(q);
        boolean stylist = authentication.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + RoleSeeder.STYLIST).equals(a.getAuthority()));
        // Custom roles: only the modules their role can view, never logins.
        if (!stylist) return search.forStaff(q, perm.viewableModules(authentication), false);
        String stylistId = authService.findStylistFor(authentication.getName())
                .map(Stylist::getStylistId).orElse(null);
        return search.forStylist(q, stylistId);
    }
}
