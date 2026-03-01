package me.ripka.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config {
    private Map<String, Object> data;
    private final Yaml yaml;
    private File file;

    public Config(String path) {
        this.yaml = new Yaml();
        this.file = new File(path);

        if (!file.exists()) {
            firstConfig();
            System.err.println("Конфиг создан бот остановлен. Заполните конфиг");
            System.exit(0);
        }

        try {
            InputStream inputStream = new FileInputStream(path);
            this.data = yaml.load(inputStream);
            inputStream.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void firstConfig() {

        if (!file.exists()) {
            Map<String, Object> defaultConfig = new LinkedHashMap<>();

            Map<String, Object> bot = new LinkedHashMap<>();

            bot.put("username", "bot username");
            bot.put("token", "bot token");
            bot.put("owner", "ownerId");

            defaultConfig.put("bot", bot);

            Map<String,Object> message = new LinkedHashMap<>();

            message.put("startMessage", "   Привет! \uD83D\uDC4B Я — Мэйбл \uD83D\uDCD3\n" +
                    "  \n" +
                    "    Каждый день я буду отправлять в группу \"вопрос дня\" — отвечайте, общайтесь и узнавайте друг друга лучше!\n" +
                    "    \n" +
                    "    Бот будет отправлять вопросы в то же время когда и будет прописана команда +опрос или же /question \n" +
                    "    После того как вы отправите команду бот запомнит время и ровно через 24 часа отправит вопрос дня!\n" +
                    "  \n" +
                    "    Вопросы бывают разные — серьёзные, весёлые, и конечно же связанные с Гравити Фолз \uD83C\uDF32\n" +
                    "  \n" +
                    "    Приятного общения! ✨");
            message.put("startAdminMessage", "    Это приветствуенное сообщение для адм2инов\n" +
                    "    /configReload\n" +
                    "    /add <ChatId>\n" +
                    "    /getGroup \n" +
                    "    /delGroup <ID>\n" +
                    "    /getQuestion" +
                    "    /getTime");
            message.put("firstChatMessage", "    Привет! \uD83D\uDC4B Я — Мэйбл \uD83D\uDCD3\n" +
                    "  \n" +
                    "    Каждый день я буду отправлять в группу \"вопрос дня\" — отвечайте, общайтесь и узнавайте друг друга лучше!\n" +
                    "    \n" +
                    "    Бот будет отправлять вопросы в то же время когда и будет прописана команда +опрос или же /question \n" +
                    "    После того как вы отправите команду бот запомнит время и ровно через 24 часа отправит вопрос дня!\n" +
                    "  \n" +
                    "    Вопросы бывают разные — серьёзные, весёлые, и конечно же связанные с Гравити Фолз \uD83C\uDF32\n" +
                    "  \n" +
                    "    Приятного общения! ✨");


            defaultConfig.put("message", message);


            this.data = defaultConfig;
            saveConfig();
        }
    }

    public void saveConfig() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Yaml yamlWriter = new Yaml(options);

        try (FileWriter writer = new FileWriter(file)) {
            yamlWriter.dump(data,writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения конфига: " + e);
        }
    }

    public String getString(String path) {
        String[] keys = path.split("\\.");
        Object current = data;

        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                return null;
            }
        }
        return current != null ? current.toString() : null;
    }

    public void configReload() {
        try {
            InputStream inputStream = new FileInputStream(file);
            this.data = yaml.load(inputStream);
            inputStream.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
