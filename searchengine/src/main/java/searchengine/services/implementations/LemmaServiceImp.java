package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;
import searchengine.services.LemmaService;

@Service
@RequiredArgsConstructor
public class LemmaServiceImp implements LemmaService {

    private final LemmaRepository lemmaRepository;
    @Override
    public Lemma getLemma(String lemmaText, Site site) {
        Lemma existingLemma = lemmaRepository.findLemmaBySiteId(lemmaText, site.getId());
        if (existingLemma == null) {
            existingLemma = new Lemma();
            existingLemma.setSite(site);
            existingLemma.setLemma(lemmaText);
            existingLemma.setFrequency(1);
        }else {
            existingLemma.setFrequency(existingLemma.getFrequency() + 1);
        }
        lemmaRepository.save(existingLemma);
        return existingLemma;
    }
}
