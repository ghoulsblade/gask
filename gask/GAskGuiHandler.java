package gask;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GAskGuiHandler implements IGuiHandler {
        //returns an instance of the Container you made earlier
        @Override
        public Object getServerGuiElement(int id, EntityPlayer player, World world,int x, int y, int z) {
        		//return new GAskGui(player);
        	return null;
        }

        //returns an instance of the Gui you made earlier
        @Override
        public Object getClientGuiElement(int id, EntityPlayer player, World world,int x, int y, int z) {
    			//return new GAskGui(player);
        	return null;

        }
}
