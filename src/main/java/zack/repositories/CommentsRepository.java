package zack.repositories;

import org.springframework.data.jpa.repository.Query;
import zack.models.Comments;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by student on 7/12/17.
 */
public interface CommentsRepository extends CrudRepository<Comments, Long> {
    @Query(value = "select comments.commentsid, comments.username, comments.photoid, comments.comment, comments.username, photos.id " +
            " from photos, comments where comments.photoid=?1 and photos.username=comments.username;",nativeQuery = true)
    List<Comments> findAllByPhotoID(long photoId);
}
