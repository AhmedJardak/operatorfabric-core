package org.lfenergy.operatorfabric.cards.publication.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurerComposite;

/**
 * Configures webflux
 *
 * @author David Binder
 */
@Slf4j
@Configuration
public class WebFluxConfig {
  /**
   * Configures CORS
   * @return
   */
  @Bean
  public WebFluxConfigurer corsConfigurer() {
    return new WebFluxConfigurerComposite() {

      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/cards/**").allowedOrigins("*")
           .allowedMethods("*");
      }
    };
  }
}
