package zack.repositories;

import org.springframework.data.jpa.repository.Query;
import zack.models.Photo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface PhotoRepository extends CrudRepository<Photo, Long>{
    List<Photo> findAllByBotmessageIsNotAndTopmessageIsNot(String botmessage, String topmessage);
    List<Photo> findAllByBotmessageEqualsAndTopmessageEquals(String botmessage, String topmessage);
    List<Photo> findAllByType(String type);

    @Query(value = "select username, id, created_at, filter, image, likes,botmessage,topmessage,title," +
            "type from photos where username=?1 order by created_at DESC LIMIT 1;",nativeQuery = true)
    List<Photo> findAllByUsernameByOrderByDateAsc(String username);
    //Photo findFirstByPhotoList(List<Photo> photoList);
    //List<Photo> findFirstBy
    //Photo findById(long id);
    Photo findById(Long id);

}
