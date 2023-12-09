package layer.model;

import java.util.Optional;

public enum SortingOrder {

	ASCENDING("ascending"), DESCENDING("descending");

	private String label;

	SortingOrder(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static Optional<SortingOrder> getByLabel(String label) {
		for (SortingOrder value : values()) {
			if (value.getLabel().startsWith(label.toLowerCase())) {
				return Optional.of(value);
			}
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return label;
	}

}
