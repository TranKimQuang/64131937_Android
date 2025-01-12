package com.ObjDetec.nhandienvatthe.Util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LabelTranslator {

    private static final Map<String, Map<String, String>> LABEL_MAP = new HashMap<>();
    private static final Map<String, Map<String, String>> SYSTEM_MESSAGES = new HashMap<>();

    static {
        // Tiếng Việt
        Map<String, String> viLabels = new HashMap<>();
        viLabels.put("Food", "Thực phẩm");
        viLabels.put("Home good", "Đồ gia dụng");
        viLabels.put("Fashion good", "Đồ thời trang");
        LABEL_MAP.put("vi", viLabels);

        Map<String, String> viSystemMessages = new HashMap<>();
        viSystemMessages.put("No QR Code detected, continuing to scan...", "Không tìm thấy QR Code, tiếp tục quét...");
        viSystemMessages.put("QR Code detected", "Đã phát hiện QR Code");
        SYSTEM_MESSAGES.put("vi", viSystemMessages);

        // Tiếng Anh
        Map<String, String> enLabels = new HashMap<>();
        enLabels.put("Food", "Food");
        enLabels.put("Home good", "Home good");
        enLabels.put("Fashion good", "Fashion good");
        LABEL_MAP.put("en", enLabels);

        Map<String, String> enSystemMessages = new HashMap<>();
        enSystemMessages.put("No QR Code detected, continuing to scan...", "No QR Code detected, continuing to scan...");
        enSystemMessages.put("QR Code detected", "QR Code detected");
        SYSTEM_MESSAGES.put("en", enSystemMessages);

        // Tiếng Pháp
        Map<String, String> frLabels = new HashMap<>();
        frLabels.put("Food", "Nourriture");
        frLabels.put("Home good", "Article ménager");
        frLabels.put("Fashion good", "Article de mode");
        LABEL_MAP.put("fr", frLabels);

        Map<String, String> frSystemMessages = new HashMap<>();
        frSystemMessages.put("No QR Code detected, continuing to scan...", "Aucun QR Code détecté, continuation du scan...");
        frSystemMessages.put("QR Code detected", "QR Code détecté");
        SYSTEM_MESSAGES.put("fr", frSystemMessages);

        // Tiếng Nga
        Map<String, String> ruLabels = new HashMap<>();
        ruLabels.put("Food", "Еда");
        ruLabels.put("Home good", "Домашний товар");
        ruLabels.put("Fashion good", "Модный товар");
        LABEL_MAP.put("ru", ruLabels);

        Map<String, String> ruSystemMessages = new HashMap<>();
        ruSystemMessages.put("No QR Code detected, continuing to scan...", "QR-код не обнаружен, продолжаю сканирование...");
        ruSystemMessages.put("QR Code detected", "QR-код обнаружен");
        SYSTEM_MESSAGES.put("ru", ruSystemMessages);
    }

    private static String currentLanguage = "vi"; // Mặc định là tiếng Việt

    public static void setLanguage(String languageCode) {
        currentLanguage = languageCode;
    }

    public static String translateLabel(String label) {
        Map<String, String> languageLabels = LABEL_MAP.get(currentLanguage);
        if (languageLabels != null) {
            return languageLabels.getOrDefault(label, "Vật thể lạ");
        }
        return label; // Trả về label gốc nếu không tìm thấy ngôn ngữ
    }

    public static String translateSystemMessage(String message) {
        Map<String, String> systemMessages = SYSTEM_MESSAGES.get(currentLanguage);
        if (systemMessages != null) {
            return systemMessages.getOrDefault(message, message); // Trả về bản dịch hoặc message gốc nếu không tìm thấy
        }
        return message; // Trả về message gốc nếu không tìm thấy ngôn ngữ
    }
}