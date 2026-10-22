package cz.uctenky;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record StoreInfo(String name, String address, String ico, String dic,
                        String cashier, String footer, String currency, BigDecimal vatRate) {

    public static StoreInfo load(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return load(reader);
        }
    }

    public static StoreInfo load(Reader reader) throws IOException {
        Properties p = new Properties();
        p.load(reader);
        String name = p.getProperty("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("V konfiguraci chybi povinny udaj 'name'.");
        }
        BigDecimal vat;
        try {
            vat = new BigDecimal(p.getProperty("vatRate", "21").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Neplatna hodnota 'vatRate'.", e);
        }
        return new StoreInfo(name.trim(),
                p.getProperty("address", "").trim(),
                p.getProperty("ico", "").trim(),
                p.getProperty("dic", "").trim(),
                p.getProperty("cashier", "").trim(),
                p.getProperty("footer", "").trim(),
                p.getProperty("currency", "Kc").trim(),
                vat);
    }
}
