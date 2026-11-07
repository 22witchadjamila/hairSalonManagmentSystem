package za.ac.cput.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.domain.Customer;
import za.ac.cput.domain.enums.PermissionModule;
import za.ac.cput.domain.valueobject.Email;
import za.ac.cput.repository.*;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchService - matching, ranking and scope")
class SearchServiceTest {

    @Mock CustomerRepository customers;
    @Mock StylistRepository stylists;
    @Mock SalonServiceRepository services;
    @Mock AppointmentRepository appointments;
    @Mock PromotionRepository promotions;
    @Mock ProductInventoryRepository products;
    @Mock UserRepository users;
    @InjectMocks SearchService search;

    private static Customer customer(String id, String first, String last, String email) {
        return new Customer.Builder().setCustomerId(id).setFirstName(first).setLastName(last)
                .setEmail(Email.of(email)).build();
    }

    private List<SearchService.Result> customersOnly(String q) {
        return search.forStaff(q, EnumSet.of(PermissionModule.CUSTOMERS), false);
    }

    @Test
    @DisplayName("trims, ignores case, and matches part of a word")
    void partialCaseInsensitiveTrimmed() {
        when(customers.findAll()).thenReturn(List.of(customer("c1", "Jane", "Doe", "jane@x.com")));
        assertEquals(1, customersOnly("   JAN  ").size());
        assertEquals(1, customersOnly("oe").size());
    }

    @Test
    @DisplayName("every word must match, in any order")
    void allWordsAnyOrder() {
        when(customers.findAll()).thenReturn(List.of(
                customer("c1", "Jane", "Doe", "jane@x.com"),
                customer("c2", "Jane", "Smith", "js@x.com")));
        assertEquals(List.of("c1"), customersOnly("doe jane").stream().map(SearchService.Result::id).toList());
    }

    @Test
    @DisplayName("ranks an exact or starting match above a match in the middle")
    void ranksBestFirst() {
        when(customers.findAll()).thenReturn(List.of(
                customer("c1", "Rosanna", "Ann", "r@x.com"),     // 'ann' in the middle of the name
                customer("c2", "Ann", "Lee", "ann@x.com")));    // name starts with 'ann'
        assertEquals("c2", customersOnly("ann").get(0).id());
    }

    @Test
    @DisplayName("too-short or blank queries return nothing instead of everything")
    void shortQueriesReturnNothing() {
        assertTrue(customersOnly("").isEmpty());
        assertTrue(customersOnly("   ").isEmpty());
        assertTrue(customersOnly("a").isEmpty());
        assertTrue(customersOnly(null).isEmpty());
        verifyNoInteractions(customers);
    }

    @Test
    @DisplayName("only searches the modules it is given - a role without CUSTOMERS never sees customers")
    void respectsScope() {
        List<SearchService.Result> r = search.forStaff("jane", EnumSet.of(PermissionModule.PROMOTIONS), false);
        assertTrue(r.isEmpty());
        verifyNoInteractions(customers, users);
    }

    @Test
    @DisplayName("links open the section already filtered (?q=)")
    void linksCarryQuery() {
        when(customers.findAll()).thenReturn(List.of(customer("c1", "Jane", "Doe", "jane@x.com")));
        assertEquals("/admin/customers?q=jane%40x.com", customersOnly("jane").get(0).link());
    }
}
