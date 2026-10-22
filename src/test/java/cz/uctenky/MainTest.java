package cz.uctenky;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    private final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);
    private final PrintStream err = new PrintStream(errBytes, true, StandardCharsets.UTF_8);

    private int run(String input, String... args) {
        return Main.run(args, new StringReader(input), out, err);
    }

    @Test
    void printsReceipt(@TempDir Path dir) throws Exception {
        Path cfg = dir.resolve("o.properties");
        Files.writeString(cfg, "name=Test Shop\n");
        int code = run("Chleb;1;30\nspatne\nMleko;2;20\n\n", "-c", cfg.toString());
        assertEquals(0, code);
        String o = outBytes.toString(StandardCharsets.UTF_8);
        assertTrue(o.contains("Test Shop"));
        assertTrue(o.contains("70.00"));
        assertTrue(o.contains("Chyba"));
    }

    @Test
    void emptyInputFails(@TempDir Path dir) throws Exception {
        Path cfg = dir.resolve("o.properties");
        Files.writeString(cfg, "name=Test Shop\n");
        assertEquals(1, run("\n", "-c", cfg.toString()));
    }

    @Test
    void missingConfigFails() {
        assertEquals(1, run("", "-c", "/neexistuje.properties"));
    }

    @Test
    void versionHelpAndUnknownArg() {
        assertEquals(0, run("", "--version"));
        assertTrue(outBytes.toString(StandardCharsets.UTF_8).startsWith("Uctenky"));
        assertEquals(0, run("", "--help"));
        assertEquals(2, run("", "--nesmysl"));
        assertEquals(2, run("", "-c"));
    }

    @Test
    void missingConfigMessageNamesFile() {
        run("", "-c", "/neexistuje.properties");
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains("neexistuje.properties"));
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains("neexistuje:"));
    }

    @Test
    void loadsDefaultConfig() throws Exception {
        StoreInfo s = Main.loadStore(null);
        assertNotNull(s.name());
    }
}
