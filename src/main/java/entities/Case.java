package entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Case {

    private String fileName;
    private String url;
    private Year year;
}
