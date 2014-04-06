package gask;


import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet29DestroyEntity;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.Player;

/**
 * Created by ghoul on 02.03.14.
 */
public class GAskCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "gask";
	}

	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender) {
		return "commands.gask.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] par2ArrayOfStr) {
		GAskUtils.debug("CLagCommandInfo");

		EntityPlayerMP p = GAskUtils.getPlayerByCmdSender(sender); // par1ICommandSender.getCommandSenderName()
		WorldServer w = p.getServerForPlayer();

		int iQuestionID=GAsk.instance.getRandomQuestionID();
		GAskUtils.chatMessage(sender, "sending question to player...");
		GAskPacketHandler.SendToPlayer_Question(p.playerNetServerHandler,iQuestionID,GAsk.instance.GetQuestionByID(iQuestionID));

		//GAskUtils.chatMessage(sender, "use the GAskItem");

		/*
		GAskUtils.chatMessage(sender, "opening gask...");
		//new GAskGui(p);
		
		// todo : this should happen on client
		Minecraft mc = FMLClientHandler.instance().getClient();
    	mc.displayGuiScreen(new GAskGui(p));
    	*/
    
		/*

		int x = (int)p.posX;
		int y = (int)p.posY;
		int z = (int)p.posZ;
		p.openGui(GAsk.instance, 0, w, x, y, z);*/
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}
}
