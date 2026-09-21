package topicmanagement.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
@ComponentScan("topicmanagement")
@Import({JpaConfig.class, SecurityConfig.class})
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer c) {
    c.enable();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry r) {
    r.addResourceHandler("/assets/**").addResourceLocations("/assets/");
  }
}
