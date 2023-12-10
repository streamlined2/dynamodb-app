package handler.user;

import java.util.List;

import lombok.Data;

@Data
public class RequestBody {

	private String name;
	private String location;
	private List<String> ageLimits;
	private String sorting;

}
