package makeo.gadomancy.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import makeo.gadomancy.api.GadomancyApi;
import makeo.gadomancy.api.golems.AdditionalGolemType;
import makeo.gadomancy.api.golems.AdditionalGolemType.GolemSlotPoint;
import java.awt.Color;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.gui.GuiGolem;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.golems.ContainerGolem;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.ItemGolemCore;
import thaumcraft.common.lib.utils.Utils;

@SideOnly(Side.CLIENT)
public class AdditionalGolemGui extends GuiGolem {
	private float xSize_lo;

	private float ySize_lo;

	private EntityGolemBase golem;

	public AdditionalGolemGui(EntityPlayer player, EntityGolemBase e) {
		super(player, e);
		this.golem = e;
	}
	
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
	    this.xSize_lo = par1;
	    this.ySize_lo = par2;
	}
	
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		if(GadomancyApi.getAdditionalGolemType(this.golem.getGolemType())==null) {
			super.drawGuiContainerBackgroundLayer(par1, par2, par3);
			return;
		}
		AdditionalGolemType type = GadomancyApi.getAdditionalGolemType(this.golem.getGolemType());
		int ordinal = type.getEnumEntry().ordinal();
		if(ordinal > (GolemSlotPoint.values().length - 1)) {
			super.drawGuiContainerBackgroundLayer(par1, par2, par3);
			return;
		}
		int[] point = GolemSlotPoint.getSlot(ordinal).getSlotStart();
		int baseX = this.guiLeft;
		int baseY = this.guiTop;
		GL11.glPushMatrix();
		GL11.glEnable(3042);
		UtilsFX.bindTexture("textures/gui/guigolem.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(baseX, baseY, 0, 0, this.xSize, this.ySize);
		int slots = this.golem.inventory.slotCount;
		IIcon icon = null;
		if (this.golem.getCore() > -1 && ItemGolemCore.hasInventory(this.golem.getCore())) {
			for (int a = 0; a < Math.min(6, slots); a++) {
				drawTexturedModalRect(baseX + 96 + a / 2 * 28, baseY + 12 + a % 2 * 31, point[0], point[1], 24, 24);
				if (this.golem.getUpgradeAmount(4) > 0) {
					drawTexturedModalRect(baseX + 96 + a / 2 * 28, baseY + 4 + a % 2 * 31, 72, 168, 24, 12);
					int color = this.golem.getColors(a + ((ContainerGolem) this.inventorySlots).currentScroll * 6);
					if (color > -1) {
						Color c = new Color(Utils.colors[color]);
						float r = c.getRed() / 255.0F;
						float g = c.getGreen() / 255.0F;
						float b = c.getBlue() / 255.0F;
						GL11.glColor4f(r, g, b, 1.0F);
						drawTexturedModalRect(baseX + 105 + a / 2 * 28, baseY + 7 + a % 2 * 31, 0, 176, 6, 6);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					}
				}
				if (this.golem.getCore() == 5) {
					FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(this.golem.inventory
							.getStackInSlot(a + ((ContainerGolem) this.inventorySlots).currentScroll * 6));
					if (fluid != null) {
						icon = fluid.getFluid().getIcon();
						if (icon != null) {
							GL11.glPushMatrix();
							GL11.glTranslatef((baseX + 108 + a / 2 * 28), (baseY + 24 + a % 2 * 31), 0.0F);
							UtilsFX.renderQuadCenteredFromIcon(true, icon, 16.0F, 1.0F, 1.0F, 1.0F, 200, 771, 1.0F);
							GL11.glPopMatrix();
							UtilsFX.bindTexture("textures/gui/guigolem.png");
						}
					}
				}
			}
			if (slots > 6) {
				if (((ContainerGolem) this.inventorySlots).currentScroll > 0) {
					drawTexturedModalRect(baseX + 111, baseY + 68, 0, 200, 24, 8);
				} else {
					drawTexturedModalRect(baseX + 111, baseY + 68, 0, 208, 24, 8);
				}
				if (((ContainerGolem) this.inventorySlots).currentScroll < ((ContainerGolem) this.inventorySlots).maxScroll) {
					drawTexturedModalRect(baseX + 135, baseY + 68, 24, 200, 24, 8);
				} else {
					drawTexturedModalRect(baseX + 135, baseY + 68, 24, 208, 24, 8);
				}
			}
		}
		if (this.golem.getCore() == 4 && this.golem.getUpgradeAmount(4) > 0) {
			drawTexturedModalRect(baseX + 104, baseY + 5, 8, 168, 8, 8);
			drawTexturedModalRect(baseX + 104, baseY + 21, 8, 168, 8, 8);
			drawTexturedModalRect(baseX + 104, baseY + 37, 8, 168, 8, 8);
			drawTexturedModalRect(baseX + 104, baseY + 53, 8, 168, 8, 8);
			if (this.golem.canAttackHostiles())
				drawTexturedModalRect(baseX + 104, baseY + 5, 8, 176, 8, 8);
			if (this.golem.canAttackAnimals())
				drawTexturedModalRect(baseX + 104, baseY + 21, 8, 176, 8, 8);
			if (this.golem.canAttackPlayers())
				drawTexturedModalRect(baseX + 104, baseY + 37, 8, 176, 8, 8);
			if (this.golem.canAttackCreepers())
				drawTexturedModalRect(baseX + 104, baseY + 53, 8, 176, 8, 8);
			this.fontRendererObj.drawString("Monsters", baseX + 122, baseY + 6, 16764108);
			this.fontRendererObj.drawString("Animals", baseX + 122, baseY + 22, 16777164);
			this.fontRendererObj.drawString("Players", baseX + 122, baseY + 38, 13421823);
			this.fontRendererObj.drawString("Creepers", baseX + 122, baseY + 54, 13434828);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
		if (this.golem.getCore() == 0) {
			drawTexturedModalRect(baseX + 62, baseY + 54, 8, 168, 8, 8);
			String text = "Precise amount";
			if (!this.golem.getToggles()[0]) {
				drawTexturedModalRect(baseX + 62, baseY + 54, 8, 176, 8, 8);
			} else {
				text = "Any amount";
			}
			GL11.glPushMatrix();
			GL11.glTranslatef((baseX + 66), (baseY + 48), 0.0F);
			GL11.glScalef(0.5F, 0.5F, 0.0F);
			int size = this.fontRendererObj.getStringWidth(text);
			this.fontRendererObj.drawString(text, -size / 2, 0, 16645629);
			GL11.glScalef(1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
		if (this.golem.getCore() == 8) {
			drawTexturedModalRect(baseX + 42, baseY + 40, 8, 168, 8, 8);
			String text1 = "Block";
			if (!this.golem.getToggles()[0]) {
				drawTexturedModalRect(baseX + 42, baseY + 40, 8, 176, 8, 8);
			} else {
				text1 = "Empty space";
			}
			drawTexturedModalRect(baseX + 42, baseY + 50, 8, 168, 8, 8);
			String text2 = "Right click";
			if (!this.golem.getToggles()[1]) {
				drawTexturedModalRect(baseX + 42, baseY + 50, 8, 176, 8, 8);
			} else {
				text2 = "Left click";
			}
			drawTexturedModalRect(baseX + 42, baseY + 60, 8, 168, 8, 8);
			String text3 = "Not sneaking";
			if (!this.golem.getToggles()[2]) {
				drawTexturedModalRect(baseX + 42, baseY + 60, 8, 176, 8, 8);
			} else {
				text3 = "Sneaking";
			}
			GL11.glPushMatrix();
			GL11.glTranslatef((baseX + 53), (baseY + 42), 0.0F);
			GL11.glScalef(0.5F, 0.5F, 0.0F);
			this.fontRendererObj.drawString(text1, 0, 0, 16645629);
			this.fontRendererObj.drawString(text2, 0, 20, 16645629);
			this.fontRendererObj.drawString(text3, 0, 40, 16645629);
			GL11.glScalef(1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
		if (this.golem.getUpgradeAmount(5) > 0 && ItemGolemCore.canSort(this.golem.getCore())) {
			int shiftx = (this.golem.getCore() == 10) ? 66 : 180;
			int shifty = (this.golem.getCore() == 10) ? 12 : 0;
			drawTexturedModalRect(baseX + shiftx, baseY + 24 + shifty, 8, 168, 8, 8);
			String text1 = "Use Ore dictionary";
			if (this.golem.checkOreDict())
				drawTexturedModalRect(baseX + shiftx, baseY + 24 + shifty, 8, 176, 8, 8);
			drawTexturedModalRect(baseX + shiftx, baseY + 34 + shifty, 8, 168, 8, 8);
			String text2 = "Ignore item damage";
			if (this.golem.ignoreDamage())
				drawTexturedModalRect(baseX + shiftx, baseY + 34 + shifty, 8, 176, 8, 8);
			drawTexturedModalRect(baseX + shiftx, baseY + 44 + shifty, 8, 168, 8, 8);
			String text3 = "Ignore NBT values";
			if (this.golem.ignoreNBT())
				drawTexturedModalRect(baseX + shiftx, baseY + 44 + shifty, 8, 176, 8, 8);
			GL11.glPushMatrix();
			GL11.glTranslatef((baseX + shiftx + 10), (baseY + 26 + shifty), 0.0F);
			GL11.glScalef(0.5F, 0.5F, 0.0F);
			this.fontRendererObj.drawString(text1, 0, 0, this.golem.checkOreDict() ? 16645629 : 6710886);
			this.fontRendererObj.drawString(text2, 0, 20, this.golem.ignoreDamage() ? 16645629 : 6710886);
			this.fontRendererObj.drawString(text3, 0, 40, this.golem.ignoreNBT() ? 16645629 : 6710886);
			GL11.glScalef(1.0F, 1.0F, 1.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
		GL11.glDisable(3042);
		GL11.glPopMatrix();
		drawGolem(this.mc, baseX + 51, baseY + 75, 30, (baseX + 51) - this.xSize_lo, (baseY + 75 - 50) - this.ySize_lo);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
