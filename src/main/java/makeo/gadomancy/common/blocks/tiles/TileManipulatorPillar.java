package makeo.gadomancy.common.blocks.tiles;

import thaumcraft.common.tiles.TileInfusionPillar;

/**
 * This class is part of the Gadomancy Mod
 * Gadomancy is Open Source and distributed under the
 * GNU LESSER GENERAL PUBLIC LICENSE
 * for more read the LICENSE file
 * <p/>
 * Created by makeo @ 27.10.2015 13:21
 */
public class TileManipulatorPillar extends TileInfusionPillar {

    public void setOrientation(byte orientation) {
        this.orientation = orientation;
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        this.markDirty();
    }
}