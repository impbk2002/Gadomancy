package makeo.gadomancy.common.blocks.tiles;

import makeo.gadomancy.common.utils.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import thaumcraft.common.tiles.TileJarFillable;

import java.util.*;

/**
 * This class is part of the Gadomancy Mod
 * Gadomancy is Open Source and distributed under the
 * GNU LESSER GENERAL PUBLIC LICENSE
 * for more read the LICENSE file
 *
 * Created by makeo @ 14.10.2015 15:06
 */
public class TileRemoteJar extends TileJarFillable {
    public UUID networkId;

    private int count;

    private boolean registered_to_network;

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (this.count % 3 == 0 && !this.getWorldObj().isRemote && this.networkId != null && (!this.registered_to_network || this.amount < this.maxAmount)) {
            this.count = 0;

            JarNetwork network = TileRemoteJar.getNetwork(this.networkId);

            this.registered_to_network = true;
            if(!network.jars.contains(this)) {
                network.jars.add((TileJarFillable) this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord));
            }

            network.update();
        }
        this.count++;
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        this.networkId = NBTHelper.getUUID(compound, "networkId");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        if(this.networkId != null) {
            NBTHelper.setUUID(compound, "networkId", this.networkId);
        }
    }

    private static Map<UUID, JarNetwork> networks = new HashMap<UUID, JarNetwork>();

    private static class JarNetwork {
        private long lastTime;
        private List<TileJarFillable> jars = new ArrayList<TileJarFillable>();

        private void update() {
            long time = MinecraftServer.getServer().getEntityWorld().getTotalWorldTime();
            if(time > this.lastTime) {
                if(this.jars.size() > 1) {
                    Collections.sort(this.jars, new Comparator<TileJarFillable>() {
                        @Override
                        public int compare(TileJarFillable o1, TileJarFillable o2) {
                            return o2.amount - o1.amount;
                        }
                    });

                    TileJarFillable jar1 = this.jars.get(0);
                    if(!JarNetwork.isValid(jar1)) {
                        this.jars.remove(0);
                        return;
                    }

                    TileJarFillable jar2 = this.jars.get(this.jars.size() - 1);
                    if(!JarNetwork.isValid(jar2)) {
                        this.jars.remove(this.jars.size() - 1);
                        return;
                    }

                    if((jar2.amount+1) < jar1.amount && jar2.addToContainer(jar1.aspect, 1) == 0) {
                        jar1.takeFromContainer(jar1.aspect, 1);
                    }
                }
                this.lastTime = time + 3;
            }
        }

        private static boolean isValid(TileJarFillable jar) {
            return jar != null && jar.getWorldObj() != null && !jar.isInvalid()
                    && jar.getWorldObj().blockExists(jar.xCoord, jar.yCoord, jar.zCoord);
        }
    }

    private static JarNetwork getNetwork(UUID id) {
        JarNetwork network = TileRemoteJar.networks.get(id);

        if(network == null) {
            network = new JarNetwork();
            TileRemoteJar.networks.put(id, network);
        }
        return network;
    }

    public void markForUpdate() {
        this.markDirty();
        this.getWorldObj().markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }
}
