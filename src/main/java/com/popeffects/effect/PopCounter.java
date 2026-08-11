package com.popeffects.effect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.popeffects.config.ConfigManager;

import net.minecraft.world.entity.Entity;

/**
 * Zaehlt, wer wie oft ein Totem gepoppt hat. Reine Client-Statistik fuer die
 * kleine Liste im HUD - genau das, was einem im Kampf sonst entgeht.
 */
public final class PopCounter {
	private static final Map<UUID, Entry> ENTRIES = new LinkedHashMap<>();

	private PopCounter() {
	}

	/** Ein Eintrag der HUD-Liste. */
	public static final class Entry {
		public final String name;
		public int pops;
		public long lastPopMillis;

		private Entry(String name) {
			this.name = name;
		}
	}

	public static void record(Entity entity) {
		Entry entry = ENTRIES.computeIfAbsent(entity.getUUID(), uuid -> new Entry(entity.getName().getString()));
		entry.pops++;
		entry.lastPopMillis = System.currentTimeMillis();
	}

	/**
	 * Die noch frischen Eintraege, der zuletzt gepoppte zuerst. Alte fliegen
	 * dabei gleich raus, damit die Map nicht endlos waechst.
	 */
	public static List<Entry> visible() {
		long now = System.currentTimeMillis();
		long maxAge = ConfigManager.get().popCounterSeconds * 1000L;
		List<Entry> visible = new ArrayList<>();

		ENTRIES.values().removeIf(entry -> now - entry.lastPopMillis > maxAge);

		for (Entry entry : ENTRIES.values()) {
			visible.add(entry);
		}

		visible.sort((a, b) -> Long.compare(b.lastPopMillis, a.lastPopMillis));
		return visible;
	}

	public static void clear() {
		ENTRIES.clear();
	}
}
