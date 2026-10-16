package claims.intake;

import java.util.List;

public class DocumentVerifier {
    public boolean isValid(List<String> documents) {
        if (documents == null || documents.isEmpty()) return false;
        return documents.stream().noneMatch(d -> d == null || d.isBlank());
    }
}