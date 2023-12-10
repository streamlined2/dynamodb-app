package layer.model;

import java.util.List;

public interface Entity<D> {

	D toDto();

	static <E extends Entity<D>, D extends Dto<E>> List<D> toDtoList(List<E> entityList) {
		return entityList.stream().map(Entity::toDto).toList();
	}

}
