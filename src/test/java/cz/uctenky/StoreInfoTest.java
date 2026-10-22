package cz.uctenky;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class StoreInfoTest {

    @Test
    void loadsValues() throws IOException {
        StoreInfo s = StoreInfo.load(new StringReader("name=Obchod\nico=123\nvatRate=15\n"));
        assertEquals("Obchod", s.name());
        assertEquals("123", s.ico());
        assertEquals(new BigDecimal("15"), s.vatRate());
        assertEquals("Kc", s.currency());
    }

    @Test
    void missingNameFails() {
        assertThrows(IllegalArgumentException.class, () -> StoreInfo.load(new StringReader("ico=1")));
    }

    @Test
    void badVatFails() {
        assertThrows(IllegalArgumentException.class,
                () -> StoreInfo.load(new StringReader("name=X\nvatRate=abc")));
    }
}
