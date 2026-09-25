package com.example.expenseapp.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeKeywordsTest {

    @Test
    void escapesLikeWildcards() {
        assertThat(LikeKeywords.escape("100%_off|sale")).isEqualTo("100|%|_off||sale");
    }

    @Test
    void keepsBlankAsIs() {
        assertThat(LikeKeywords.escape(null)).isNull();
        assertThat(LikeKeywords.escape("")).isEmpty();
    }
}
