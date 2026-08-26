package za.ac.cput.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.SalonService;
import za.ac.cput.domain.Stylist;
import za.ac.cput.domain.enums.AppointmentStatus;
import za.ac.cput.domain.enums.ServiceCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("AppointmentFactory Tests")
public class AppointmentFactoryTest {

    private Customer customer;
    private Stylist stylist;
    private SalonService salonService;

    @BeforeEach
    void setUp() {
        customer = CustomerFactory.buildCustomer(
                "Jane", "Doe", "jane@email.com", "0821234567");
        stylist = StylistFactory.buildStylist(
                "Lebo", "M", "lebo@salon.com", "0711234567", "Cuts");
        salonService = SalonServiceFactory.buildService(
                "Blowout", "Full blowout and style",
                60, new BigDecimal("200.00"), ServiceCategory.STYLING);
    }

    @Test
    @DisplayName("Should create appointment with PENDING status")
    void shouldCreateAppointmentWithPendingStatus(){
        Appointment appt = AppointmentFactory.buildAppointment(
                customer, stylist, salonService,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), "No notes");
        assertNotNull(appt);
        assertEquals(AppointmentStatus.PENDING, appt.getStatus());
        assertNotNull(appt.getAppointmentId());
    }

    @Test
    @DisplayName("Should derive end time from the salon service's duration")
    void shouldCalculatedEndTime() {
        Appointment appt = AppointmentFactory.buildAppointment(
                customer, stylist, salonService,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), null);
        assertNotNull(appt);
        assertEquals(LocalTime.of(11, 0), appt.getEndTime());
    }

    @Test
    @DisplayName("Should return null when date is in the past")
    void shouldReturnNullWhenDateIsInPast(){
        Appointment appt = AppointmentFactory.buildAppointment(
                customer, stylist, salonService,
                LocalDate.now().minusDays(1),
                LocalTime.of(10, 0), null);
        assertNull(appt);
    }

    @Test
    @DisplayName("Should return null when salon service is null")
    void shouldReturnNullWhenSalonServiceIsNull(){
        Appointment appt = AppointmentFactory.buildAppointment(
                customer, stylist, null,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), null);
        assertNull(appt);
    }

    @Test
    @DisplayName("Should link customer, stylist and salon service to appointment")
    void shouldLinkCustomerAndStylist(){
        Appointment appt = AppointmentFactory.buildAppointment(
                customer, stylist, salonService,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), null);
        assertNotNull(appt);
        assertEquals(customer, appt.getCustomer());
        assertEquals(stylist, appt.getStylist());
        assertEquals(salonService, appt.getSalonService());
    }
}
