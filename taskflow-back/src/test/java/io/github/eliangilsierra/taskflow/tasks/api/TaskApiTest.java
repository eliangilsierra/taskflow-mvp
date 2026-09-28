package io.github.eliangilsierra.taskflow.tasks.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskApiTest {

  @Autowired MockMvc mockMvc;

  private String token;

  @BeforeEach
  void signIn() throws Exception {
    token = registerAndGetToken();
  }

  @Test
  void requiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    mockMvc
        .perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void runsTheFullTaskLifecycle() throws Exception {
    long id = createTask("Write report", "HIGH");

    mockMvc
        .perform(authed(get("/api/tasks/" + id)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Write report"))
        .andExpect(jsonPath("$.status").value("TODO"))
        .andExpect(jsonPath("$.priority").value("HIGH"));

    mockMvc
        .perform(
            authed(put("/api/tasks/" + id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"title\":\"Write final report\",\"priority\":\"LOW\",\"dueDate\":\"2030-01-01\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Write final report"))
        .andExpect(jsonPath("$.priority").value("LOW"))
        .andExpect(jsonPath("$.dueDate").value("2030-01-01"));

    mockMvc
        .perform(
            authed(patch("/api/tasks/" + id + "/status"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DONE\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DONE"));

    mockMvc.perform(authed(delete("/api/tasks/" + id))).andExpect(status().isNoContent());
    mockMvc.perform(authed(get("/api/tasks/" + id))).andExpect(status().isNotFound());
  }

  @Test
  void flagsOverdueTasks() throws Exception {
    mockMvc
        .perform(
            authed(post("/api/tasks"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Late\",\"priority\":\"LOW\",\"dueDate\":\"2000-01-01\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.overdue").value(true));
  }

  @Test
  void validatesTheRequestBody() throws Exception {
    mockMvc
        .perform(
            authed(post("/api/tasks"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"  \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors", hasSize(2)));
  }

  @Test
  void isolatesTasksBetweenUsers() throws Exception {
    long id = createTask("Mine", "LOW");
    String other = "Bearer " + registerAndGetToken();

    mockMvc
        .perform(get("/api/tasks/" + id).header(HttpHeaders.AUTHORIZATION, other))
        .andExpect(status().isNotFound());
    mockMvc
        .perform(delete("/api/tasks/" + id).header(HttpHeaders.AUTHORIZATION, other))
        .andExpect(status().isNotFound());
    mockMvc
        .perform(get("/api/tasks").header(HttpHeaders.AUTHORIZATION, other))
        .andExpect(jsonPath("$.totalItems").value(0));
    mockMvc.perform(authed(get("/api/tasks/" + id))).andExpect(status().isOk());
  }

  @Test
  void filtersSortsAndPaginatesTheList() throws Exception {
    createTask("Alpha", "LOW");
    createTask("Beta", "HIGH");
    createTask("Gamma", "HIGH");

    mockMvc
        .perform(
            authed(get("/api/tasks"))
                .param("priority", "HIGH")
                .param("size", "1")
                .param("page", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.totalItems").value(2))
        .andExpect(jsonPath("$.totalPages").value(2));

    mockMvc
        .perform(authed(get("/api/tasks")).param("q", "alp"))
        .andExpect(jsonPath("$.items[0].title").value("Alpha"))
        .andExpect(jsonPath("$.totalItems").value(1));

    mockMvc
        .perform(authed(get("/api/tasks")).param("sort", "PRIORITY"))
        .andExpect(jsonPath("$.items[2].title").value("Alpha"));
  }

  @Test
  void rejectsInvalidQueryParameters() throws Exception {
    mockMvc
        .perform(authed(get("/api/tasks")).param("size", "1000"))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(authed(get("/api/tasks")).param("status", "NOPE"))
        .andExpect(status().isBadRequest());
  }

  private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder request) {
    return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
  }

  private long createTask(String title, String priority) throws Exception {
    String body =
        mockMvc
            .perform(
                authed(post("/api/tasks"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"%s\",\"priority\":\"%s\"}".formatted(title, priority)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return ((Number) JsonPath.read(body, "$.id")).longValue();
  }

  private String registerAndGetToken() throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"email\":\"u-%s@example.com\",\"password\":\"s3cret-pass\",\"displayName\":\"U\"}"
                            .formatted(UUID.randomUUID())))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JsonPath.read(body, "$.accessToken");
  }
}
