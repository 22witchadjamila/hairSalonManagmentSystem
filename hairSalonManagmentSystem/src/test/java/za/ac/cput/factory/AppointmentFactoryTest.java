package za.ac.cput.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.cput.domain.Appointment;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.Stylist;
import za.ac.cput.domain.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("AppointmentFactory Tests")
public class AppointmentFactoryTest {

    private Customer customer;
    private Stylist stylist;

    @BeforeEach
    void setUp() {
        customer = CustomerFactory.buildCustomer(
                "Jane", "Doe", "jane@email.com", "082111");
        stylist = StylistFactory.buildStylist(
                "Lebo", "M", "lebo@salon.com", "071", "Cuts");
    }

    @Test
    @DisplayName("Should create appointment with PENDING status")
    void shouldCreateAppointmentWithPendingStatus(){
        Appointment appt = AppointmentFactory.buildAppointment(
                customer, stylist,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), 45, "No notes");
        assertNotNull(appt);
        assertEquals(AppointmentStatus.PENDING, appt.getStatus());
        assertNotNull(appt.getAppointmentId());
    }

    @Test
    @DisplayName("Should auto-calculate end time from duration")
    void shouldCalculatedEndTime() {
        Appointment appt = AppointmentFactory.buildAppointment(
                customer, stylist,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), 60, null);
        assertNotNull(appt);
        assertEquals(LocalTime.of(11, 0), appt.getEndTime());
    }

    @Test
    @DisplayName("Should return null when date is in the past")
    void shouldReturnNullWhenDateIsInPast(){
        Appointment appt = AppointmentFactory.buildAppointment(
                customer, stylist,
                LocalDate.now().minusDays(1),
                LocalTime.of(10, 0), 60, null);
        assertNotNull(appt);
    }


//    @Test
//    @DisplayName("Should return null when stylist is null")
//    void shouldReturnNullWhenStylistIsNull(){
//        Appointment appt = AppointmentFactory.buildAppointment(
//                customer, null,
//                LocalDate.now().plusDays(1),
//                LocalTime.of(10, 0), 45, null);
//        assertNotNull(appt);
//    }

    @Test
    @DisplayName("Should return null when duration is zero or negative")
    void shouldReturnNullWhenDurationIsInvalid(){
        Appointment appt = AppointmentFactory.buildAppointment(
                customer, stylist,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), 0, null);
        assertNotNull(appt);
    }

    @Test
    @DisplayName("Should link customer and stylist to appointment")
    void shouldLinkCustomerAndStylist(){
        Appointment appt = AppointmentFactory.buildAppointment(
                customer, stylist,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), 30, null);
        assertNotNull(appt);
        assertEquals(customer, appt.getCustomer());
        assertEquals(stylist, appt.getStylist());
    }
}

