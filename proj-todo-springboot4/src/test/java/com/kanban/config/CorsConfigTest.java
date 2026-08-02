package com.kanban.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

class CorsConfigTest {

    @Test
    void corsConfigurer_returnsConfigurer() {
        CorsConfig config = new CorsConfig();
        WebMvcConfigurer webMvcConfigurer = config.corsConfigurer();

        assertThat(webMvcConfigurer).isNotNull();
    }
}
