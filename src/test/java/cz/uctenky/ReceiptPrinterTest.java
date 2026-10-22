package cz.uctenky;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReceiptPrinterTest {

    private final StoreInfo store = new StoreInfo("Obchod", "Ulice 1", "123", "CZ123",
            "Jana", "Diky!", "Kc", new BigDecimal("21"));
    private final ReceiptPrinter printer = new ReceiptPrinter(store);

    @Test
    void sumAddsAllItems() {
        List<Item> items = List.of(Item.parse("a;2;10"), Item.parse("b;1;5,5"));
        assertEquals(new BigDecimal("25.50"), ReceiptPrinter.sum(items));
    }

    @Test
    void vatIsExtractedFromGrossPrice() {
        assertEquals(new BigDecimal("21.00"), printer.vat(new BigDecimal("121.00")));
    }

    @Test
    void rendersAllSections() {
        String r = printer.render(List.of(Item.parse("Mleko;2;20")), 7,
                LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        assertTrue(r.contains("Obchod"));
        assertTrue(r.contains("ICO: 123"));
        assertTrue(r.contains("Mleko"));
        assertTrue(r.contains("40.00"));
        assertTrue(r.contains("000007"));
        assertTrue(r.contains("02.01.2026 03:04:05"));
        assertTrue(r.contains("Obsluhoval: Jana"));
        assertTrue(r.contains("Diky!"));
    }

    @Test
    void linesFitReceiptWidth() {
        String r = printer.render(List.of(Item.parse("Mleko;2;20")), 1, LocalDateTime.now());
        r.lines().forEach(l -> assertTrue(l.length() <= ReceiptPrinter.WIDTH, l));
    }

    @Test
    void formatsQuantities() {
        assertEquals("2", ReceiptPrinter.fmt(new BigDecimal("2.00")));
        assertEquals("0.5", ReceiptPrinter.fmt(new BigDecimal("0.50")));
        assertEquals("10", ReceiptPrinter.fmt(new BigDecimal("1E+1")));
    }
}
