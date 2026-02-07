package com.visor.school.search.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.search.controller.SearchController;
import com.visor.school.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@WithMockUser
class SearchControllerContractTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private SearchService searchService;

        @Test
        void getSearchShouldReturnSearchResults() throws Exception {
                // Given
                String query = "John";
                String type = "students";

                // When & Then
                mockMvc.perform(
                                get("/api/v1/search")
                                                .param("q", query)
                                                .param("type", type))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        void getSearchShouldSupportTeachersType() throws Exception {
                // Given
                String query = "Smith";
                String type = "teachers";

                // When & Then
                mockMvc.perform(
                                get("/api/v1/search")
                                                .param("q", query)
                                                .param("type", type))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void getSearchShouldSupportClassesType() throws Exception {
                // Given
                String query = "Mathematics";
                String type = "classes";

                // When & Then
                mockMvc.perform(
                                get("/api/v1/search")
                                                .param("q", query)
                                                .param("type", type))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }
}
