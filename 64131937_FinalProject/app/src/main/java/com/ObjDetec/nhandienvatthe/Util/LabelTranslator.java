package com.ObjDetec.nhandienvatthe.Util;

public class LabelTranslator {

    public static String translateLabel(String label) {
        switch (label) {
            case "Food":
                return "Thực phẩm";
            case "Home good":
                return "Đồ gia dụng";
            case "Fashion good":
                return "Đồ thời trang";
            default:
                return "Vật thể lạ";
        }
    }
}