package searchengine.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
@Data
public class LinkStructure {
    private String url;
    private String path;
    private String NameOfSite;

    public static LinkStructure getFromLink(String link){
        LinkStructure structure = new LinkStructure();
        Pattern pathPattern = Pattern.compile("(https?://)([^/]+)(/[^?#]*)?");
        Matcher linkMatcher = pathPattern.matcher(link);
        if (linkMatcher.matches()) {
            structure.setUrl(linkMatcher.group(1) + linkMatcher.group(2));
        }
        if (linkMatcher.matches()) {
            structure.setNameOfSite(linkMatcher.group(2));
            structure.setPath(linkMatcher.group(3));
        }
        return structure;
    }
}


