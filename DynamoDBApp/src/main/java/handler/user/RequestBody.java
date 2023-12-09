package handler.user;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestBody {

	private String name;
	private String location;
	private List<String> ageLimits;
	private String sorting;

}
