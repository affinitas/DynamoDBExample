package de.affinitas.data;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.affinitas.Application;
import de.affinitas.data.dynamo.LocalDynamoDBConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LocalDynamoDBConfiguration.class, Application.class})
@AutoConfigureMockMvc
public class UserDataRestAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void apiTestCase() throws Exception {
        String userId = UUID.randomUUID().toString();

        User user = new User();
        user.setId(userId);
        user.setLastName("Test");

        mockMvc.perform(post(UserRepository.USER_REPOSITY_PATH)
                                                               .content(new ObjectMapper().writeValueAsString(user)))
               .andExpect(status().isCreated());


        mockMvc.perform(get(UserRepository.USER_REPOSITY_PATH + "/{userId}", userId))
               .andExpect(status().isOk())
                          .andExpect(jsonPath("$.lastName", is("Test")));

        mockMvc.perform(get(UserRepository.USER_REPOSITY_PATH + "/{userId}", "1"))
               .andExpect(status().isNotFound());

    }

}
