package me.kimsuhwan.springbootdeveloper.config;

public enum SortOrder {
    LIKES, VIEWS, LATEST;

    public  static SortOrder fromString(String order) {
        try {
            return SortOrder.valueOf(order.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return SortOrder.LATEST;
        }
    }
}
