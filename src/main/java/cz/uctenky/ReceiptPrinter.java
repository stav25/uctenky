package cz.uctenky;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Sestavi text uctenky. */
public class ReceiptPrinter {

    static final int WIDTH = 40;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final StoreInfo store;

    public ReceiptPrinter(StoreInfo store) {
        this.store = store;
    }

    public static BigDecimal sum(List<Item> items) {
        return items.stream().map(Item::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** DPH obsazene v cene s DPH. */
    public BigDecimal vat(BigDecimal total) {
        BigDecimal rate = store.vatRate();
        return total.multiply(rate)
                .divide(BigDecimal.valueOf(100).add(rate), 2, RoundingMode.HALF_UP);
    }

    public String render(List<Item> items, int receiptNumber, LocalDateTime time) {
        StringBuilder sb = new StringBuilder();
        String line = "-".repeat(WIDTH);
        String dbl = "=".repeat(WIDTH);

        sb.append(center(store.name())).append('\n');
        if (!store.address().isEmpty()) sb.append(center(store.address())).append('\n');
        if (!store.ico().isEmpty()) sb.append(center("ICO: " + store.ico())).append('\n');
        if (!store.dic().isEmpty()) sb.append(center("DIC: " + store.dic())).append('\n');
        sb.append(dbl).append('\n');

        for (Item it : items) {
            sb.append(it.name()).append('\n');
            String left = "  " + fmt(it.quantity()) + " x " + money(it.unitPrice());
            sb.append(twoCols(left, money(it.total()))).append('\n');
        }

        BigDecimal total = sum(items);
        sb.append(line).append('\n');
        sb.append(twoCols("CELKEM (" + store.currency() + ")", money(total))).append('\n');
        sb.append(twoCols("z toho DPH " + fmt(store.vatRate()) + " %", money(vat(total)))).append('\n');
        sb.append(dbl).append('\n');

        sb.append(twoCols("Uctenka c. " + String.format("%06d", receiptNumber), time.format(DATE))).append('\n');
        if (!store.cashier().isEmpty()) sb.append("Obsluhoval: ").append(store.cashier()).append('\n');
        if (!store.footer().isEmpty()) {
            sb.append('\n').append(center(store.footer())).append('\n');
        }
        return sb.toString();
    }

    static String money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    static String fmt(BigDecimal v) {
        BigDecimal s = v.stripTrailingZeros();
        return (s.scale() < 0 ? s.setScale(0) : s).toPlainString();
    }

    static String center(String s) {
        if (s.length() >= WIDTH) return s;
        return " ".repeat((WIDTH - s.length()) / 2) + s;
    }

    static String twoCols(String left, String right) {
        int pad = WIDTH - left.length() - right.length();
        return left + " ".repeat(Math.max(1, pad)) + right;
    }
}
