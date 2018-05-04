package tk.nukeduck.hud.element.text;

import static tk.nukeduck.hud.BetterHud.MANAGER;
import static tk.nukeduck.hud.BetterHud.MC;
import static tk.nukeduck.hud.BetterHud.SPACER;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.Event;
import tk.nukeduck.hud.element.settings.Legend;
import tk.nukeduck.hud.element.settings.SettingChoose;
import tk.nukeduck.hud.util.Bounds;
import tk.nukeduck.hud.util.Direction;
import tk.nukeduck.hud.util.PaddedBounds;
import tk.nukeduck.hud.util.Point;

public class Distance extends TextElement {
	private final SettingChoose mode = new SettingChoose(3);

	@Override
	public void loadDefaults() {
		super.loadDefaults();

		position.set(Direction.CENTER);
		mode.setIndex(0);
	}

	public Distance() {
		super("distance", Direction.CORNERS | Direction.CENTER.flag());
		this.settings.add(new Legend("misc"));
		this.settings.add(mode);
	}

	@Override
	protected PaddedBounds moveBounds(PaddedBounds bounds) {
		if(position.getDirection() == Direction.CENTER) {
			bounds.position = new Point(MANAGER.getResolution().x / 2 - SPACER, MANAGER.getResolution().y / 2 - SPACER);
			return Direction.SOUTH_EAST.align(bounds);
		} else {
			return super.moveBounds(bounds);
		}
	}

	@Override
	protected Bounds getPadding() {
		return Bounds.getPadding(border ? 2 : 0);
	}

	@Override
	protected Bounds render(Event event, List<String> text) {
		border = mode.getIndex() == 2;
		return super.render(event, text);
	}

	@Override
	protected List<String> getText() {
		RayTraceResult trace = MC.getRenderViewEntity().rayTrace(200, 1.0F);

		if(trace != null) {
			long distance = Math.round(Math.sqrt(trace.getBlockPos().distanceSqToCenter(MC.player.posX, MC.player.posY, MC.player.posZ)));

			if(mode.getIndex() == 2) {
				return Arrays.asList(String.valueOf(distance));
			} else {
				return Arrays.asList(I18n.format("betterHud.hud.distance." + mode.getIndex(), String.valueOf(distance)));
			}
		} else {
			return null;
		}
	}
}
