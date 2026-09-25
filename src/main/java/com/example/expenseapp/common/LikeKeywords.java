package com.example.expenseapp.common;

import org.springframework.util.StringUtils;

public final class LikeKeywords {

    private LikeKeywords() {
    }

    /**
     * MyBatis 側で {@code ESCAPE '|'} と組み合わせて使う。
     */
    public static String escape(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return keyword;
        }
        return keyword.replace("|", "||").replace("%", "|%").replace("_", "|_");
    }
}
