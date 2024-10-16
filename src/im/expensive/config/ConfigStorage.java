package im.expensive.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigStorage {
    public final Logger logger = Logger.getLogger(ConfigStorage.class.getName());

    // Исправлен путь к директории конфигураций
    public final File CONFIG_DIR = new File(Minecraft.getInstance().gameDir, "expensive/configs");
    public final File AUTOCFG_DIR = new File(CONFIG_DIR, "autocfg.cfg");

    public final JsonParser jsonParser = new JsonParser();

    public void init() throws IOException {
        setupFolder();
    }

    public void setupFolder() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
            logger.log(Level.SEVERE, "Created configuration directory: " + CONFIG_DIR.getPath());
        }

        if (!AUTOCFG_DIR.exists()) {
            try {
                logger.log(Level.SEVERE, "Creating system configuration...");
                AUTOCFG_DIR.createNewFile();
                logger.log(Level.SEVERE, "Created system configuration: " + AUTOCFG_DIR.getPath());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create system configuration file", e);
            }
        } else {
            loadConfiguration("system");
            logger.log(Level.SEVERE, "Loaded system configuration: " + AUTOCFG_DIR.getPath());
        }
    }

    public boolean isEmpty() {
        List<Config> configs = getConfigs();
        logger.log(Level.INFO, "Found " + configs.size() + " configurations.");
        return configs.isEmpty();
    }

    public List<Config> getConfigs() {
        List<Config> configs = new ArrayList<>();
        File[] configFiles = CONFIG_DIR.listFiles();

        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".cfg")) {
                    String configName = configFile.getName().replace(".cfg", "");
                    Config config = findConfig(configName);
                    if (config != null) {
                        configs.add(config);
                    }
                }
            }
        }

        return configs;
    }

    public void loadConfiguration(String configuration) {
        Config config = findConfig(configuration);
        if (config == null) {
            logger.log(Level.WARNING, "Configuration not found: " + configuration);
            return;  // Прекращаем выполнение, если конфигурация не найдена
        }

        try (FileReader reader = new FileReader(config.getFile())) {
            JsonObject object = (JsonObject) jsonParser.parse(reader);
            config.loadConfig(object);
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Configuration file not found: " + config.getFile().getPath(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO Exception while loading configuration: " + config.getFile().getPath(), e);
        } catch (NullPointerException e) {
            logger.log(Level.SEVERE, "Null pointer exception in Config!", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading configuration!", e);
        }
    }

    public void saveConfiguration(String configuration) {
        Config config = findConfig(configuration);
        if (config == null) {
            logger.log(Level.WARNING, "Configuration not found for saving: " + configuration);
            return;
        }

        String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(config.saveConfig());
        try (FileWriter writer = new FileWriter(config.getFile())) {
            writer.write(contentPrettyPrint);
            logger.log(Level.INFO, "Saved configuration to file: " + config.getFile().getPath());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to save configuration to file: " + config.getFile().getPath(), e);
        } catch (NullPointerException e) {
            logger.log(Level.SEVERE, "Null pointer exception while saving configuration!", e);
        }
    }

    public Config findConfig(String configName) {
        if (configName == null) return null;
        File configFile = new File(CONFIG_DIR, configName + ".cfg");
        if (configFile.exists()) {
            return new Config(configName);
        }
        return null;
    }
}
