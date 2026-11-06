package za.ac.cput.service;

import org.springframework.stereotype.Service;
import za.ac.cput.domain.*;
import za.ac.cput.domain.enums.PermissionModule;
import za.ac.cput.repository.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The header ("global") search, scoped to what the caller is allowed to see.
 *
 *   ADMIN       - customers, stylists, services, appointments, promotions, products, logins
 *   custom role - only the modules their role can VIEW (never logins)
 *   STYLIST     - their OWN appointments, and the service list
 *
 * Scoping happens here on the server. Filtering in the browser would mean first sending a
 * stylist every customer's details and trusting the page to hide them.
 *
 * Global search vs section search: this finds records ACROSS sections and links to the section
 * page with ?q= set, where that page's own search box (filtering just its table) takes over.
 * Both follow the same rules - trimmed, case-insensitive, every word must match.
 *
 * Matching and ranking:
 *   - the query is trimmed and split into words; EVERY word must appear in the record's
 *     searchable fields ("jane doe" and "doe jane" both find Jane Doe)
 *   - only fields a person would search by: names, email, phone, code, service, date, status -
 *     never ids, prices or internal notes
 *   - each word scores by how well it hits a field: whole field > start of field > start of a
 *     word > anywhere, and the record's primary field (name, code) counts double
 *   - results are sorted best-first across all types, so the strongest hit comes first rather
 *     than whatever happened to be first in the table; capped per type and overall
 * The salon's data is small (hundreds of rows), so this filters in memory; a large deployment
 * would push the matching into SQL or a full-text index instead.
 */
@Service
public class SearchService {

    public record Result(String type, String id, String title, String subtitle, String link, int score) {}

    static final int MIN_LENGTH = 2;
    private static final int PER_TYPE = 5;
    private static final int MAX_RESULTS = 20;

    private final CustomerRepository customers;
    private final StylistRepository stylists;
    private final SalonServiceRepository services;
    private final AppointmentRepository appointments;
    private final PromotionRepository promotions;
    private final ProductInventoryRepository products;
    private final UserRepository users;

    public SearchService(CustomerRepository customers, StylistRepository stylists, SalonServiceRepository services,
                         AppointmentRepository appointments, PromotionRepository promotions,
                         ProductInventoryRepository products, UserRepository users) {
        this.customers = customers;
        this.stylists = stylists;
        this.services = services;
        this.appointments = appointments;
        this.promotions = promotions;
        this.products = products;
        this.users = users;
    }

    public List<Result> forAdmin(String query) {
        return forStaff(query, EnumSet.allOf(PermissionModule.class), true);
    }

    /** Admins and custom roles. Only the given modules are searched; logins only for admins. */
    public List<Result> forStaff(String query, Set<PermissionModule> modules, boolean includeLogins) {
        List<String> words = words(query);
        if (words.isEmpty()) return List.of();
        List<Result> out = new ArrayList<>();
        if (modules.contains(PermissionModule.CUSTOMERS))
            out.addAll(match(customers.findAll().stream(), words,
                    c -> fields(c.getFirstName() + " " + c.getLastName(), value(c.getEmail()), value(c.getPhoneNumber())),
                    (c, score) -> new Result("Customer", c.getCustomerId(), c.getFirstName() + " " + c.getLastName(),
                            value(c.getEmail()), link("/admin/customers", value(c.getEmail())), score)));
        if (modules.contains(PermissionModule.STYLISTS))
            out.addAll(match(stylists.findAll().stream(), words,
                    s -> fields(s.getFirstName() + " " + s.getLastName(), value(s.getEmail()), s.getSpeciality()),
                    (s, score) -> new Result("Stylist", s.getStylistId(), s.getFirstName() + " " + s.getLastName(),
                            s.getSpeciality() + (s.isActive() ? "" : " · archived"),
                            link("/admin/stylists", value(s.getEmail())), score)));
        if (modules.contains(PermissionModule.SERVICES))
            out.addAll(serviceResults(words, "/admin/services"));
        if (modules.contains(PermissionModule.APPOINTMENTS))
            out.addAll(match(appointments.findAll().stream(), words, SearchService::appointmentFields,
                    (a, score) -> appointmentResult(a, "/admin/appointments", score)));
        if (modules.contains(PermissionModule.PROMOTIONS))
            out.addAll(match(promotions.findAll().stream(), words,
                    p -> fields(p.getCode(), p.getDescription()),
                    (p, score) -> new Result("Promotion", p.getPromotionId(), p.getCode(), p.getDescription(),
                            link("/admin/promotions", p.getCode()), score)));
        if (modules.contains(PermissionModule.INVENTORY))
            out.addAll(match(products.findAll().stream(), words,
                    p -> fields(p.getName(), p.getBrand(), p.getCategory()),
                    (p, score) -> new Result("Product", p.getProductId(), p.getName(),
                            p.getBrand() + " · " + p.getStockQuantity() + " in stock",
                            link("/admin/inventory", p.getName()), score)));
        if (includeLogins)
            out.addAll(match(users.findAll().stream(), words,
                    u -> fields(u.getUsername(), u.getFirstName() + " " + u.getLastName(), value(u.getEmail())),
                    (u, score) -> new Result("Login", u.getUserId(), u.getUsername(),
                            u.getRole() == null ? "" : u.getRole().getName(), link("/admin/users", u.getUsername()), score)));
        return rank(out);
    }

    public List<Result> forStylist(String query, String stylistId) {
        List<String> words = words(query);
        if (words.isEmpty() || stylistId == null) return List.of();
        List<Result> out = new ArrayList<>();
        out.addAll(match(appointments.findByStylist_StylistId(stylistId).stream(), words,
                SearchService::appointmentFields, (a, score) -> appointmentResult(a, "/stylist/appointments", score)));
        out.addAll(serviceResults(words, "/stylist/schedule"));
        return rank(out);
    }

    // ---------------------------------------------------------------- matching

    /**
     * 0 = no match. Otherwise the sum over words of that word's best field score. fields[0] is
     * the record's primary field and counts double.
     */
    static int score(List<String> words, List<String> fields) {
        int total = 0;
        for (String w : words) {
            int best = 0;
            for (int i = 0; i < fields.size(); i++)
                best = Math.max(best, fieldScore(w, fields.get(i)) * (i == 0 ? 2 : 1));
            if (best == 0) return 0;   // every word must match somewhere
            total += best;
        }
        return total;
    }

    private static int fieldScore(String word, String field) {
        if (field == null || field.isEmpty()) return 0;
        if (field.equals(word)) return 100;
        if (field.startsWith(word)) return 60;
        for (String part : field.split("[\\s@._-]+"))
            if (part.startsWith(word)) return 40;
        return field.contains(word) ? 10 : 0;
    }

    private record Scored<T>(T row, int score) {}

    private static <T> List<Result> match(Stream<T> rows, List<String> words, Function<T, List<String>> fieldsOf,
                                          BiFunction<T, Integer, Result> toResult) {
        return rows.map(r -> new Scored<>(r, score(words, fieldsOf.apply(r))))
                .filter(s -> s.score() > 0)
                .sorted(Comparator.comparingInt((Scored<T> s) -> s.score()).reversed())
                .limit(PER_TYPE)
                .map(s -> toResult.apply(s.row(), s.score()))
                .toList();
    }

    private static List<Result> rank(List<Result> results) {
        return results.stream()
                .sorted(Comparator.comparingInt(Result::score).reversed()
                        .thenComparing(Result::title, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .limit(MAX_RESULTS)
                .toList();
    }

    // ---------------------------------------------------------------- per type

    private List<Result> serviceResults(List<String> words, String section) {
        return match(services.findAll().stream(), words,
                s -> fields(s.getName(), s.getCategory() == null ? null : s.getCategory().name()),
                (s, score) -> new Result("Service", s.getServiceId(), s.getName(),
                        "R" + s.getPrice().getValue() + " · " + s.getDurationMinutes() + " min"
                                + (s.isActive() ? "" : " · archived"), link(section, s.getName()), score));
    }

    private static List<String> appointmentFields(Appointment a) {
        return fields(
                a.getCustomer() == null ? null : a.getCustomer().getFirstName() + " " + a.getCustomer().getLastName(),
                a.getStylist() == null ? null : a.getStylist().getFirstName() + " " + a.getStylist().getLastName(),
                a.getSalonService() == null ? null : a.getSalonService().getName(),
                a.getAppointmentDate() == null ? null : a.getAppointmentDate().toString(),
                a.getStatus() == null ? null : a.getStatus().name(),
                a.getPromoCode());
    }

    private static Result appointmentResult(Appointment a, String section, int score) {
        String who = a.getCustomer() == null ? "Customer" : a.getCustomer().getFirstName() + " " + a.getCustomer().getLastName();
        String what = a.getSalonService() == null ? "" : a.getSalonService().getName();
        return new Result("Appointment", a.getAppointmentId(), who + " — " + what,
                a.getAppointmentDate() + " " + a.getStartTime() + " · " + a.getStatus(), link(section, who), score);
    }

    // ---------------------------------------------------------------- helpers

    /** Lower-cased, trimmed fields; a null stays null so the primary field stays at index 0. */
    private static List<String> fields(String... parts) {
        List<String> out = new ArrayList<>(parts.length);
        for (String p : parts) out.add(p == null ? null : p.trim().toLowerCase(Locale.ROOT));
        return out;
    }

    private static String value(Object valueObject) {
        return valueObject == null ? null : valueObject.toString();
    }

    /** The section page opens already filtered to this record, through its own search box. */
    private static String link(String section, String q) {
        return q == null || q.isBlank() ? section
                : section + "?q=" + URLEncoder.encode(q.trim(), StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Trimmed, lower-cased words; empty when the query is too short to be useful. */
    static List<String> words(String query) {
        if (query == null) return List.of();
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.length() < MIN_LENGTH) return List.of();
        return Stream.of(q.split("\\s+")).filter(w -> !w.isEmpty()).toList();
    }
}
