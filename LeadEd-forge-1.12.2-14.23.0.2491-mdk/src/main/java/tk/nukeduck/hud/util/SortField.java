package tk.nukeduck.hud.util;

import java.util.Comparator;

public interface SortField<T> extends Comparator<T> {
	public String getUnlocalizedName();
	public boolean isInverted();
}
