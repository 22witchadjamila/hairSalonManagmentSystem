package za.ac.cput.controller;
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

    @PostMapping
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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
