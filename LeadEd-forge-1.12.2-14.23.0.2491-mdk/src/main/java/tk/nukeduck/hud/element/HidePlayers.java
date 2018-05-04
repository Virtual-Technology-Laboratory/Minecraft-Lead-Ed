package tk.nukeduck.hud.element;

import static tk.nukeduck.hud.BetterHud.MC;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tk.nukeduck.hud.element.settings.SettingBoolean;
import tk.nukeduck.hud.util.Bounds;

public class HidePlayers extends HudElement {
	private final SettingBoolean includeMe;

	public HidePlayers() {
		super("hidePlayers");
		this.settings.add(includeMe = new SettingBoolean("includeMe"));
	}

	@SubscribeEvent
	public void entityRender(RenderLivingEvent.Pre<EntityPlayer> e) {
		if(isEnabled() && e.getEntity() instanceof EntityPlayer &&
				(includeMe.get() || !e.getEntity().equals(MC.player))) {
			e.setCanceled(true);
		}
	}

	@Override
	public void loadDefaults() {
		super.loadDefaults();

		setEnabled(false);
		includeMe.set(false);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override public Bounds render(Event event) {return null;}
	@Override public boolean shouldRender(Event event) {return false;}
}
