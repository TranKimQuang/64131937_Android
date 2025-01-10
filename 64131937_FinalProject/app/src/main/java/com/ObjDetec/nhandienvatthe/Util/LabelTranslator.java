package com.ObjDetec.nhandienvatthe.Util;

import java.util.HashMap;
import java.util.Map;

public class LabelTranslator {

    private static final Map<String, String> LABEL_MAP = new HashMap<>();

    static {
        LABEL_MAP.put("Food", "Thực phẩm");
        LABEL_MAP.put("Home good", "Đồ gia dụng");
        LABEL_MAP.put("Fashion good", "Đồ thời trang");
    }

    public static String translateLabel(String label) {
        return LABEL_MAP.getOrDefault(label, "Vật thể lạ");
    }
}