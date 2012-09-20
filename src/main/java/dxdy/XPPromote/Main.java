package dxdy.XPPromote;

import java.util.ArrayList;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import dxdy.XPPromote.PSW.VaultWrapper;

public class Main extends JavaPlugin{

	public Main()
	{
	    mTaskID = -1;
	    mUpdateInterval = 1000;
	    mLevelLoss = -1;
	    mPS = 0;
	}

	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		System.out.println((new StringBuilder(String.valueOf(cmdName))).append("by ").append(author).append(" version ").append(version).append(" disabled.").toString());
	}
	    
	public void onEnable()
	{
		pdf = getDescription();
		name = pdf.getName();
		cmdName = (new StringBuilder("[")).append(name).append("] ").toString();
		version = pdf.getVersion();
		author = "dxdy";

		mLevelPackListener = new LevelPackListener(mLevelLoss, this);
		ReloadConfiguration();
		
		
		getServer().getPluginManager().registerEvents(mLevelPackListener, this);
		System.out.println((new StringBuilder(String.valueOf(cmdName))).append("by ").append(author).append(" version ").append(version).append(" enabled.").toString());
	}
	
	public int getPermSystem()
	{
		return mPS;
	}
	/*
	 * Gets the permission system wrapper that can be used to manipulate the player
	 */
	public VaultWrapper getPermissionWrapper(Player pPlayer, int sSystem)
	{
			VaultWrapper p = new VaultWrapper(pPlayer, mRSP);
			
				return p;
	
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) 
	{
		if (label.equalsIgnoreCase("xpp"))
		{
			if (args.length != 1)
			{
				sender.sendMessage(ChatColor.DARK_RED + "Usage: /xpp [LevelID]");
				sender.sendMessage(ChatColor.DARK_RED + "Usage: /xpp list");
				return true;
			}
			if (args[0].equalsIgnoreCase("list"))
			{
				for (LevelPack p : mUpdaterTask.mLevels)
				{
					sender.sendMessage((new StringBuilder()).append(ChatColor.GOLD).append(Integer.toString(p.mLevel)).append(ChatColor.GRAY).append(": ").append(ChatColor.GOLD).append(p.mMessage.split(System.getProperty("line.separator"))[0]).toString());
				}
				return true;
			}
			try{
			mUpdaterTask.displayDescription(sender, Integer.parseInt(args[0]));
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage(ChatColor.DARK_RED + "Usage: /xpp [LevelID]");
				return true;
			}
			return true;
		}

		return false;
	}
	    
	/* 
	 * Cancels any existing UpdaterTasks and starts them again
	 */
	void ReloadConfiguration()
	{		
		if (mTaskID != -1)
		{
			getServer().getScheduler().cancelTask(mTaskID);
			mTaskID = -1;
		}
		
		ArrayList<LevelPack> Levels = new ArrayList<LevelPack>();
		ConfigurationReader Reader = new ConfigurationReader(Levels);
		boolean ReturnValue = Reader.ReadConfiguration();
		if (ReturnValue == false)
		{
			return;
		}
		
		if (Reader.mPS == 2)
		{
			mRSP = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class).getProvider();
		}
		
		mLevelLoss = Reader.mLevelLoss;
		mUpdateInterval = Reader.mUpdateInterval;
		mPS = Reader.mPS;
		
		mLevelPackListener.mLevelLoss = mLevelLoss;
		mUpdaterTask = new UpdaterTask(this, mPS, Levels, mLevelPackListener.mListToUpdate, mLevelPackListener.mPlayersLevels);
		mTaskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, mUpdaterTask, mUpdateInterval, mUpdateInterval);
		
	}
	
	int mTaskID; // The ID of the experience Checker Task. -1 indicates that the task wasn't started.
	
	int mUpdateInterval; // The Interval between executions of the UpdaterTask
	int mLevelLoss; // The percentage that is lost on death
	
	LevelPackListener mLevelPackListener;
	UpdaterTask mUpdaterTask;
	int mPS;
	public Permission mRSP;
	public static PluginDescriptionFile pdf;
	public static String name;
	public static String cmdName;
	public static String version;
	public static String author;
}
