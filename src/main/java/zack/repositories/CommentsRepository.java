package zack.repositories;

import org.springframework.data.jpa.repository.Query;
import zack.models.Comments;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by student on 7/12/17.
 */
public interface CommentsRepository extends CrudRepository<Comments, Long> {
    @Query(value = "select * from photos, comments where photos.username=comments.username and photoid=?1;",nativeQuery = true)
    List<Comments> findAllByPhotoID(long photoId);
}
