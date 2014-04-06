package gask;


import gask.GAsk.GAskQuestion;
import gask.GAsk.GAskQuestionGroup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GAskPacketHandler implements IPacketHandler {
	public enum Command
	{
	    AskChunks_S2C_Question,
	    AskChunks_C2S_Answer,
	};
    private static final Command[] myCommandFromInt = Command.values();
    public static Command CommandFromInt (int i) { return myCommandFromInt[i]; }


    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
    	if (packet.channel == null) return;
    	if (packet.data == null) return;
        if (packet.channel.equals(GAskInfo.NET_MAIN)) {
            DataInputStream is = new DataInputStream(new ByteArrayInputStream(packet.data));
            try {
            	int cmd = is.readShort();
            	if (cmd == Command.AskChunks_C2S_Answer.ordinal())		Handle_C2S_Answer(manager,packet,player,is);
            	if (cmd == Command.AskChunks_S2C_Question.ordinal())	Handle_S2C_Question(manager,packet,player,is);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
    

    // ---------------------- receive
    
    public void Handle_C2S_Answer	(INetworkManager manager, Packet250CustomPayload packet, Player player,DataInputStream is) throws IOException
    {
    	if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) return;

    	int iQuestionGroupID = is.readShort();
    	int iQuestionID = is.readShort();
    	
    	GAskQuestionGroup group = GAsk.instance.getQuestionGroupByID(iQuestionGroupID);
    	if (group == null) return; // invalid group
    	
    	GAskQuestion o = group.getQuestionByID(iQuestionID);
    	if (o == null) return; // invalid question
    	
    	int iAnswerId = is.readShort();
    	String sAnswer = is.readUTF();
    	
    	boolean bCorrect = false;
    	if (o.answers.length > 0 && iAnswerId == 0) bCorrect = true;
    	String aLow = sAnswer.toLowerCase().trim();
    	for(String s : o.answers_freetext) if (s.toLowerCase().equals(aLow)) bCorrect = true;

        EntityPlayerMP p = (EntityPlayerMP) player;
        int qleft = group.questions.size() - (iQuestionID + 1);
    	if (bCorrect)
    	{
    		GAskUtils.sendChat(p, "the answer was correct! "+qleft+" more question(s)");
    	} else
    	{
    		GAskUtils.sendChat(p, "the answer was wrong! "+qleft+" more question(s)");
    	}
    	
    	// send next question in group
    	iQuestionID += 1;
    	if (group.questions.size() > iQuestionID)
    	{
        	o = group.getQuestionByID(iQuestionID);
    		GAskPacketHandler.SendToPlayer_Question(p.playerNetServerHandler,group,iQuestionID,o);
    	}
    }

    public void Handle_S2C_Question	(INetworkManager manager, Packet250CustomPayload packet, Player player,DataInputStream is) throws IOException
    {
    	if (FMLCommonHandler.instance().getEffectiveSide() != Side.CLIENT) return;
    	Client_Handle_Question(manager, packet, player,is);
    }

    @SideOnly(Side.CLIENT)
    public void Client_Handle_Question	(INetworkManager manager, Packet250CustomPayload packet, Player player,DataInputStream is) throws IOException
    {
    	GAskQuestion o = new GAskQuestion("unknown");
    	int iGroupID = is.readShort();
    	int iQuestionID = is.readShort();
    	
    	o.q = is.readUTF();
    	
    	int len,i;

    	len = is.readShort();
    	o.answers = new String[len];
    	for (i=0;i<len;++i) o.answers[i] = is.readUTF();

        FMLLog.info("Client_Handle_Question %s %d",o.q,o.answers.length);

		Minecraft mc = FMLClientHandler.instance().getClient();
    	mc.displayGuiScreen(new GAskGui(iGroupID,iQuestionID,o));
    }

    
    // ---------------------- send
    
    public static void SendToPlayer_Question (NetServerHandler player,GAskQuestionGroup group,int iQuestionID,GAskQuestion o)
    {
		if (o == null) return;

		try {
			int minest = 2 + 2+2 + 2+o.q.length() + 2+40*o.answers.length + 2+40*o.answers_freetext.length;
			ByteArrayOutputStream bos = new ByteArrayOutputStream(minest); // just a minimal estimate
			DataOutputStream os = new DataOutputStream(bos);

	        os.writeShort(Command.AskChunks_S2C_Question.ordinal());

	        os.writeShort(group.groupid);
	        os.writeShort(iQuestionID);

			os.writeUTF(o.q);

			os.writeShort(o.answers.length);
			for(String s : o.answers) os.writeUTF(s);
		 	
	        FMLLog.info("SendToPlayer_Question %s %d",o.q,o.answers.length);
			
			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = GAskInfo.NET_MAIN;
			packet.data = bos.toByteArray();
			packet.length = bos.size();
			player.sendPacketToPlayer(packet);
			// PacketDispatcher.sendPacketToPlayer(packet,player);
		} catch (Exception ex) {
	        ex.printStackTrace();
		}
    }
    
    public static void SendToServer_Answer (int iGroupID,int iQuestionID,int iAnswerID,String sAnswer)
    {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(2*2+2+sAnswer.length());
		DataOutputStream outputStream = new DataOutputStream(bos);
		
		try {
	        outputStream.writeShort(Command.AskChunks_C2S_Answer.ordinal());
	        outputStream.writeShort(iGroupID);
	        outputStream.writeShort(iQuestionID);
	        outputStream.writeShort(iAnswerID);
	        outputStream.writeUTF(sAnswer);
		} catch (Exception ex) {
	        ex.printStackTrace();
		}
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = GAskInfo.NET_MAIN;
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		PacketDispatcher.sendPacketToServer(packet);
    }
    
    
    // ---------------------- utils
    
}
