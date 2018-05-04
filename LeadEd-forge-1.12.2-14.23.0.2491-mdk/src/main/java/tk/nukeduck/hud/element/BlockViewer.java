package tk.nukeduck.hud.element;

import static tk.nukeduck.hud.BetterHud.MANAGER;
import static tk.nukeduck.hud.BetterHud.MC;
import static tk.nukeduck.hud.BetterHud.SPACER;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldNameable;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.Console;
import tk.nukeduck.hud.BetterHud;
import tk.nukeduck.hud.element.settings.Legend;
import tk.nukeduck.hud.element.settings.SettingBoolean;
import tk.nukeduck.hud.element.text.TextElement;
import tk.nukeduck.hud.network.InventoryNameQuery;
import tk.nukeduck.hud.network.Version;
import tk.nukeduck.hud.util.Bounds;
import tk.nukeduck.hud.util.Direction;
import tk.nukeduck.hud.util.GlUtil;
import tk.nukeduck.hud.util.PaddedBounds;

public class BlockViewer extends TextElement {
	private final SettingBoolean showBlock = new SettingBoolean("showItem").setUnlocalizedValue(SettingBoolean.VISIBLE);
	private final SettingBoolean showIds = new SettingBoolean("showIds").setUnlocalizedValue(SettingBoolean.VISIBLE);

	private final SettingBoolean invNames = new SettingBoolean("invNames") {
		@Override
		public boolean enabled() {
			return super.enabled() && BetterHud.serverVersion.compareTo(new Version(1, 4)) >= 0;
		}
	};

	private RayTraceResult trace;
	private IBlockState state;
	private ItemStack stack;

	private double leadLevel = 0.0;
	private int tickCounter = 0;

	public BlockViewer() {
		super("blockViewer", Direction.CORNERS | Direction.getFlags(Direction.CENTER, Direction.NORTH));

		settings.add(new Legend("misc"));
		settings.add(showBlock);
		settings.add(invNames);
		settings.add(showIds);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void loadDefaults() {
		super.loadDefaults();

		position.set(Direction.NORTH);
		showBlock.set(false);
		showIds.set(false);
		invNames.set(false);
	}

	@Override
	public boolean shouldRender(Event event) {
        /*
		Entity entity =  MC.player;

		Console.println(entity);

		if (entity == null) {
			return false;
		}

		BlockPos blockpos = new BlockPos(entity);
		*/

		BlockPos blockpos = new BlockPos(MC.player.prevPosX,
				 						 MC.player.prevPosY - 1.0,
										 MC.player.prevPosZ);

		state = MC.world.getBlockState(blockpos);
		return true;

		// todo: should retrace down to get the block underneath the player
		/*
		if(!super.shouldRender(event)) return false;

		trace = MC.getRenderViewEntity().rayTrace(HudElement.GLOBAL.getBillboardDistance(), 1f);

		if(trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK) {
			state = MC.world.getBlockState(trace.getBlockPos());
			stack = getDisplayStack(trace, state);
			return true;
		} else {
			return false;
		}
		*/
	}

	private double getLeadLevel(int id, int meta) {

		if (id == 1){ // stone
			if (meta == 6) {
				return 0.0;
			}
			if (meta == 2) {
				return -100.0;
			}
			return 4.0;
		}
		else if (id == 2) { // grass
			return 2.0;
		}
		else if (id == 3){ // dirt
			return 20.0;
		}
		else if (id == 12) { // sand
			return 40.0;
		}
		else if (id == 8) { // flowing water
			return 42.0;
		}
		else if (id == 9) { // still water
			return 43.0;
		}
		else if (id == 0) { //Air
			return 0.0;
		}

		return 0.0;
	}

	public void decrementLeadLevel() {
		leadLevel -= 1000;
		if (leadLevel < 0.0) {
			leadLevel = 0.0;
		}
	}

	private void setMaxHealth(double health) {
		MC.player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		if (health < MC.player.getHealth()) {
			MC.player.setPlayerSPHealth((float)health);
		}
	}
	private double maxHealthFunc(double leadLevel) {
		if (leadLevel > 5000) {
			return 10.0;
		} else if (leadLevel > 4000) {
			return 12.0;
		} else if (leadLevel > 2000) {
			return 14.0;
		} else if (leadLevel > 1000) {
			return 16.0;
		} else if (leadLevel > 400) {
			return 18.0;
		}
		return 20.0;
	}

	@Override
	protected List<String> getText() {
		tickCounter++;

		if (tickCounter > 30) {

			int id = Block.getIdFromBlock(state.getBlock());
			int meta = state.getBlock().getMetaFromState(state);

			leadLevel += getLeadLevel(id, meta);

			if (leadLevel < 0.0) {
				leadLevel = 0.0;
			}

			double maxHealth = maxHealthFunc(leadLevel);
			setMaxHealth(maxHealth);
			tickCounter = 0;
		}

		String text =  String.format("%d", (int)leadLevel);
		return Arrays.asList(text);
	}

	@Override
	protected Bounds getPadding() {
		int vPad = 20 - MC.fontRenderer.FONT_HEIGHT;
		int bottom = vPad / 2;
		Bounds bounds = Bounds.getPadding(5, vPad - bottom, 5, bottom);

		if(stack != null && showBlock.get()) {
			bounds.left(bounds.left() - 21);
		}
		return bounds;
	}

	@Override
	protected void drawBorder(PaddedBounds bounds) {
		GlUtil.drawTooltipBox(bounds.x(), bounds.y(), bounds.width(), bounds.height());
	}

	@Override
	protected void drawExtras(PaddedBounds bounds) {
		if(stack != null && showBlock.get()) {
			RenderHelper.enableGUIStandardItemLighting();
			MC.getRenderItem().renderItemAndEffectIntoGUI(stack, bounds.x() + 5, bounds.y() + 2);
			RenderHelper.disableStandardItemLighting();
		}
	}

	@Override
	protected PaddedBounds moveBounds(PaddedBounds bounds) {
		if(position.getDirection() == Direction.CENTER) {
			bounds.position = MANAGER.getResolution().scale(.5f, .5f).sub(0, SPACER);
			return Direction.SOUTH.align(bounds);
		} else {
			return super.moveBounds(bounds);
		}
	}

	/** Creates the most representative item stack for the given result.<br>
	 * If the block has no {@link net.minecraft.item.ItemBlock}, it is impossible to create a stack.
	 *
	 * @see net.minecraftforge.common.ForgeHooks#onPickBlock(RayTraceResult, net.minecraft.entity.player.EntityPlayer, net.minecraft.world.World) */
	private static ItemStack getDisplayStack(RayTraceResult trace, IBlockState state) {
		return null;
		/*
		ItemStack stack = state.getBlock().getPickBlock(state, trace, MC.world, trace.getBlockPos(), MC.player);
		if(isStackEmpty(stack)) {
			// Pick block is disabled, however we can grab the information directly
			stack = new ItemStack(state.getBlock(), state.getBlock().getMetaFromState(state));

			if(isStackEmpty(stack)) { // There's no registered ItemBlock, no stack exists
				return null;
			}
		}
		// At this point stack contains some item

		if(state.getBlock().hasTileEntity(state)) { // Tile entity data can change rendering or display name
			TileEntity tileEntity = MC.world.getTileEntity(trace.getBlockPos());

			if(tileEntity != null) {
				MC.storeTEInStack(stack, tileEntity);
			}
		}
		return stack;
		*/

	}

	/** Chooses the best name for the given result and its related stack.
	 * @param stack The direct result of {@link #getDisplayStack(RayTraceResult, IBlockState)}. May be {@code null}
	 *
	 * @see ItemStack#getDisplayName()
	 * @see TileEntity#getDisplayName()
	 * @see net.minecraft.item.ItemBlock#getUnlocalizedName(ItemStack) */
	private String getBlockName(RayTraceResult trace, IBlockState state, ItemStack stack) {
		return "";
		/*
		if(state.getBlock() == Blocks.END_PORTAL) {
			return I18n.format("tile.endPortal.name");
		}

		if(invNames.get() && state.getBlock().hasTileEntity(state)) {
			TileEntity tileEntity = MC.world.getTileEntity(trace.getBlockPos());

			if(tileEntity instanceof IWorldNameable) {
				ITextComponent invName = ensureInvName(trace.getBlockPos());

				if(invName != null) {
					return invName.getFormattedText();
				}
			}
		}
		
		return isStackEmpty(stack) ? state.getBlock().getLocalizedName() : stack.getDisplayName();
		*/
	}

	/** @return Information about the block's related IDs */
	private String getIdString(IBlockState state) {
		return "";
		/*
		String name = Block.REGISTRY.getNameForObject(state.getBlock()).toString();
		int id = Block.getIdFromBlock(state.getBlock());
		int meta = state.getBlock().getMetaFromState(state);

		return String.format("%s(%s:%d/#%04d)", ChatFormatting.YELLOW, name, meta, id);
		*/
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerDisconnected(ClientDisconnectionFromServerEvent event) {
		nameCache.clear();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerChangeDimension(PlayerChangedDimensionEvent event) {
		nameCache.clear();
	}

	@SideOnly(Side.SERVER)
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		BetterHud.NET_WRAPPER.sendToDimension(new InventoryNameQuery.Response(event.getPos(), null), event.getWorld().provider.getDimension());
	}

	public static final Map<BlockPos, ITextComponent> nameCache = new HashMap<BlockPos, ITextComponent>();

	public void onNameReceived(BlockPos pos, ITextComponent name) {
		nameCache.put(pos, name);
	}

	/** If the client doesn't know the name of an inventory, this method
	 * asks the server, then until the response is given, a generic
	 * container name will be returned. When the client finds out the actual name
	 * of the inventory, it will return that value */
	private static ITextComponent ensureInvName(BlockPos pos) {
		return null;
		/*
		if(!nameCache.containsKey(pos)) {
			BetterHud.NET_WRAPPER.sendToServer(new InventoryNameQuery.Request(pos));
			nameCache.put(pos, null);
		}
		ITextComponent name = nameCache.get(pos);

		if(name != null) {
			return name;
		} else {
			return MC.world.getTileEntity(pos).getDisplayName();
		}
		*/
	}

	/** Considers {@code null} to be empty
	 * @see ItemStack#isEmpty() */
	private static boolean isStackEmpty(ItemStack stack) {
		return stack == null || stack.isEmpty();
	}
}
