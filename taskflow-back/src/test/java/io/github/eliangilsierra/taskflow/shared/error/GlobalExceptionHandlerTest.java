package io.github.eliangilsierra.taskflow.shared.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
  }

  @Test
  void mapsNotFoundToProblemDetail() throws Exception {
    mockMvc
        .perform(get("/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Resource not found"))
        .andExpect(jsonPath("$.detail").value("missing"));
  }

  @Test
  void mapsConflictAndUnauthorized() throws Exception {
    mockMvc.perform(get("/conflict")).andExpect(status().isConflict());
    mockMvc.perform(get("/unauthorized")).andExpect(status().isUnauthorized());
  }

  @Test
  void doesNotLeakUnexpectedErrorDetails() throws Exception {
    mockMvc
        .perform(get("/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(
            jsonPath("$.detail").value("An unexpected error occurred. Please try again later."));
  }

  @Test
  void reportsFieldViolationsOnValidationFailure() throws Exception {
    mockMvc
        .perform(
            post("/validate").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].field").value("name"));
  }

  @RestController
  static class ThrowingController {

    @GetMapping("/not-found")
    void notFound() {
      throw new NotFoundException("missing");
    }

    @GetMapping("/conflict")
    void conflict() {
      throw new ConflictException("conflict");
    }

    @GetMapping("/unauthorized")
    void unauthorized() {
      throw new UnauthorizedException("nope");
    }

    @GetMapping("/boom")
    void boom() {
      throw new IllegalStateException("secret internal detail");
    }

    @PostMapping("/validate")
    void validate(@Valid @RequestBody Payload payload) {}
  }

  record Payload(@NotBlank String name) {}
}
