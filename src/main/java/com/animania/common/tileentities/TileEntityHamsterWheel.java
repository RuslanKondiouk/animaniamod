package com.animania.common.tileentities;

import javax.annotation.Nullable;

import com.animania.common.entities.rodents.EntityHamster;
import com.animania.config.AnimaniaConfig;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

public class TileEntityHamsterWheel extends TileEntity implements ITickable, IEnergyProvider
{

	private boolean isRunning;
	private EntityHamster hamster;
	private NBTTagCompound hamsterNBT;
	//private EnergyHandlerHamsterWheel energy;
	private int timer;
	private int energy;


	@Override
	public void update()
	{
		if(hamster == null && hamsterNBT != null)
		{
			hamster = new EntityHamster(world);
			hamster.readFromNBT(hamsterNBT);
		}

		if (hamster != null)
		{
			isRunning = true;
			this.markDirty();
		}
		else
		{
			isRunning = false;
		}

		if (isRunning)
		{
			energy += AnimaniaConfig.gameRules.hamsterWheelRFGeneration;
			timer++;

		}

		if(timer >= AnimaniaConfig.gameRules.hamsterWheelUseTime)
		{
			timer = 0;
			ejectHamster();
			isRunning = false;
			this.markDirty();

		}

		for(EnumFacing facing : EnumFacing.VALUES)
		{
			if(getSurroundingTE(facing) != null && getSurroundingTE(facing) instanceof IEnergyReceiver )
			{
				IEnergyReceiver reciever = (IEnergyReceiver) getSurroundingTE(facing);
				int recieved = reciever.receiveEnergy(facing.getOpposite(), energy, false);
				energy -= recieved;
			}
		}

	}

	public boolean isRunning()
	{
		return isRunning;
	}

	public void setRunning(boolean isRunning)
	{
		this.isRunning = isRunning;
	}

	public EntityHamster getHamster()
	{
		return hamster;
	}

	public void setHamster(EntityHamster hamster)
	{
		this.hamster = hamster;
	}

	public int getTimer()
	{
		return timer;
	}

	public void setTimer(int timer)
	{
		this.timer = timer;
	}

	public int getEnergy()
	{
		return energy;
	}

	public void ejectHamster()
	{
		if(hamster != null && !world.isRemote)
		{
			this.hamster.setPosition(this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5);
			world.spawnEntity(hamster);
			//this.hamster.setWatered(false);
			this.hamster.setFed(false);
			this.hamster = null;
			this.hamsterNBT = null;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		NBTTagCompound tag = super.writeToNBT(compound);
		tag.setInteger("energy", energy);
		tag.setBoolean("running", isRunning);
		tag.setInteger("timer", this.timer);
		NBTTagCompound hamster = new NBTTagCompound();
		tag.setTag("hamster", ((this.hamster == null) ? hamster : this.hamster.writeToNBT(hamster)));
		return tag;

	}


	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		this.energy = compound.getInteger("energy");
		NBTTagCompound hamster = compound.getCompoundTag("hamster");
		if (!hamster.equals(new NBTTagCompound()))
			this.hamsterNBT = hamster;		
		this.isRunning = compound.getBoolean("running");
		this.timer = compound.getInteger("timer");
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {

		return (oldState.getBlock() != newState.getBlock());

	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
	{
		this.readFromNBT(pkt.getNbtCompound());
		if (this.blockType != null && this.pos != null)
			this.world.notifyBlockUpdate(this.pos, this.blockType.getDefaultState(), this.blockType.getDefaultState(), 1);

	}

	@Override
	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound tagCompound = new NBTTagCompound();
		this.writeToNBT(tagCompound);
		return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag()
	{
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public int getEnergyStored(EnumFacing from)
	{
		return energy;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		return AnimaniaConfig.gameRules.hamsterWheelCapacity;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return true;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {

		energy -= maxExtract;

		return maxExtract;
	}

	private TileEntity getSurroundingTE(EnumFacing facing)
	{
		BlockPos pos = this.pos.offset(facing);
		return world.getTileEntity(pos);		
	}

}