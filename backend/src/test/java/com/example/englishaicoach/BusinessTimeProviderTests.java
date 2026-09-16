package com.example.englishaicoach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.englishaicoach.common.clock.BusinessTimeProvider;
import com.example.englishaicoach.config.ClockConfiguration;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRulesException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BusinessTimeProviderTests {

    private static final Instant DATE_BOUNDARY = Instant.parse("2026-09-16T18:30:00Z");

    @Test
    void registersUtcClockAndBusinessTimeProvider() {
        new ApplicationContextRunner()
                .withUserConfiguration(ClockConfiguration.class)
                .run(context -> {
                    Clock clock = context.getBean(Clock.class);

                    assertEquals(ZoneOffset.UTC, clock.getZone());
                    assertNotNull(context.getBean(BusinessTimeProvider.class));
                });
    }

    @Test
    void returnsInstantFromInjectedFixedClock() {
        BusinessTimeProvider provider = new BusinessTimeProvider(
                Clock.fixed(DATE_BOUNDARY, ZoneOffset.UTC));

        assertEquals(DATE_BOUNDARY, provider.now());
    }

    @Test
    void derivesDifferentLocalDaysFromUserProfileTimezones() {
        BusinessTimeProvider provider = new BusinessTimeProvider(
                Clock.fixed(DATE_BOUNDARY, ZoneOffset.UTC));

        assertEquals(LocalDate.of(2026, 9, 17), provider.today("Asia/Ho_Chi_Minh"));
        assertEquals(LocalDate.of(2026, 9, 16), provider.today("America/New_York"));
    }

    @Test
    void ignoresClockZoneWhenDerivingUserLocalDay() {
        BusinessTimeProvider provider = new BusinessTimeProvider(
                Clock.fixed(DATE_BOUNDARY, ZoneId.of("Pacific/Honolulu")));

        assertEquals(LocalDate.of(2026, 9, 17), provider.today("Asia/Tokyo"));
    }

    @Test
    void rejectsInvalidUserProfileTimezone() {
        BusinessTimeProvider provider = new BusinessTimeProvider(
                Clock.fixed(DATE_BOUNDARY, ZoneOffset.UTC));

        assertThrows(ZoneRulesException.class, () -> provider.today("Invalid/Timezone"));
    }
}
