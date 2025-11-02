package common;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = ClassLoader.getSystemResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                System.err.println("[Config] config.properties nao encontrado no classpath!");
            }
        } catch (IOException e) {
            System.err.println("[Config] Erro ao ler config.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static int getInt(String key, int def) {
        try { return Integer.parseInt(props.getProperty(key)); }
        catch (Exception e) { return def; }
    }
}
