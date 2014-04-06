package gask;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.world.World;

public class GAskItem extends Item 
{
	public GAskItem (int i) 
	{ 
		super(i); 
		setMaxStackSize(16);
		setCreativeTab(CreativeTabs.tabMisc);
		setUnlocalizedName("GAskItem");
	}

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
    	// mc.displayGuiScreen(new GAskGui(par3EntityPlayer));
        return par1ItemStack;
    }
    
    public Minecraft mc = ModLoader.getMinecraftInstance();
}
