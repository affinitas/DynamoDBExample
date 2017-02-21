package de.affinitas.data;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.affinitas.data.dynamo.LocalDynamoDBConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LocalDynamoDBConfiguration.class})
public class UserRepositoryTest {

    @Autowired
    UserRepository repository;

    @Test
    public void sampleTestCase() {
        User userMatthews = new User("Matthews");
        repository.save(userMatthews);

        User userBeauford = new User("Beauford");
        repository.save(userBeauford);

        List<User> result = repository.findByLastName("Matthews");
        assertThat(result.size(), is(1));
        assertThat(result, hasItems(userMatthews));

    }
}
