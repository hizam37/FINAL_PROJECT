package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import java.time.LocalDateTime;

@Repository
public interface SiteRepository extends JpaRepository<Site,Integer> {

    @Query("SELECT COUNT(url) FROM Site")
    long countSite();
    @Query("SELECT s.id from Site s WHERE s.url = :url")
    Integer getIdByUrl(String url);
    Site findSiteByUrl(String url);
    Site findSiteById(Integer id);
    @Query("SELECT s.status FROM Site s WHERE s.url=:url")
    String findStatusByUrl(@Param("url") String url);
    @Query("SELECT s.statusTime FROM Site s WHERE s.url=:url")
    LocalDateTime findStatusTimeByUrl(@Param("url") String url);
    @Query("SELECT s.lastError FROM Site s WHERE s.url=:url")
    String findLastErrorByUrl(@Param("url") String url);

}
