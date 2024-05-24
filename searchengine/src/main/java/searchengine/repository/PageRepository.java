package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page,Integer> {

    @Query("SELECT COUNT(path) FROM Page")
    long countByPage();

    @Query("SELECT COUNT(p.path) FROM Page p WHERE p.site.id=:siteId")
    long countPageBySiteId(@Param("siteId") Integer siteId);

    @Query("SELECT p FROM Page p WHERE  p.id IN :pageIds and p.site.id = :siteId")
    List<Page> findPageBySiteIdAndPageId(@Param("pageIds") List<Integer> pageIds, @Param("siteId") Integer siteId);

    @Query("SELECT p FROM Page p WHERE  p.id IN :pageIds")
    List<Page> findPageByPageId(@Param("pageIds") List<Integer> pageIds);

    List<Page> findPageBySiteId(Integer id);
}
