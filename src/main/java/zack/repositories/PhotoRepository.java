package zack.repositories;

import org.springframework.data.jpa.repository.Query;
import zack.models.Photo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface PhotoRepository extends CrudRepository<Photo, Long>{
    List<Photo> findAllByBotmessageIsNotAndTopmessageIsNot(String botmessage, String topmessage);
    List<Photo> findAllByBotmessageEqualsAndTopmessageEquals(String botmessage, String topmessage);
    List<Photo> findAllByType(String type);
    List<Photo> findAllByUsername(String type);
    @Query(value = "select username, id, created_at, filter, image, likes,botmessage,topmessage,title," +
            "type from photos where username=?1 order by created_at DESC LIMIT 1;",nativeQuery = true)
    List<Photo> findAllByUsernameByOrderByDateAsc(String username);

    @Query(value = "select distinct user_data.username,photos.username, followers.follower_name," +
            "followers.username,photos.id, created_at, filter, image," +
            "likes, type, botmessage, topmessage, title from user_data, followers," +
            "photos where user_data.username=?1 and followers.username=?2 and photos.username=followers.follower_name;",nativeQuery = true)
    List<Photo> FindAllByFollower(String username, String followerUsername);
            /*
            select user_data.username,photo.username, followers.follower_name, followers.username,id, created_at, filter, image, " +
            "likes, botmessage, topmessage, title from user_data, followers, photos where user_data.username=followers.username and" +
            " photo.username=followers.follower_name;
             */
    //Photo findFirstByPhotoList(List<Photo> photoList);
    //List<Photo> findFirstBy
    //Photo findById(long id);
    Photo findById(Long id);

}
