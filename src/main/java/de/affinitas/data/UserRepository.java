package de.affinitas.data;

import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@EnableScan
@RepositoryRestResource(path = UserRepository.USER_REPOSITY_PATH, collectionResourceRel = "users")
public interface UserRepository extends CrudRepository<User, String> {

    public static final String USER_REPOSITY_PATH = "/users";

    List<User> findByLastName(String lastName);

}
