package gask;


import gask.GAsk.GAskQuestion;
import gask.GAsk.GAskQuestionGroup;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

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
		GAskUtils.debug("GAskCommand");

		String groupname;
		String playername;

		// params
		
		if ( par2ArrayOfStr.length < 1 ) 
		{
			GAskUtils.chatMessage(sender, "usage: /"+getCommandName()+" GROUPNAME [PLAYERNAME]");
			return;
		}
		
		if ( par2ArrayOfStr.length >= 2 ) 
		{
			groupname = par2ArrayOfStr[0];
			playername = par2ArrayOfStr[1];
		} 
		else
		{
			groupname = par2ArrayOfStr[0];
			playername = sender.getCommandSenderName();
		}
		

		//EntityPlayerMP p = GAskUtils.getPlayerByCmdSender(sender); // par1ICommandSender.getCommandSenderName()
		EntityPlayerMP p = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(playername);
		if (p == null)
		{
			GAskUtils.chatMessage(sender, "player not found: "+playername);
			return;
		}

		GAskQuestionGroup group = GAsk.instance.getQuestionGroupByName(groupname);
		if (group == null)
		{
			GAskUtils.chatMessage(sender, "group not found: "+groupname);
			return;
		}
		
		
		/// TODO:  /gask frizzleandpop math   : trigger first question from math.cfg , ask them all in order, then print num_right/num_wrong

		//int iQuestionID=GAsk.instance.getRandomQuestionID();
		int iQuestionID=0;
		GAskQuestion q = group.getQuestionByID(iQuestionID);

		if (q == null)
		{
			GAskUtils.chatMessage(sender, "no question in group: "+groupname);
			return;
		}
		
		GAskUtils.chatMessage(sender, "sending questions to player...");
		GAskPacketHandler.SendToPlayer_Question(p.playerNetServerHandler,group,iQuestionID,q);

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
