package makeo.gadomancy.common.entities.golems.types;

import makeo.gadomancy.api.golems.AdditionalGolemType;
import makeo.gadomancy.common.Gadomancy;
import makeo.gadomancy.common.utils.SimpleResourceLocation;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class GoldGolemType extends AdditionalGolemType {
	
    public GoldGolemType() {
        super(120, 10, 0.2f, true, 5, 1, 30, 4);
    }
    @Override
    public String getUnlocalizedName() {
        return "item.ItemGolemPlacer.gold";
    }

    private IIcon icon;
    @Override
    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerIcon(Gadomancy.MODID + ":golem_gold");
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.icon;
    }

    @Override
    public ResourceLocation getEntityTexture() {
        return new SimpleResourceLocation("models/golem_gold.png");
    }

    @Override
    public ResourceLocation getInvSlotTexture() {
        return new SimpleResourceLocation("gui/gold_slot.png");
    }
}
