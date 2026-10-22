package cz.uctenky;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ItemTest {

    @Test
    void parsesLineWithCommaDecimals() {
        Item it = Item.parse("Rohlik;3;2,50");
        assertEquals("Rohlik", it.name());
        assertEquals(new BigDecimal("7.50"), it.total());
    }

    @Test
    void totalIsRoundedToTwoDecimals() {
        Item it = new Item("Syr", new BigDecimal("0.333"), new BigDecimal("100"));
        assertEquals(new BigDecimal("33.30"), it.total());
    }

    @Test
    void rejectsBadFormat() {
        assertThrows(IllegalArgumentException.class, () -> Item.parse("jen nazev"));
        assertThrows(IllegalArgumentException.class, () -> Item.parse("a;b;c"));
    }

    @Test
    void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Item.parse(" ;1;1"));
        assertThrows(IllegalArgumentException.class, () -> Item.parse("x;0;1"));
        assertThrows(IllegalArgumentException.class, () -> Item.parse("x;1;-1"));
    }

    @Test
    void rejectsItemRoundingToZero() {
        assertThrows(IllegalArgumentException.class, () -> Item.parse("x;0,001;1"));
        assertEquals(new BigDecimal("0.00"), Item.parse("zdarma;1;0").total());
    }
}
