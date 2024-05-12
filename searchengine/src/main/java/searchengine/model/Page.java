package searchengine.model;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import javax.persistence.*;
import javax.persistence.Index;

@Entity
@Getter
@Setter
@Table(indexes = @Index(name = "idx_path",columnList = "path",unique = true))
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.MERGE)
    private Site site;

    @Column(columnDefinition = "TEXT NOT NULL")
    private String path;

    private Integer code;

    @Column(columnDefinition = "MEDIUMTEXT NOT NULL")
    private String content;

}
