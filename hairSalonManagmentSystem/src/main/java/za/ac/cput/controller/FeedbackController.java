package za.ac.cput.controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Feedback;
import za.ac.cput.service.IFeedbackService;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final IFeedbackService service;

    public FeedbackController(IFeedbackService service) {
        this.service = service;
    }

    public record FeedbackRequest(String appointmentId, String customerId, int rating, String comment) {}

    /** What the public home page may show about a review - first name and initial, no contact details. */
    public record PublicReview(String reviewId, int rating, String comment,
                               java.time.LocalDateTime submittedAt, String reviewerName) {
        static PublicReview of(Feedback f) {
            String first = f.getCustomer() != null ? f.getCustomer().getFirstName() : "Client";
            String last = f.getCustomer() != null ? f.getCustomer().getLastName() : null;
            String name = (last == null || last.isBlank()) ? first : first + " " + last.charAt(0) + ".";
            return new PublicReview(f.getReviewId(), f.getRating(), f.getComment(), f.getSubmittedAt(), name);
        }
    }

    /**
     * Public. Verified reviews that say something. This replaces calling GET /api/feedback from
     * the home page, which returned every review with the reviewer's full customer record -
     * email address and phone number included - to anyone.
     */
    @GetMapping("/public")
    public java.util.List<PublicReview> publicReviews() {
        return service.getAll().stream()
                .filter(f -> f.isVerified() && f.getComment() != null && !f.getComment().isBlank())
                .map(PublicReview::of)
                .toList();
    }

    /** A customer reviews only their OWN appointment, as themselves. */
    @PostMapping
    @PreAuthorize("@perm.granted() or (@access.isCustomer(#request.customerId()) and @access.isAppointmentCustomer(#request.appointmentId()))")
    public Feedback create(@RequestBody FeedbackRequest request) {
        return service.submitFeedback(request.appointmentId(), request.customerId(),
                request.rating(), request.comment());
    }

    @GetMapping("/{id}")
    public Feedback read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<Feedback> getAll() {
        return service.getAll();
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@perm.granted() or @access.isCustomer(#customerId)")
    public List<Feedback> findByCustomer(@PathVariable String customerId) {
        return service.findByCustomer(customerId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
