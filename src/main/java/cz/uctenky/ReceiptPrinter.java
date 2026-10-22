package cz.uctenky;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    public BigDecimal vat(BigDecimal total) {
        BigDecimal rate = store.vatRate();
        return total.multiply(rate)
                .divide(BigDecimal.valueOf(100).add(rate), 2, RoundingMode.HALF_UP);
    }

    public String render(List<Item> items, int receiptNumber, LocalDateTime time) {
        StringBuilder sb = new StringBuilder();
        String line = "-".repeat(WIDTH);
        String dbl = "=".repeat(WIDTH);

        appendCentered(sb, store.name());
        if (!store.address().isEmpty()) appendCentered(sb, store.address());
        if (!store.ico().isEmpty()) appendCentered(sb, "ICO: " + store.ico());
        if (!store.dic().isEmpty()) appendCentered(sb, "DIC: " + store.dic());
        sb.append(dbl).append('\n');

        for (Item it : items) {
            for (String part : wrap(it.name())) sb.append(part).append('\n');
            String left = "  " + fmt(it.quantity()) + " x " + money(it.unitPrice());
            sb.append(twoCols(left, money(it.total()))).append('\n');
        }

        BigDecimal total = sum(items);
        sb.append(line).append('\n');
        sb.append(twoCols("CELKEM (" + store.currency() + ")", money(total))).append('\n');
        sb.append(twoCols("z toho DPH " + fmt(store.vatRate()) + " %", money(vat(total)))).append('\n');
        sb.append(dbl).append('\n');

        sb.append(twoCols("Uctenka c. " + String.format("%06d", receiptNumber), time.format(DATE))).append('\n');
        if (!store.cashier().isEmpty()) {
            for (String part : wrap("Obsluhoval: " + store.cashier())) sb.append(part).append('\n');
        }
        if (!store.footer().isEmpty()) {
            sb.append('\n');
            appendCentered(sb, store.footer());
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
        if (pad < 1) {
            return left + "\n" + " ".repeat(Math.max(0, WIDTH - right.length())) + right;
        }
        return left + " ".repeat(pad) + right;
    }

    private static void appendCentered(StringBuilder sb, String text) {
        for (String part : wrap(text)) sb.append(center(part)).append('\n');
    }

    static List<String> wrap(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            while (word.length() > WIDTH) {
                if (cur.length() > 0) {
                    lines.add(cur.toString());
                    cur.setLength(0);
                }
                lines.add(word.substring(0, WIDTH));
                word = word.substring(WIDTH);
            }
            if (cur.length() > 0 && cur.length() + 1 + word.length() > WIDTH) {
                lines.add(cur.toString());
                cur.setLength(0);
            }
            if (cur.length() > 0) cur.append(' ');
            cur.append(word);
        }
        if (cur.length() > 0 || lines.isEmpty()) lines.add(cur.toString());
        return lines;
    }
}
