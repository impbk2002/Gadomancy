package makeo.gadomancy.common.entities.golems.types;

import makeo.gadomancy.api.golems.AdditionalGolemType;
import makeo.gadomancy.common.Gadomancy;
import makeo.gadomancy.common.utils.SimpleResourceLocation;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class EmeraldGolemType extends AdditionalGolemType {
	
    public EmeraldGolemType() {
        super(60, 60, 0.33f, true, 4, 64, 75, 8);
    }
    @Override
    public String getUnlocalizedName() {
        return "item.ItemGolemPlacer.emerald";
    }

    private IIcon icon;
    @Override
    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerIcon(Gadomancy.MODID + ":golem_emerald");
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.icon;
    }

    @Override
    public ResourceLocation getEntityTexture() {
        return new SimpleResourceLocation("models/golem_emerald.png");
    }

    @Override
    public ResourceLocation getInvSlotTexture() {
        return new SimpleResourceLocation("gui/emerald_slot.png");
    }
}
