package net.samagames.partygames.minigames.blockdodger.tasks;

import net.samagames.partygames.game.PartyGamesPlayer;
import net.samagames.partygames.minigames.blockdodger.BlockDodger;
import net.samagames.partygames.minigames.blockdodger.BlockDodgerRoom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class BlockDodgerTask extends BukkitRunnable {

    private BlockDodger miniGame;

    public BlockDodgerTask(BlockDodger miniGame) {
        this.miniGame = miniGame;
    }

    @Override
    public void run() {
        if(!isThereActiveRooms()) {
            miniGame.endGame();
            return;
        }

        if(miniGame.getTimer().getTime() <= 0) {
            miniGame.endGame();
            return;
        }

        for(PartyGamesPlayer player : miniGame.getRooms().keySet()) {
            BlockDodgerRoom room = miniGame.getRooms().get(player);

            if(room.isActive()) {
                handleRoom(room, player);
            }
        }
    }

    private boolean isThereActiveRooms() {
        for(BlockDodgerRoom room : miniGame.getRooms().values()) {
            if(room.isActive())
                return true;
        }
        return false;
    }

    private void handleRoom(BlockDodgerRoom room, PartyGamesPlayer player) {
        for (Block oldBlock : room.getMovingBlocks().toArray(new Block[room.getMovingBlocks().size()])) {
            Block newBlock;

            if (room.getColumnAxis().equals(BlockDodgerRoom.ColumnAxis.X_AXIS)) {
                if (room.getBlockPos1().getBlockX() < room.getBlockPos2().getBlockX()) {
                    newBlock = oldBlock.getRelative(1, 0, 0);
                } else {
                    newBlock = oldBlock.getRelative(-1, 0, 0);
                }
            } else {
                if (room.getBlockPos1().getBlockZ() < room.getBlockPos2().getBlockZ()) {
                    newBlock = oldBlock.getRelative(0, 0, 1);
                } else {
                    newBlock = oldBlock.getRelative(0, 0, -1);
                }
            }

            if (isBlockInsideBlocksArea(newBlock, room)) {
                newBlock.setType(Material.STEP);
                room.getMovingBlocks().add(newBlock);
            }

            if (newBlock.getLocation().equals(player.getPlayerIfOnline().getLocation().getBlock().getLocation())) {
                int points = miniGame.getTimer().getInitialTime() - miniGame.getTimer().getTime();
                points /= 0.3; // Make points amount with 100 as maximal instead of 30

                player.givePoints(points);
                room.setActive(false);
                room.clearBlocks();

                Bukkit.broadcastMessage(ChatColor.RED + player.getPlayerData().getEffectiveName() +
                        " a été heurté par un bloc.");
                player.getPlayerIfOnline().sendMessage(ChatColor.GOLD + "+ " + points + " points");
                return;
            }

            oldBlock.setType(Material.AIR);
            room.getMovingBlocks().remove(oldBlock);
        }

        int y = room.getBlockPos1().getBlockY();
        int x, z;
        if (room.getColumnAxis().equals(BlockDodgerRoom.ColumnAxis.X_AXIS)) {
            x = room.getBlockPos1().getBlockZ();
            if (room.getBlockPos1().getBlockZ() < room.getBlockPos2().getBlockZ()) {
                z = ThreadLocalRandom.current().nextInt(room.getBlockPos1().getBlockZ(),
                        room.getBlockPos2().getBlockZ());
            } else {
                z = ThreadLocalRandom.current().nextInt(room.getBlockPos2().getBlockZ(),
                        room.getBlockPos1().getBlockZ());
            }
        } else {
            z = room.getBlockPos1().getBlockZ();
            if (room.getBlockPos1().getBlockX() < room.getBlockPos2().getBlockX()) {
                x = ThreadLocalRandom.current().nextInt(room.getBlockPos1().getBlockX(),
                        room.getBlockPos2().getBlockX());
            } else {
                x = ThreadLocalRandom.current().nextInt(room.getBlockPos2().getBlockX(),
                        room.getBlockPos1().getBlockX());
            }
        }

        Block block = new Location(room.getBlockPos1().getWorld(), x, y, z).getBlock();
        block.setType(Material.STEP);
        room.getMovingBlocks().add(block);
    }

    private boolean isBlockInsideBlocksArea(Block b, BlockDodgerRoom room) {
        if(b.getY() != room.getBlockPos1().getBlockY())
            return false;

        if(room.getBlockPos1().getBlockX() < room.getBlockPos2().getBlockX()) {
            if(b.getX() < room.getBlockPos1().getBlockX() ||
                    b.getX() > room.getBlockPos2().getBlockX())
                return false;
        } else {
            if(b.getX() > room.getBlockPos1().getBlockX() ||
                    b.getX() < room.getBlockPos2().getBlockX())
                return false;
        }

        if(room.getBlockPos1().getBlockZ() < room.getBlockPos2().getBlockZ()) {
            if(b.getZ() < room.getBlockPos1().getBlockZ() ||
                    b.getZ() > room.getBlockPos2().getBlockZ())
                return false;
        } else {
            if(b.getZ() > room.getBlockPos1().getBlockZ() ||
                    b.getZ() < room.getBlockPos2().getBlockZ())
                return false;
        }

        return true;
    }
}