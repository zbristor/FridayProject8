package zack.repositories;

import zack.models.Followers;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by student on 7/12/17.
 */
public interface FollowersRepository extends CrudRepository<Followers, Long> {
}
