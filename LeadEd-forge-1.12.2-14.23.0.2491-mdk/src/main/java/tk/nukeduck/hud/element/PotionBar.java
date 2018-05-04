package tk.nukeduck.hud.element;

import static tk.nukeduck.hud.BetterHud.MC;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tk.nukeduck.hud.element.settings.SettingBoolean;
import tk.nukeduck.hud.element.settings.SettingPosition;
import tk.nukeduck.hud.util.Bounds;
import tk.nukeduck.hud.util.Colors;
import tk.nukeduck.hud.util.Direction;
import tk.nukeduck.hud.util.GlUtil;

public class PotionBar extends HudElement {
	private final SettingPosition position = new SettingPosition("position", Direction.CORNERS | Direction.CENTER.flag());
	private final SettingBoolean disableDefault = new SettingBoolean("disableDefault");

	public static final ResourceLocation INVENTORY = new ResourceLocation("textures/gui/container/inventory.png");

	@Override
	public void loadDefaults() {
		super.loadDefaults();

		setEnabled(false);
		position.set(Direction.NORTH_WEST);
		disableDefault.set(true);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public PotionBar() {
		super("potionBar");

		settings.add(position);
		this.settings.add(disableDefault);
	}

	@SubscribeEvent
	public void onRenderTick(RenderGameOverlayEvent.Pre event) {
		if(isEnabled() && disableDefault.get() && event.getType() == ElementType.POTION_ICONS) {
			event.setCanceled(true);
		}
	}

	@Override
	public Bounds render(Event event) {
		int amount = MC.player.getActivePotionEffects().size();
		Bounds bounds = position.applyTo(new Bounds(amount * 16, 16));

		GlUtil.enableBlendTranslucent();

		Bounds icon = new Bounds(bounds.x(), bounds.y(), 18, 18);

		for(PotionEffect effect : MC.player.getActivePotionEffects()) {
			if(!effect.getPotion().shouldRenderHUD(effect)) continue;

			MC.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);

			int iconIndex = effect.getPotion().getStatusIconIndex();

			int u = 18 * (iconIndex % 8);
			int v = 198 + 18 * (iconIndex / 8);

			GlStateManager.color(1, 1, 1, effect.getDuration() / 600);

			MC.ingameGUI.drawTexturedModalRect(icon.x(), icon.y(), u, v, icon.width(), icon.height());

			String unlocalizedLevel = "potion.potency." + effect.getAmplifier();
			if(I18n.hasKey(unlocalizedLevel)) {
				MC.ingameGUI.drawString(MC.fontRenderer, I18n.format(unlocalizedLevel), icon.x(), icon.y(), Colors.WHITE);
			}

			icon.x(icon.x() + 16);
		}
		return bounds;
	}
}
