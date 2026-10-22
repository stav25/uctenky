package cz.uctenky;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static final String NAME = "Uctenky";
    static final String DEFAULT_CONFIG = "obchod.properties";
    private static final AtomicInteger COUNTER = new AtomicInteger();

    public static void main(String[] args) {
        System.exit(run(args, new InputStreamReader(System.in, StandardCharsets.UTF_8),
                new PrintStream(System.out, true, StandardCharsets.UTF_8), System.err));
    }

    static int run(String[] args, Reader in, PrintStream out, PrintStream err) {
        Path config = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h", "--help" -> {
                    out.println(usage());
                    return 0;
                }
                case "-v", "--version" -> {
                    out.println(NAME + " " + version());
                    return 0;
                }
                case "-c", "--config" -> {
                    if (++i >= args.length) {
                        err.println("Chybi hodnota pro --config.");
                        return 2;
                    }
                    config = Path.of(args[i]);
                }
                default -> {
                    err.println("Neznamy argument: " + args[i]);
                    err.println(usage());
                    return 2;
                }
            }
        }

        StoreInfo store;
        try {
            store = loadStore(config);
        } catch (NoSuchFileException e) {
            err.println("Konfiguracni soubor neexistuje: " + config);
            return 1;
        } catch (IOException | IllegalArgumentException e) {
            err.println("Nelze nacist konfiguraci: " + e.getMessage());
            return 1;
        }

        List<Item> items;
        try {
            items = readItems(in, out);
        } catch (IOException e) {
            err.println("Chyba cteni vstupu: " + e.getMessage());
            return 1;
        }
        if (items.isEmpty()) {
            err.println("Nebyla zadana zadna polozka, uctenka se netiskne.");
            return 1;
        }
        out.println();
        out.print(new ReceiptPrinter(store).render(items, COUNTER.incrementAndGet(), LocalDateTime.now()));
        return 0;
    }

    static StoreInfo loadStore(Path explicit) throws IOException {
        if (explicit != null) {
            return StoreInfo.load(explicit);
        }
        Path local = Path.of(DEFAULT_CONFIG);
        if (Files.exists(local)) {
            return StoreInfo.load(local);
        }
        try (InputStream in = Main.class.getResourceAsStream("/" + DEFAULT_CONFIG)) {
            if (in == null) {
                throw new NoSuchFileException(DEFAULT_CONFIG);
            }
            return StoreInfo.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    static List<Item> readItems(Reader in, PrintStream out) throws IOException {
        BufferedReader br = new BufferedReader(in);
        List<Item> items = new ArrayList<>();
        out.println("Zadavejte polozky ve tvaru: nazev;mnozstvi;cena  (prazdny radek = konec)");
        String line;
        while ((line = br.readLine()) != null && !line.isBlank()) {
            try {
                items.add(Item.parse(line));
            } catch (IllegalArgumentException e) {
                out.println("Chyba: " + e.getMessage() + " - zkuste znovu.");
            }
        }
        return items;
    }

    static String version() {
        String v = Main.class.getPackage().getImplementationVersion();
        return v != null ? v : "dev";
    }

    static String usage() {
        return """
                Pouziti: java -jar uctenky.jar [-c obchod.properties] [-h] [-v]
                  -c, --config  soubor s udaji obchodu (vychozi: obchod.properties v aktualni slozce, jinak vestavena kopie)
                  -h, --help    napoveda
                  -v, --version verze programu
                Polozky se zadavaji ze standardniho vstupu, jedna na radek: nazev;mnozstvi;cena""";
    }
}
