package zack.repositories;

import org.springframework.data.jpa.repository.Query;
import zack.models.Comments;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by student on 7/12/17.
 */
public interface CommentsRepository extends CrudRepository<Comments, Long> {
    @Query(value = "select comments.photoid,comments.commentsid, comments.username,comments.comment, comments.username, photos.id " +
            " from photos, comments where comments.photoid=?1 ;",nativeQuery = true)
    List<Comments> findAllByPhotoID(long photoId);
}
