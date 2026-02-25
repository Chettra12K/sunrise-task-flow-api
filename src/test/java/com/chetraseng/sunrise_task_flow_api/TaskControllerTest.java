package com.chetraseng.sunrise_task_flow_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/tasks";

    // ── Helper ────────────────────────────────────────────────────────────────

    private void createTask(String title, String description) throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"%s","description":"%s"}
                        """.formatted(title, description)));
    }

    // ── GET all ───────────────────────────────────────────────────────────────

    @Test
    void getAllTasks_initially_returnsEmptyList() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getAllTasks_afterCreatingTwo_returnsBothTasks() throws Exception {
        createTask("Task One", "First task");
        createTask("Task Two", "Second task");

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Task One", "Task Two")));
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Test
    void createTask_validRequest_returns201WithBody() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Learn Spring Boot","description":"Complete the course"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.description").value("Complete the course"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    // ── GET by ID ─────────────────────────────────────────────────────────────

    @Test
    void getTaskById_existingTask_returns200() throws Exception {
        createTask("My Task", "Some description");

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("My Task"));
    }

    @Test
    void getTaskById_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/9999"))
                .andExpect(status().isNotFound());
    }

    // ── PUT ───────────────────────────────────────────────────────────────────

    @Test
    void updateTask_existingTask_returns200WithUpdatedFields() throws Exception {
        createTask("Original Title", "Original description");

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated Title","description":"Updated description"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateTask_nonExistingId_returns404() throws Exception {
        mockMvc.perform(put(BASE_URL + "/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Ghost","description":"does not exist"}
                                """))
                .andExpect(status().isNotFound());
    }

    // ── PATCH complete ────────────────────────────────────────────────────────

    @Test
    void completeTask_existingTask_returns200AndCompletedIsTrue() throws Exception {
        createTask("Task to complete", "Do this");

        mockMvc.perform(patch(BASE_URL + "/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void completeTask_nonExistingId_returns404() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/9999/complete"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test
    void deleteTask_existingTask_returns204() throws Exception {
        createTask("Task to delete", "Will be gone");

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_existingTask_isNoLongerRetrievable() throws Exception {
        createTask("Task to delete", "Will be gone");

        mockMvc.perform(delete(BASE_URL + "/1"));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_nonExistingId_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/9999"))
                .andExpect(status().isNotFound());
    }

    // ── Filter by completed ───────────────────────────────────────────────────

    @Test
    void getAllTasks_filterCompletedTrue_returnsOnlyCompletedTasks() throws Exception {
        createTask("Pending task", "Not done yet");
        createTask("Done task", "Already finished");
        mockMvc.perform(patch(BASE_URL + "/2/complete"));

        mockMvc.perform(get(BASE_URL + "?completed=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Done task"))
                .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    void getAllTasks_filterCompletedFalse_returnsOnlyPendingTasks() throws Exception {
        createTask("Pending task", "Not done yet");
        createTask("Done task", "Already finished");
        mockMvc.perform(patch(BASE_URL + "/2/complete"));

        mockMvc.perform(get(BASE_URL + "?completed=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Pending task"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    void getAllTasks_noFilter_returnsAllTasks() throws Exception {
        createTask("Pending task", "Not done yet");
        createTask("Done task", "Already finished");
        mockMvc.perform(patch(BASE_URL + "/2/complete"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
