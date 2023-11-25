package layer.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RequestBody {

    private String name;
    private String location;
    private List<String> ageLimits;
    private String sorting;

}
