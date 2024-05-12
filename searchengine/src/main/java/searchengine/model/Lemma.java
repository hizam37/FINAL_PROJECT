package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
public class Lemma implements Comparable<Lemma>{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.MERGE)
    private Site site;

    @Column(columnDefinition = "VARCHAR(255)")
    private String lemma;

    private Integer frequency;

    @Override
    public int compareTo(Lemma o) {
        return this.frequency.compareTo(o.frequency);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lemma lemma1 = (Lemma) o;
        return Objects.equals(id, lemma1.id)
                && Objects.equals(site, lemma1.site)
                && Objects.equals(lemma, lemma1.lemma)
                && Objects.equals(frequency, lemma1.frequency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, site, lemma, frequency);
    }
}
