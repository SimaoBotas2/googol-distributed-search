package common;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
                System.out.println("[Config] Carregado config.properties com sucesso!");
            } else {
                System.err.println("[Config] config.properties nao encontrado no classpath!");
            }
        } catch (IOException e) {
            System.err.println("[Config] Erro ao ler config.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            System.out.println("[Config] AVISO: Chave '" + key + "' nao encontrada!");
        }
        return value;
    }

    public static int getInt(String key, int def) {
        try { 
            String value = props.getProperty(key);
            if (value == null) return def;
            return Integer.parseInt(value); 
        }
        catch (Exception e) { return def; }
    }
}

