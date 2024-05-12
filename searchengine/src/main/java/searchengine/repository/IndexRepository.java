package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexTable;
import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexTable,Integer> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM IndexTable l WHERE l.page.id = :pageId")
    void deleteByPageId(@Param("pageId") Integer pageId);

    @Query("SELECT SUM(i.rank) FROM IndexTable i where i.page.id IN i.page.id GROUP BY i.page.id")
    List<Long> findRankPerPageByPageIds(@Param("pageIds") List<Integer> pageIds);

    @Query("SELECT i.page.id FROM IndexTable i WHERE i.lemma.id IN :lemmaIds")
    List<Integer> findPageIdsByLemmaIds(List<Integer> lemmaIds);

    @Query("SELECT i.lemma.id FROM IndexTable i WHERE i.page.id=:pageId")
    List<Integer> findLemmasIdByPageId(@Param("pageId") Integer pageId);

}
