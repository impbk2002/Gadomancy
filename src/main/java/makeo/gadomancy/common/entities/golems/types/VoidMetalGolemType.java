package makeo.gadomancy.common.entities.golems.types;

import makeo.gadomancy.api.golems.AdditionalGolemType;
import makeo.gadomancy.common.Gadomancy;
import makeo.gadomancy.common.utils.SimpleResourceLocation;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class VoidMetalGolemType extends AdditionalGolemType {
	
    public VoidMetalGolemType() {
        super(35, 15, 0.6f, true, 3, 128, 30, 4);
    }
    @Override
    public String getUnlocalizedName() {
        return "item.ItemGolemPlacer.voidmetal";
    }

    private IIcon icon;
    @Override
    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerIcon(Gadomancy.MODID + ":golem_voidmetal");
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.icon;
    }

    @Override
    public ResourceLocation getEntityTexture() {
        return new SimpleResourceLocation("models/golem_voidmetal.png");
    }

    @Override
    public ResourceLocation getInvSlotTexture() {
        return new SimpleResourceLocation("gui/voidmetal_slot.png");
    }

}
