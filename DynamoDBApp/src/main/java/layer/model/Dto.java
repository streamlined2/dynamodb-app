package layer.model;

import java.util.List;

public interface Dto<E> {

	E toEntity();

	static <E extends Entity<D>, D extends Dto<E>> List<E> toEntityList(List<D> dtoList) {
		return dtoList.stream().map(Dto::toEntity).toList();
	}

}
