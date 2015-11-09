package makeo.gadomancy.common.utils.world;

import makeo.gadomancy.common.blocks.tiles.TileAdditionalEldritchPortal;
import makeo.gadomancy.common.data.ModConfig;
import makeo.gadomancy.common.utils.world.fake.FakeWorldTCGeneration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import thaumcraft.common.lib.world.dim.CellLoc;
import thaumcraft.common.lib.world.dim.MazeHandler;
import thaumcraft.common.lib.world.dim.MazeThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is part of the Gadomancy Mod
 * Gadomancy is Open Source and distributed under the
 * GNU LESSER GENERAL PUBLIC LICENSE
 * for more read the LICENSE file
 * <p/>
 * Created by HellFirePvP @ 05.11.2015 10:40
 */
public class TCMazeHandler {

    //How maze gen works overall (for us):
    //Defining what chunks are currently occupied with the 'labyrinthCopy' map.
    //If a chunk is not listed in there, it's considered empty.
    //level saving itself is disabled. Players without active session will get teleported out.

    //Our map to work with.
    public static ConcurrentHashMap<CellLoc, Short> labyrinthCopy = new ConcurrentHashMap<CellLoc, Short>();
    public static final int TELEPORT_LAYER_Y = 55;

    public static final FakeWorldTCGeneration GEN = new FakeWorldTCGeneration();

    private static List<TCMazeSession> flaggedSessions = new ArrayList<TCMazeSession>();

    private static Map<EntityPlayer, TCMazeSession> runningSessions = new HashMap<EntityPlayer, TCMazeSession>();

    public static void closeAllSessionsAndCleanup() {
        for (EntityPlayer pl : runningSessions.keySet()) {
            runningSessions.get(pl).closeSession(false);
        }
        init();
    }

    public static void tick() {
        WorldServer w = DimensionManager.getWorld(ModConfig.dimOuterId);
        if (w != null) {
            if (!w.levelSaving) w.levelSaving = true;

            WorldServer out = MinecraftServer.getServer().worldServerForDimension(0);
            List playerObjects = w.playerEntities;
            for (int i = 0; i < playerObjects.size(); i++) {
                EntityPlayer player = (EntityPlayer) playerObjects.get(i);
                if (!hasOpenSession(player)) {
                    WorldUtil.tryTeleportBack((EntityPlayerMP) player, 0);
                    ChunkCoordinates cc = out.getSpawnPoint();
                    int y = w.getTopSolidOrLiquidBlock(cc.posX, cc.posZ);
                    player.setPosition(cc.posX + 0.5, y, cc.posZ + 0.5);
                }
            }
            for (EntityPlayer player : runningSessions.keySet()) {
                if (player.worldObj.provider.dimensionId != ModConfig.dimOuterId) { //If the player left our dim, but he should still be in the session, ...
                    closeSession(player, false);
                }
            }
        }
    }

    /*
     *  Coordinates wanted here are the absolute Portal coordinates.
     */
    public static boolean createSessionWaitForTeleport(EntityPlayer player, double pX, double pY, double pZ) {
        if (hasOpenSession(player) || !hasFreeSessionSpace()) return false;
        WorldServer w = MinecraftServer.getServer().worldServerForDimension(ModConfig.dimOuterId);
        reserveSessionSpace(player);
        setupSession(player, w, pX, pY, pZ);
        return true;
    }

    private static void reserveSessionSpace(EntityPlayer player) {
        runningSessions.put(player, TCMazeSession.placeholder());
    }

    private static void setupSession(EntityPlayer player, WorldServer world, double pX, double pY, double pZ) {
        Vec3 currentPos = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
        int originDim = player.worldObj.provider.dimensionId;
        Map<CellLoc, Short> locs = calculateCellLocs(world, pX, pZ);
        MazeBuilderThread t = new MazeBuilderThread((EntityPlayerMP) player, locs, originDim, currentPos);
        t.start();
    }

    private static Map<CellLoc, Short> calculateCellLocs(WorldServer world, double pX, double pZ) {
        ConcurrentHashMap<CellLoc, Short> oldDat = MazeHandler.labyrinth;
        ConcurrentHashMap<CellLoc, Short> bufferOld = new ConcurrentHashMap<CellLoc, Short>(labyrinthCopy);
        MazeHandler.labyrinth = labyrinthCopy;
        int chX = ((int) pX) >> 4;
        int chZ = ((int) pZ) >> 4;
        int w = randWH(world.rand);
        int h = randWH(world.rand);
        while (MazeHandler.mazesInRange(chX, chZ, w, h)) {
            chX++;
        }
        MazeThread mt = new MazeThread(chX, chZ, w, h, world.rand.nextLong());
        mt.run();
        Map<CellLoc, Short> locs = calculateDifferences(bufferOld);
        labyrinthCopy = MazeHandler.labyrinth;
        MazeHandler.labyrinth = oldDat;
        return locs;
    }

    private static Map<CellLoc, Short> calculateDifferences(ConcurrentHashMap<CellLoc, Short> bufferOld) {
        ConcurrentHashMap<CellLoc, Short> newDat = MazeHandler.labyrinth; //Only the new data has data, the old one doesn't have.
        Map<CellLoc, Short> newlyEvaluatedMaze = new HashMap<CellLoc, Short>();
        for (CellLoc loc : newDat.keySet()) {
            if (!bufferOld.containsKey(loc)) {
                newlyEvaluatedMaze.put(loc, newDat.get(loc));
            }
        }
        return newlyEvaluatedMaze;
    }

    private static boolean hasFreeSessionSpace() {
        return ModConfig.maxMazeCount == -1 || runningSessions.size() < ModConfig.maxMazeCount;
    }

    private static int randWH(Random random) {
        return 17 + random.nextInt(2) * 2;
    }

    public static void init() {
        runningSessions = new HashMap<EntityPlayer, TCMazeSession>();
    }

    public static boolean hasOpenSession(EntityPlayer player) {
        return !player.worldObj.isRemote && runningSessions.get(player) != null;
    }

    public static void closeSession(EntityPlayer player, boolean teleport) {
        if (player.worldObj.isRemote) return;

        if (runningSessions.containsKey(player)) {
            runningSessions.get(player).closeSession(teleport);
            runningSessions.remove(player);
        }
        if (runningSessions.size() == 0) {
            //addToWorldUnloadQueue(ModConfig.dimOuterId);
        }
    }

    public static void free(Map<CellLoc, Short> locations) {
        if (locations == null) return;
        WorldServer ws = DimensionManager.getWorld(ModConfig.dimOuterId);
        for (CellLoc loc : locations.keySet()) {
            labyrinthCopy.remove(loc);
            forceChunkUnloading(ws, loc.x, loc.z);
        }
    }

    private static void forceChunkUnloading(WorldServer ws, int chX, int chZ) {
        if (ws == null) return;
        long chunkPair = ChunkCoordIntPair.chunkXZ2Int(chX, chZ);
        ChunkProviderServer serverChProvider = ws.theChunkProviderServer;
        LongHashMap chunks = serverChProvider.loadedChunkHashMap;
        Chunk c = (Chunk) chunks.getValueByKey(chunkPair);

        serverChProvider.loadedChunkHashMap.remove(chunkPair);
        if (c != null) {
            c.onChunkUnload();
            serverChProvider.loadedChunks.remove(c);
        }
    }

    public static void scheduleTick() {
        Iterator<TCMazeSession> it = flaggedSessions.iterator();
        while(it.hasNext()) {
            TCMazeSession s = it.next();
            it.remove();
            s.startSession();
            runningSessions.put(s.player, s);
        }
    }

    public static void flagSessionForStart(TCMazeSession session) {
        flaggedSessions.add(session);
    }

    public static class MazeBuilderThread extends Thread {

        private final EntityPlayerMP player;
        private final Map<CellLoc, Short> chunksAffected;
        private final int originDimId;
        private final Vec3 originLocation;

        public MazeBuilderThread(EntityPlayerMP player, Map<CellLoc, Short> chunksAffected, int originDimId, Vec3 originLocation) {
            this.player = player;
            this.chunksAffected = chunksAffected;
            this.originDimId = originDimId;
            this.originLocation = originLocation;
            setName("GadomancyEldritchGen (SERVER/ThreadID=" + getId() + ")");
        }

        @Override
        public void run() {
            long startMs = System.currentTimeMillis();
            for (CellLoc l : chunksAffected.keySet()) {
                ConcurrentHashMap<CellLoc, Short> old = MazeHandler.labyrinth;
                MazeHandler.labyrinth = labyrinthCopy;
                MazeHandler.generateEldritch(GEN, GEN.rand, l.x, l.z);
                MazeHandler.labyrinth = old;
            }
            System.out.println("BuildTime: " + (System.currentTimeMillis() - startMs) + " (ms)");
            System.out.println("Blocks SET: " + GEN.blockCount);
            System.out.println("Blocks OVERWRITTEN: " + GEN.blockOverwriteCount);
            System.out.println("BufferChunks CREATED: " + FakeWorldTCGeneration.ChunkBuffer.cnt);

            finishBuild();
        }

        private void finishBuild() {
            TileAdditionalEldritchPortal.informSessionStart(player);
            TCMazeSession session = new TCMazeSession(player, chunksAffected, originDimId, originLocation);
            flagSessionForStart(session);
        }

    }
}
