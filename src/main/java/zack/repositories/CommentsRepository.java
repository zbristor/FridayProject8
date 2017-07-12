package zack.repositories;

import zack.models.Comments;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by student on 7/12/17.
 */
public interface CommentsRepository extends CrudRepository<Comments, Long> {
}
