package ru.kafpin.school;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SchoolboyApiTests {
    private static final String API = "/api/schoolboys";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    @Test
    void performsFullCrudAndPartialUpdate() throws Exception {
        mvc.perform(get(API)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/api/schools")).andExpect(jsonPath("$.classes.length()").value(3));
        ObjectNode data = body();
        var created = mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(data.toString()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.surname").value("Фомин"))
                .andReturn().getResponse();
        String resource = created.getHeader("Location");
        mvc.perform(get(resource)).andExpect(status().isOk()).andExpect(jsonPath("$.className").value("7А"));

        data.put("classId", 2);
        mvc.perform(put(resource).contentType(MediaType.APPLICATION_JSON).content(data.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.className").value("8Б"));
        mvc.perform(patch(resource).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phoneNumber":"+7 900 000-44-99","address":{"apartment":"17"}}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.phoneNumber").value("+7 900 000-44-99"))
                .andExpect(jsonPath("$.data.address.apartment").value("17"))
                .andExpect(jsonPath("$.data.address.city").value("Москва"));
        mvc.perform(delete(resource)).andExpect(status().isNoContent());
        mvc.perform(get(resource)).andExpect(status().isNotFound());
        mvc.perform(delete(resource)).andExpect(status().isNotFound());
    }

    @Test
    void validatesRequestsAndCountsByClass() throws Exception {
        mvc.perform(get(API + "/statistics")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].count").value(2))
                .andExpect(jsonPath("$[1].count").value(1));
        mvc.perform(get(API + "/999")).andExpect(status().isNotFound());
        mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.surname").exists());
        mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(body().put("classId", 3).toString()))
                .andExpect(status().isBadRequest());
        mvc.perform(patch(API + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"unknown\":0}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch(API + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"weight\":null}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.weight").exists());
        mvc.perform(get(API + "/1")).andExpect(jsonPath("$.data.weight").value(46.1));
    }

    private ObjectNode body() throws Exception {
        return (ObjectNode) json.readTree("""
                {
                    "surname":"Фомин","name":"Артём","patronymic":"Олегович","gender":"Мужской",
                    "nationality":"Русский","height":156.5,"weight":48.0,"birthDate":"2013-04-15",
                    "phoneNumber":"+7 900 000-44-01","schoolId":1,"classId":1,
                    "address":{"postalCode":"101000","country":"Россия","region":"Москва","district":"Басманный",
                               "city":"Москва","street":"Покровка","house":"8","apartment":null}
                }
                """);
    }
}
