package com.swagger.demo;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swagger.demo.model.Item;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SwaggerDemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    @WithMockUser
    void getAllItems_shouldReturnInitialItems() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Sample Item 1")))
                .andExpect(jsonPath("$[1].name", is("Sample Item 2")));
    }

    @Test
    @WithMockUser
    void getItemById_whenItemExists_shouldReturnItem() throws Exception {
        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Sample Item 1")));
    }

    @Test
    @WithMockUser
    void getItemById_whenItemDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/items/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createItem_withValidData_shouldCreateItem() throws Exception {
        Item newItem = new Item(0, "New Item", "A brand new item.");

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("New Item")));
    }

    @Test
    @WithMockUser
    void createItem_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Name is blank, which violates @NotBlank
        Item invalidItem = new Item(0, "", "Invalid item.");

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateItem_whenItemExists_shouldUpdateItem() throws Exception {
        Item updatedItem = new Item(1, "Updated Item 1", "Updated description.");

        mockMvc.perform(put("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Item 1")));
    }

    @Test
    @WithMockUser
    void updateItem_whenItemDoesNotExist_shouldReturnNotFound() throws Exception {
        Item updatedItem = new Item(99, "Non-existent", "This item does not exist.");

        mockMvc.perform(put("/api/items/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteItem_whenItemExists_shouldDeleteItem() throws Exception {
        mockMvc.perform(delete("/api/items/2"))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/api/items/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteItem_whenItemDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/items/99"))
                .andExpect(status().isNotFound());
    }
}
