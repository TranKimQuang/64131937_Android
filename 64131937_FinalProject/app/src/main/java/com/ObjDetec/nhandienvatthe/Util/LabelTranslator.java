package com.ObjDetec.nhandienvatthe.Util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LabelTranslator {

    private static final Map<String, Map<String, String>> LABEL_MAP = new HashMap<>();

    static {
        // Tiếng Việt
        Map<String, String> viLabels = new HashMap<>();
        viLabels.put("Food", "Thực phẩm");
        viLabels.put("Home good", "Đồ gia dụng");
        viLabels.put("Fashion good", "Đồ thời trang");
        LABEL_MAP.put("vi", viLabels);

        // Tiếng Anh
        Map<String, String> enLabels = new HashMap<>();
        enLabels.put("Food", "Food");
        enLabels.put("Home good", "Home good");
        enLabels.put("Fashion good", "Fashion good");

        LABEL_MAP.put("en", enLabels);

        // Tiếng Pháp
        Map<String, String> frLabels = new HashMap<>();
        frLabels.put("Food", "Nourriture");
        frLabels.put("Home good", "Article ménager");
        frLabels.put("Fashion good", "Article de mode");
        LABEL_MAP.put("fr", frLabels);

        // Tiếng Nga
        Map<String, String> ruLabels = new HashMap<>();
        ruLabels.put("Food", "Еда");
        ruLabels.put("Home good", "Домашний товар");
        ruLabels.put("Fashion good", "Модный товар");
        LABEL_MAP.put("ru", ruLabels);
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
}