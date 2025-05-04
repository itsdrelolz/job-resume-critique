package itsc4155.jobsearch.tika;

import lombok.Getter;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

@Component
public class TikaHandler {

    @Getter
    private Tika tika;

    public TikaHandler() {
        tika = new Tika();
    }
}
