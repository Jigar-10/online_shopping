package model;

import java.util.Objects;

/**
 * Reusable Key-Value item for Swing JComboBox dropdowns.
 * Displays user-friendly text while maintaining internal DB ID.
 */
public class ComboItem<T> {
    private final T id;
    private final String label;
    private final Object extra;

    public ComboItem(T id, String label) {
        this(id, label, null);
    }

    public ComboItem(T id, String label, Object extra) {
        this.id = id;
        this.label = label;
        this.extra = extra;
    }

    public T getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Object getExtra() {
        return extra;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComboItem<?> comboItem)) return false;
        return Objects.equals(id, comboItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
