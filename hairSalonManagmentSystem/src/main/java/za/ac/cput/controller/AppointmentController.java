package za.ac.cput.controller;

import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Appointment;
import za.ac.cput.service.IAppointmentService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final IAppointmentService service;

    public AppointmentController(IAppointmentService service) {
        this.service = service;
    }

    public record BookAppointmentRequest(String customerId, String stylistId, String salonServiceId,
                                         LocalDate date, LocalTime startTime, String notes) {}

    @PostMapping("/book")
    public Appointment book(@RequestBody BookAppointmentRequest request) {
        return service.bookAppointment(request.customerId(), request.stylistId(), request.salonServiceId(),
                request.date(), request.startTime(), request.notes());
    }

    @GetMapping("/{id}")
    public Appointment read(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping
    public List<Appointment> getAll() {
        return service.getAll();
    }

    @GetMapping("/customer/{customerId}")
    public List<Appointment> findByCustomer(@PathVariable String customerId) {
        return service.findByCustomer(customerId);
    }

    @PostMapping("/{id}/cancel")
    public Appointment cancel(@PathVariable String id) {
        return service.cancelAppointment(id);
    }

    @PostMapping("/{id}/complete")
    public Appointment complete(@PathVariable String id) {
        return service.completeAppointment(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}

