package bkrc.chaptersofu.domain.aladin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Phrase {
    private String pageNo;
    private String phrase;
}
