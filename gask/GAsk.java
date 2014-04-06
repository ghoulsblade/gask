// ask questions from config, with freetext or multiple choice answer
package gask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = GAskInfo.ID, name = GAskInfo.NAME, version = GAskInfo.VERS)
@NetworkMod(clientSideRequired=true, serverSideRequired=true, channels={GAskInfo.NET_MAIN}, packetHandler = GAskPacketHandler.class)
public class GAsk {

	// The instance of your mod that Forge uses.
	@Instance(value = GAskInfo.ID)
	public static GAsk instance;

	// Says where the client and server 'proxy' code is loaded.
	//@SidedProxy(clientSide = GAskInfo.CLIENTPROXY, serverSide = GAskInfo.COMMONPROXY)
	//public static CommonProxy proxy;

	public File configfile;
	public static boolean debug = true;
	
	public static Item item;
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		GAskUtils.debug("GAsk: serverStarting 01");
		GAskUtils.debug("GAsk: adding commands...");
		event.registerServerCommand(new GAskCommand());
		GAskUtils.debug("GAsk: serverStarting 02");
		GAskUtils.debug("GAsk: serverStarting 03");

	}

	@EventHandler // used in 1.6.2
	//@PreInit    // used in 1.5.2
	public void preInit(FMLPreInitializationEvent event) {
		GAskUtils.debug("GAsk: preInit01");
		configfile = event.getSuggestedConfigurationFile();

		loadConfig();
	}

	
	/// question groups

	List<GAskQuestionGroup> mQuestionGroups = new ArrayList<GAskQuestionGroup>();
	public Map<String, GAskQuestionGroup> mQuestionGroupsByName = new HashMap<String, GAskQuestionGroup>();

	static public class GAskQuestionGroup {
		int groupid;
		int number_of_questions = 0;
		List<GAskQuestion> questions = new ArrayList<GAskQuestion>();
		GAskQuestionGroup(int _groupid,String name,File groupconfigfile)
		{
			groupid = _groupid;
			GAskUtils.debug("GAskQuestionGroup : loading from file: "+groupconfigfile.getAbsolutePath());
			Configuration config = new Configuration(groupconfigfile);

			// loading the configuration from its file
			config.load();
			String cat = Configuration.CATEGORY_GENERAL;

			number_of_questions = config.get(cat, "number_of_questions", number_of_questions).getInt();
			questions.clear();
			

			if (number_of_questions == 0)
			{
				GAskUtils.debug("GAsk no questions found in config, adding example question");
				// dummy for testing
				GAskQuestion o = new GAskQuestion("Hello, what's your name?");
				questions.add(o);
				o.answers = new String[] {"Steve","Mario","Luigi","Freddy"};
				o.answers_freetext = new String[] {"Frankenstein"};

				config.get(cat, "question0", "Hello, what's your name?").getString();
				config.get(cat, "answer0", o.answers);
				config.get(cat, "answer0freetext", o.answers_freetext);
			}

			
			for (int i=1;i<=number_of_questions;++i)
			{
				String q = config.get(cat, "question"+i, "1+1=?").getString();
				GAskQuestion o = new GAskQuestion(q);
				questions.add(o);
				o.answers = config.get(cat, "answer"+i, new String[] {}).getStringList();
				o.answers_freetext = config.get(cat, "answer"+i+"freetext", new String[] {}).getStringList();
			}

			GAskUtils.debug("GAsk loaded questions: "+number_of_questions+" in group "+name);

			// saving the configuration to its file
			// config.save();   do not save, readonly, avoid mixing up order
		}

		public GAskQuestion getQuestionByID (int iQuestionID)
		{
			return questions.get(iQuestionID); 
		}
	}
	
	static public class GAskQuestion {
		public String q;
		public String[] answers;
		public String[] answers_freetext;
		public GAskQuestion (String _q) { q = _q; }
	};

	public GAskQuestionGroup getQuestionGroupByID (int iQuestionGroupID)
	{
		return mQuestionGroups.get(iQuestionGroupID);
	}
	
	public GAskQuestionGroup getQuestionGroupByName (String groupname)
	{
		return mQuestionGroupsByName.get(groupname);
	}

	public int getRandomQuestionID (String groupname)
	{
		GAskQuestionGroup group = mQuestionGroupsByName.get(groupname);
		if (group == null) return 0;
		Random rand = new Random();
    	return rand.nextInt(group.questions.size());
	}
	
	public void loadConfig() {
		GAskUtils.debug("GAsk: loadConfig01");
		// you will be able to find the config file in .minecraft/config/ and it will be named Dummy.cfg
		// here our Configuration has been instantiated, and saved under the name "config"
		Configuration config = new Configuration(configfile);

		// loading the configuration from its file
		config.load();

		FMLLog.info("GAsk: loadConfig02");
		

		String cat = Configuration.CATEGORY_GENERAL;
		debug = config.get(cat, "debug", debug).getBoolean(debug);
		int i;
		
		int number_of_groups = config.get(cat, "number_of_groups", 0).getInt();
		for (i=1;i<=number_of_groups;++i)
		{
			String name = config.get(cat, "group"+i, "unknown").getString();
			// configfile
			File groupconfigfile = new File(configfile.getParent(),name+".cfg");
			int groupid = mQuestionGroups.size();
			GAskQuestionGroup o = new GAskQuestionGroup(groupid,name,groupconfigfile);
			mQuestionGroups.add(o);
			mQuestionGroupsByName.put(name,o);
		}
		
		

		/*
		int[] arr = config.get(cat, "blacklist", new int[] {}).getIntList();
		g.BlackListClear();
		for (int i=0;i<arr.length;++i) g.BlackListAdd(arr[i]);
	*/
		

		GAskUtils.debug("GAsk: loadConfig03");

		// saving the configuration to its file
		config.save();
		GAskUtils.debug("GAsk: loadConfig04");
	}

	// EventManager eventmanager = new EventManager();

	@EventHandler // used in 1.6.2
	//@Init       // used in 1.5.2
	public void load(FMLInitializationEvent event) {
		GAskUtils.debug("GAsk: load 01");
		//proxy.registerTickHandler();
		GAskUtils.debug("GAsk: load 2");

		MinecraftForge.EVENT_BUS.register(this);
		GAskUtils.debug("GAsk: load 3");

        //"this" is an instance of the @Mod
        NetworkRegistry.instance().registerGuiHandler(this, new GAskGuiHandler());
		
		item = new GAskItem(21400);
		LanguageRegistry.addName(item, "GAskItem");

	}

	@EventHandler // used in 1.6.2
	//@PostInit   // used in 1.5.2
	public void postInit(FMLPostInitializationEvent event) {
		// Stub Method

	}



}
