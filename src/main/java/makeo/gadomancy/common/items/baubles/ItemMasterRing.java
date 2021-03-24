package makeo.gadomancy.common.items.baubles;

import java.util.List;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import makeo.gadomancy.common.Gadomancy;
import makeo.gadomancy.common.registration.RegisteredItems;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.items.wands.ItemWandCasting;

public class ItemMasterRing extends Item implements IBauble, IVisDiscountGear, IRunicArmor
{
    public IIcon[] icon;
    
    public ItemMasterRing() {
        this.icon = new IIcon[7];
        this.maxStackSize = 1;
        this.canRepair = false;
        this.setMaxDamage(0);
        this.setCreativeTab(RegisteredItems.creativeTab);
        this.setHasSubtypes(true);
        this.setUnlocalizedName("ItemMasterRing");
    }
    
    @SideOnly(Side.CLIENT)
    public void registerIcons(final IIconRegister ir) {
        this.icon[0] = ir.registerIcon(Gadomancy.MODID + ":master_ignis_ring");
        this.icon[1] = ir.registerIcon(Gadomancy.MODID + ":master_aqua_ring");
        this.icon[2] = ir.registerIcon(Gadomancy.MODID + ":master_terra_ring");
        this.icon[3] = ir.registerIcon(Gadomancy.MODID + ":master_aer_ring");
        this.icon[4] = ir.registerIcon(Gadomancy.MODID + ":master_ordo_ring");
        this.icon[5] = ir.registerIcon(Gadomancy.MODID + ":master_perditio_ring");
        this.icon[6] = ir.registerIcon(Gadomancy.MODID + ":master_one_ring");
    }
    
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(final int metadata) {
        if (metadata < 7) {
            return this.icon[metadata];
        }
        return null;
    }
    
    public String getUnlocalizedName(final ItemStack itemstack) {
        return super.getUnlocalizedName() + "." + itemstack.getItemDamage();
    }
    
    @SideOnly(Side.CLIENT)
    public void getSubItems(final Item item, final CreativeTabs creativeTab, final List list) {
        for (int c = 0; c < 7; ++c) {
            list.add(new ItemStack((Item)this, 1, c));
        }
    }
    
    public BaubleType getBaubleType(final ItemStack itemstack) {
        if (itemstack.getItemDamage() < 7) {
            return BaubleType.RING;
        }
        return BaubleType.RING;
    }
    
    public void onWornTick(final ItemStack itemstack, final EntityLivingBase player) {
        final int metadata = itemstack.getItemDamage();
        Aspect toCharge = null;
        if (metadata == 0) {
            toCharge = Aspect.FIRE;
        }
        if (metadata == 1) {
            toCharge = Aspect.WATER;
        }
        if (metadata == 2) {
            toCharge = Aspect.EARTH;
        }
        if (metadata == 3) {
            toCharge = Aspect.AIR;
        }
        if (metadata == 4) {
            toCharge = Aspect.ORDER;
        }
        if (metadata == 5) {
            toCharge = Aspect.ENTROPY;
        }
        if (toCharge != null && !player.worldObj.isRemote && player.ticksExisted % 20 == 0 && player instanceof EntityPlayer) {
            for (int i = 0; i < ((EntityPlayer)player).inventory.getSizeInventory(); ++i) {
                final ItemStack curStack = ((EntityPlayer)player).inventory.getStackInSlot(i);
                if (curStack != null && curStack.getItem() instanceof ItemWandCasting) {
                    final ItemWandCasting wand = (ItemWandCasting) curStack.getItem();
                    //int amt = Math.min(player.worldObj.rand.nextInt(10), ThaumcraftBridge.getItemWandCasting_getMethodgetGetMaxVis(wand, curStack) - ThaumcraftBridge.getItemWandCasting_getMethodgetGetVis(wand, curStack, toCharge));
                    int amt = Math.min(player.worldObj.rand.nextInt(10), wand.getMaxVis(curStack) - wand.getVis(curStack, toCharge));
                    amt += wand.getVis(curStack, toCharge);
                    //ThaumcraftBridge.getItemWandCasting_getMethodStoreVis(wand, curStack, toCharge, amt);
                    wand.storeVis(player.getHeldItem(), toCharge, wand.getVis(curStack, toCharge) + amt);
                }
            }
        }
        if (metadata == 6 && !player.worldObj.isRemote && player.ticksExisted % 10 == 0 && player instanceof EntityPlayer) {
            for (int i = 0; i < ((EntityPlayer)player).inventory.getSizeInventory(); ++i) {
                final ItemStack curStack = ((EntityPlayer)player).inventory.getStackInSlot(i);
                if (curStack != null && curStack.getItem() instanceof ItemWandCasting) {
                    final ItemWandCasting wand = (ItemWandCasting) curStack.getItem();
                    final AspectList al = wand.getAspectsWithRoom(curStack);
                    if (al != null) {
                        for (final Aspect aspect : al.getAspects()) {
                            if (aspect != null && player.worldObj.rand.nextInt(6) == 1) {
                                int amt2 = Math.min(player.worldObj.rand.nextInt(10), wand.getMaxVis(curStack) - wand.getVis(curStack, aspect));
                                amt2 += wand.getVis(curStack, aspect);
                                wand.storeVis(curStack, aspect, amt2);
                            }
                        }
                    }
                }
            }
        }
    }

    
    public int getVisDiscount(final ItemStack stack, final EntityPlayer player, final Aspect aspect) {
        final int metadata = stack.getItemDamage();
        switch (metadata) {
            case 0: {
                return Aspect.FIRE.equals(aspect) ? 4 : 0;
            }
            case 1: {
                return Aspect.WATER.equals(aspect) ? 4 : 0;
            }
            case 2: {
                return Aspect.EARTH.equals(aspect) ? 4 : 0;
            }
            case 3: {
                return Aspect.AIR.equals(aspect) ? 4 : 0;
            }
            case 4: {
                return Aspect.ORDER.equals(aspect) ? 4 : 0;
            }
            case 5: {
                return Aspect.ENTROPY.equals(aspect) ? 4 : 0;
            }
            case 6: {
                return 3;
            }
            default: {
                return 0;
            }
        }
    }
    
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List list, final boolean par4) {
        final int metadata = stack.getItemDamage();
        switch (metadata) {
            case 0: {
                list.add(EnumChatFormatting.RED + "Ignis " + StatCollector.translateToLocal(Gadomancy.MODID + ".discount") + ": 4%");
                break;
            }
            case 1: {
                list.add(EnumChatFormatting.BLUE + "Aqua " + StatCollector.translateToLocal(Gadomancy.MODID + ".discount") + ": 4%");
                break;
            }
            case 2: {
                list.add(EnumChatFormatting.GREEN + "Terra " + StatCollector.translateToLocal(Gadomancy.MODID + ".discount") + ": 4%");
                break;
            }
            case 3: {
                list.add(EnumChatFormatting.YELLOW + "Aer " + StatCollector.translateToLocal(Gadomancy.MODID + ".discount") + ": 4%");
                break;
            }
            case 4: {
                list.add(EnumChatFormatting.WHITE + "Ordo " + StatCollector.translateToLocal(Gadomancy.MODID + ".discount") + ": 4%");
                break;
            }
            case 5: {
                list.add(EnumChatFormatting.GRAY + "Perditio " + StatCollector.translateToLocal(Gadomancy.MODID + ".discount") + ": 4%");
                break;
            }
            case 6: {
                list.add(EnumChatFormatting.DARK_PURPLE + "Vis " + StatCollector.translateToLocal(Gadomancy.MODID + ".discount") + ": 3%");
                break;
            }
        }
    }
    
    public boolean canEquip(final ItemStack itemstack, final EntityLivingBase player) {
        return true;
    }
    
    public boolean canUnequip(final ItemStack itemstack, final EntityLivingBase player) {
        return true;
    }
    
    public int getRunicCharge(final ItemStack itemstack) {
        if (itemstack.getItemDamage() == 6) {
            return 4;
        }
        return 0;
    }

	@Override
	public void onEquipped(ItemStack arg0, EntityLivingBase arg1) {
		Thaumcraft.instance.runicEventHandler.isDirty = true;
	}

	@Override
	public void onUnequipped(ItemStack arg0, EntityLivingBase arg1) {
		Thaumcraft.instance.runicEventHandler.isDirty = true;
	}
}