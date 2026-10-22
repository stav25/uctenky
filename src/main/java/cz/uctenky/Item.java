package cz.uctenky;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Jedna polozka nakupu. */
public record Item(String name, BigDecimal quantity, BigDecimal unitPrice) {

    public Item {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nazev polozky nesmi byt prazdny.");
        }
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Mnozstvi musi byt kladne.");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Cena nesmi byt zaporna.");
        }
        name = name.trim();
    }

    public BigDecimal total() {
        return quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    /** Parsuje radek ve tvaru "nazev;mnozstvi;cena" (desetinna carka i tecka). */
    public static Item parse(String line) {
        String[] parts = line.split(";");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Ocekavan format 'nazev;mnozstvi;cena': " + line);
        }
        try {
            return new Item(parts[0], num(parts[1]), num(parts[2]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Neplatne cislo v radku: " + line, e);
        }
    }

    private static BigDecimal num(String s) {
        return new BigDecimal(s.trim().replace(',', '.'));
    }
}
