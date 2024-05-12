package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import java.util.List;
import java.util.Set;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma,Integer> {

    Lemma findByLemma(String word);

    @Query("SELECT COUNT(lemma) FROM Lemma")
    long countByLemma();

    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemma AND l.site.id=:siteId")
    Lemma findLemmaBySiteId(@Param("lemma") String lemma,@Param("siteId") Integer siteId);

    @Query("SELECT l.frequency FROM Lemma l WHERE l.lemma IN :lemma")
    Double findFrequencyByLemma(@Param("lemma") String lemma);

    @Query("SELECT l.id FROM Lemma l WHERE l.lemma IN :lemmas AND l.site.id = :siteId")
    List<Integer> findLemmaIdsByLemmaAndSiteId(Set<String> lemmas, Integer siteId);

    @Query("SELECT l.id FROM Lemma l WHERE l.lemma IN :lemmas")
    List<Integer> findLemmaIdsByLemmas(Set<String> lemmas);

    @Query(value = "SELECT l FROM Lemma l WHERE l.id IN :lemmaIds")
    List<Lemma> findByLemmaIds(@Param("lemmaIds") List<Integer> lemmaIds);

    @Query("SELECT l.frequency FROM Lemma l WHERE l.lemma = :lemma AND l.site.id=:siteId")
    Double findFrequencyByLemmaAndSiteId(@Param("lemma") String lemma, @Param("siteId") Integer siteId);

    @Query("SELECT COUNT(lemma) FROM Lemma WHERE site.id=:siteId")
    long countLemmaBySiteId(@Param("siteId") Integer siteId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Lemma l WHERE l.site.id = :siteId")
    void deleteBySiteId(@Param("siteId") Integer siteId);

}
