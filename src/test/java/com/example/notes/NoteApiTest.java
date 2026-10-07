package com.example.notes;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Integration test: full application context, real HTTP layer (MockMvc), real MySQL. */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
class NoteApiTest {

    @Autowired
    private MockMvc mockMvc;

    private long createNote(String title, String content) throws Exception {
        String json = "{\"title\":\"" + title + "\",\"content\":\"" + content + "\"}";
        MvcResult result = mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    @Test
    void createNoteReturns201WithBody() throws Exception {
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Groceries\",\"content\":\"milk\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Groceries")));
    }

    @Test
    void createNoteWithBlankTitleReturns400() throws Exception {
        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"content\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMissingNoteReturns404() throws Exception {
        mockMvc.perform(get("/api/notes/987654321"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createdNoteCanBeReadBack() throws Exception {
        String title = "read-" + UUID.randomUUID();
        long id = createNote(title, "content");
        mockMvc.perform(get("/api/notes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(title)));
    }

    @Test
    void titleFilterFindsOnlyMatchingNote() throws Exception {
        String title = "filter-" + UUID.randomUUID();
        createNote(title, "content");
        mockMvc.perform(get("/api/notes").param("title", title.toUpperCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(title)));
    }

    @Test
    void deletedNoteIsNoLongerFound() throws Exception {
        long id = createNote("delete-" + UUID.randomUUID(), "content");
        mockMvc.perform(delete("/api/notes/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/notes/" + id)).andExpect(status().isNotFound());
    }
}
