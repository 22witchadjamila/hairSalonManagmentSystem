package za.ac.cput.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.Stylist;
import za.ac.cput.service.IStylistService;
import za.ac.cput.service.RetirementService;
import za.ac.cput.service.AuthService;
import za.ac.cput.domain.valueobject.Email;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StylistController Tests")
class StylistControllerTest {

    @Mock
    private IStylistService service;

    @Mock
    private RetirementService retirementService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private StylistController controller;

    @Test
    @DisplayName("create() delegates to service.register()")
    void create_delegatesToService() {
        Stylist stylist = mock(Stylist.class);
        when(service.register("Lebo", "Mokoena", "lebo@salon.com",
                "0711234567", "Colouring")).thenReturn(stylist);

        StylistController.StylistRequest request =
                new StylistController.StylistRequest("Lebo", "Mokoena",
                        "lebo@salon.com", "0711234567", "Colouring");

        assertEquals(stylist, controller.create(request));
    }

    @Test
    @DisplayName("read() delegates to service.read()")
    void read_delegatesToService() {
        Stylist stylist = mock(Stylist.class);
        when(service.read("s1")).thenReturn(stylist);

        assertEquals(stylist, controller.read("s1"));
    }

    @Test
    @DisplayName("getAll() delegates to service.getAll()")
    void getAll_delegatesToService() {
        List<Stylist> stylists = List.of(mock(Stylist.class));
        when(service.getAll()).thenReturn(stylists);

        assertEquals(stylists, controller.getAll());
    }

    @Test
    @DisplayName("findActive() is public, so it returns names and speciality only - never email or phone")
    void findActive_returnsPublicProjection() {
        Stylist thandi = new Stylist.Builder().setStylistId("s1").setFirstName("Thandi").setLastName("M")
                .setEmail(Email.of("thandi@judithhairsalon.co.za")).setSpeciality("Colour").setActive(true).build();
        when(service.findActiveStylists()).thenReturn(List.of(thandi));

        List<StylistController.PublicStylist> result = controller.findActive();

        assertEquals(List.of(new StylistController.PublicStylist("s1", "Thandi", "M", "Colour")), result);
    }

    @Test
    @DisplayName("delete() archives-or-deletes via RetirementService")
    void delete_usesRetirementStrategy() {
        RetirementService.Result deleted = new RetirementService.Result(RetirementService.Outcome.DELETED, "deleted");
        when(retirementService.retireStylist("s1")).thenReturn(deleted);

        assertEquals(deleted, controller.delete("s1"));
        verify(service, never()).delete(any());
    }
}

