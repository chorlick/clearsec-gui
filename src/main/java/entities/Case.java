package entities;

import lombok.*;

@Data
@AllArgsConstructor
public class Case {

    private String fileName;
    private String url;
    private Year year;
}
