package vazkii.tinkerer.common.block.fire;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.MapColor;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import vazkii.tinkerer.common.registry.ITTinkererBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static net.minecraftforge.common.util.ForgeDirection.*;

public abstract class BlockFireBase extends BlockFire implements ITTinkererBlock {

	public abstract HashMap<Block, Block> getBlockTransformation();

	public boolean isTransmutationTarget(Block block) {
		return getBlockTransformation().keySet().contains(block);
	}

	public boolean isTransmutationResult(Block block) {
		return getBlockTransformation().values().contains(block);
	}

	public boolean isNeighborTarget(World w, int x, int y, int z) {
		for (ForgeDirection f : ForgeDirection.VALID_DIRECTIONS) {
			if (w.blockExists(x + f.offsetX, y + f.offsetY, z + f.offsetZ) && isTransmutationTarget(w.getBlock(x + f.offsetX, y + f.offsetY, z + f.offsetZ))) {
				return true;
			}
		}
		return false;
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
		return null;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public int getRenderType() {
		return 3;
	}

	public int quantityDropped(Random p_149745_1_) {
		return 0;
	}

	public int tickRate(World p_149738_1_) {
		return 25;
	}

	public void setBlockWithTransmutationTarget(World world, int x, int y, int z, int meta, Block block) {
		if (isTransmutationTarget(world.getBlock(x, z, y))) {
			world.setBlock(x, z, y, getBlockTransformation().get(world.getBlock(x, z, y)));
		} else {
			world.setBlock(x, z, y, block, meta, 3);
		}
	}

	public void updateTick(World world, int x, int y, int z, Random rand) {
		if (world.getGameRules().getGameRuleBooleanValue("doFireTick")) {

			if (world.isRaining() && (world.canLightningStrikeAt(x, y, z) || world.canLightningStrikeAt(x - 1, y, z) || world.canLightningStrikeAt(x + 1, y, z) || world.canLightningStrikeAt(x, y, z - 1) || world.canLightningStrikeAt(x, y, z + 1))) {
				world.setBlockToAir(x, y, z);
			} else {
				if (!isNeighborTarget(world, x, y, z)) {
					world.setBlockToAir(x, y, z);
				}
				if (rand.nextInt(20) == 0) {
					if (isNeighborTarget(world, x, y, z)) {
						for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
							if (isTransmutationTarget(world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ))) {
								world.setBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, getBlockTransformation().get(world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)));
							}
						}
					}

				}

			}
			int l = world.getBlockMetadata(x, y, z);

			if (l < 15) {
				world.setBlockMetadataWithNotify(x, y, z, l + rand.nextInt(3) / 2, 4);
			}

			world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world) + rand.nextInt(3));

			boolean flag1 = world.isBlockHighHumidity(x, y, z);
			byte b0 = 0;

			if (flag1) {
				b0 = -50;
			}

			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
				this.tryCatchFire(world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ, 300 + b0, rand, l, direction);
			}

			for (int i1 = x - 1; i1 <= x + 1; i1++) {
				for (int j1 = z - 1; j1 <= z + 1; j1++) {
					for (int k1 = y - 1; k1 <= y + 2; k1++) {
						if (i1 != x || k1 != y || j1 != z) {
							int l1 = 100;

							int i2 = this.getChanceOfNeighborsEncouragingFire(world, i1, k1, j1);

							if (i2 > 0) {
								int j2 = (i2 + 70) / (l + 30);
								j2 += 70;
								if (flag1) {
									j2 /= 2;
								}

								if (j2 > 0 && rand.nextInt(l1) <= j2 && (!world.isRaining() || !world.canLightningStrikeAt(i1, k1, j1)) && !world.canLightningStrikeAt(i1 - 1, k1, z) && !world.canLightningStrikeAt(i1 + 1, k1, j1) && !world.canLightningStrikeAt(i1, k1, j1 - 1) && !world.canLightningStrikeAt(i1, k1, j1 + 1)) {
									int k2 = l + rand.nextInt(5) / 4;

									if (k2 > 15) {
										k2 = 15;
									}
									setBlockWithTransmutationTarget(world, i1, j1, k1, k2, this);
								}
							}
						}
					}

				}
			}
		}
	}

	public boolean func_149698_L() {
		return false;
	}

	public int getBlockFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		if (isTransmutationTarget(world.getBlock(x, y, z))) {
			return 100;
		}
		if (isTransmutationResult(world.getBlock(x, y, z))) {
			return 0;
		}

		return world.getBlock(x, y, z).getFlammability(world, x, y, z, face);
	}

	private void tryCatchFire(World world, int x, int y, int z, int strength, Random rand, int meta, ForgeDirection face) {
		int j1 = getBlockFlammability(world, x, y, z, face);

		if (rand.nextInt(strength) < j1) {
			boolean flag = world.getBlock(x, y, z) == Blocks.tnt;

			if (rand.nextInt(meta + 10) < 5 && !world.canLightningStrikeAt(x, y, z)) {
				int k1 = meta + rand.nextInt(5) / 4;

				if (k1 > 15) {
					k1 = 15;
				}

				setBlockWithTransmutationTarget(world, x, y, z, k1, this);
			} else {
				setBlockWithTransmutationTarget(world, x, y, z, 0, Blocks.air);
			}

			if (flag) {
				Blocks.tnt.onBlockDestroyedByPlayer(world, x, y, z, 1);
			}
		}
	}

	/**
	 * Returns true if at least one block next to this one can burn.
	 */
	private boolean canNeighborBurn(World world, int x, int y, int z) {
		return isNeighborTarget(world, x, y, z);
	}

	/**
	 * Gets the highest chance of a neighbor block encouraging this block to catch fire
	 */
	private int getChanceOfNeighborsEncouragingFire(World world, int x, int y, int z) {
		byte b0 = 0;

		if (!world.isAirBlock(x, y, z)) {
			return 0;
		} else {
			if (isNeighborTarget(world, x, y, z)) {
				return 100;
			}
			return 0;
		}
	}

	/**
	 * Returns if this block is collidable (only used by Fire). Args: x, y, z
	 */
	public boolean isCollidable() {
		return false;
	}

	/**
	 * Checks the specified block coordinate to see if it can catch fire.  Args: blockAccess, x, y, z
	 */
	@Deprecated
	public boolean canBlockCatchFire(IBlockAccess p_149844_1_, int p_149844_2_, int p_149844_3_, int p_149844_4_) {
		return canCatchFire(p_149844_1_, p_149844_2_, p_149844_3_, p_149844_4_, UP);
	}

	@Deprecated
	public int func_149846_a(World p_149846_1_, int p_149846_2_, int p_149846_3_, int p_149846_4_, int p_149846_5_) {
		return getChanceToEncourageFire(p_149846_1_, p_149846_2_, p_149846_3_, p_149846_4_, p_149846_5_, UP);
	}

	/**
	 * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
	 */
	public boolean canPlaceBlockAt(World p_149742_1_, int p_149742_2_, int p_149742_3_, int p_149742_4_) {
		return true;
	}

	public boolean canCatchFire(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return getBlockFlammability(world, x, y, z, face) > 0;
	}

	public int getChanceToEncourageFire(IBlockAccess world, int x, int y, int z, int oldChance, ForgeDirection face) {
		int newChance = world.getBlock(x, y, z).getFireSpreadSpeed(world, x, y, z, face);

		return (newChance > oldChance ? newChance : oldChance);
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
	 * their own) Args: x, y, z, neighbor Block
	 */
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if (!World.doesBlockHaveSolidTopSurface(world, x, y - 1, z) && !this.canNeighborBurn(world, x, y, z)) {
			world.setBlockToAir(x, y, z);
		}
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	public void onBlockAdded(World world, int x, int y, int z) {
		if (world.provider.dimensionId > 0 || !Blocks.portal.func_150000_e(world, x, y, z)) {
			world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world) + world.rand.nextInt(10));
		}
	}

	/**
	 * A randomly called display update to be able to add particles or other items for display
	 */
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		if (rand.nextInt(24) == 0) {
			world.playSound((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), "fire.fire", 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
		}

		int l;
		float f;
		float f1;
		float f2;

		if (!World.doesBlockHaveSolidTopSurface(world, x, y - 1, z) && !Blocks.fire.canCatchFire(world, x, y - 1, z, UP)) {
			if (Blocks.fire.canCatchFire(world, x - 1, y, z, EAST)) {
				for (l = 0; l < 2; ++l) {
					f = (float) x + rand.nextFloat() * 0.1F;
					f1 = (float) y + rand.nextFloat();
					f2 = (float) z + rand.nextFloat();
					world.spawnParticle("largesmoke", (double) f, (double) f1, (double) f2, 0.0D, 0.0D, 0.0D);
				}
			}

			if (Blocks.fire.canCatchFire(world, x + 1, y, z, WEST)) {
				for (l = 0; l < 2; ++l) {
					f = (float) (x + 1) - rand.nextFloat() * 0.1F;
					f1 = (float) y + rand.nextFloat();
					f2 = (float) z + rand.nextFloat();
					world.spawnParticle("largesmoke", (double) f, (double) f1, (double) f2, 0.0D, 0.0D, 0.0D);
				}
			}

			if (Blocks.fire.canCatchFire(world, x, y, z - 1, SOUTH)) {
				for (l = 0; l < 2; ++l) {
					f = (float) x + rand.nextFloat();
					f1 = (float) y + rand.nextFloat();
					f2 = (float) z + rand.nextFloat() * 0.1F;
					world.spawnParticle("largesmoke", (double) f, (double) f1, (double) f2, 0.0D, 0.0D, 0.0D);
				}
			}

			if (Blocks.fire.canCatchFire(world, x, y, z + 1, NORTH)) {
				for (l = 0; l < 2; ++l) {
					f = (float) x + rand.nextFloat();
					f1 = (float) y + rand.nextFloat();
					f2 = (float) (z + 1) - rand.nextFloat() * 0.1F;
					world.spawnParticle("largesmoke", (double) f, (double) f1, (double) f2, 0.0D, 0.0D, 0.0D);
				}
			}

			if (Blocks.fire.canCatchFire(world, x, y + 1, z, DOWN)) {
				for (l = 0; l < 2; ++l) {
					f = (float) x + rand.nextFloat();
					f1 = (float) (y + 1) - rand.nextFloat() * 0.1F;
					f2 = (float) z + rand.nextFloat();
					world.spawnParticle("largesmoke", (double) f, (double) f1, (double) f2, 0.0D, 0.0D, 0.0D);
				}
			}
		} else {
			for (l = 0; l < 3; ++l) {
				f = (float) x + rand.nextFloat();
				f1 = (float) y + rand.nextFloat() * 0.5F + 0.5F;
				f2 = (float) z + rand.nextFloat();
				world.spawnParticle("largesmoke", (double) f, (double) f1, (double) f2, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	public MapColor getMapColor(int p_149728_1_) {
		return MapColor.tntColor;
	}

	@Override
	public ArrayList<Object> getSpecialParameters() {
		return null;
	}

	@Override
	public boolean shouldRegister() {
		return true;
	}

	@Override
	public boolean shouldDisplayInTab() {
		return true;
	}

	@Override
	public Class<? extends TileEntity> getTileEntity() {
		return null;
	}

	@Override
	public Class<? extends ItemBlock> getItemBlock() {
		return null;
	}

	private static class FireInfo {
		public int encouragement = 0;
		public int flammibility = 0;

		public FireInfo(int flammibility, int encouragement) {
			this.flammibility = flammibility;
			this.encouragement = encouragement;
		}
	}
}
