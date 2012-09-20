package dxdy.XPPromote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dxdy.XPPromote.PSW.VaultWrapper;


/* 
 * Updater Task
 * Recalculates the queued players permissions based on their experience level.
 */
public class UpdaterTask implements Runnable{
	// Constructor needs to be provided with the permission-level table
	public UpdaterTask(Main Plugin, int System, ArrayList<LevelPack> Levels, ArrayList<String> Players, HashMap<String, Integer> PlayersLevels)
	{
		mPlugin = Plugin;
		mSystem = System;
		mLevels = Levels;
		mPlayers = Players;
		mPlayersLevels = PlayersLevels;
		mTemporaryMessageCalc = new ArrayList<LevelPack>();
	}
	
	
	@Override
	public void run() {
		long TimeStart = System.nanoTime();
		//		
		Logger.getLogger("Minecraft").info("[XPP] Checking permissions");
		
		// Get players in queue
		for (String PlayerName : mPlayers)
		{
			// Get the player instance associated with that name
			Player Player = Bukkit.getPlayer(PlayerName);
			if (Player == null)
			{
				//Logger.getLogger("Minecraft").info("[XPP] Couln't get player instance for " + PlayerName + ". Presumably he is offline. Permissions will be added the next time he logs in.");
				continue;
			}

			
			int ExpLevel = Player.getLevel();
			
			// Check the difference
			int OldLevel = mPlayersLevels.get(Player.getName());
			int DiffLevel = ExpLevel - OldLevel;
			//if (DiffLevel == 0) // TODO: PERFORMANCE POTIENTIAL: ONLY CHECK WHEN LEVEL ACTUALLY CHANGED. PAY ATTENTION TO LOGIN
			//	continue; // Nothing to be done if the level equals the old one
			boolean DiffPositive = DiffLevel > 0 ? true : false;

			// as well as the permissions ex user
			VaultWrapper PEXPlayer = mPlugin.getPermissionWrapper(Player, mSystem);

			if (PEXPlayer == null)
			{
				//Logger.getLogger("Minecraft").info("[XPP] Couln't get PEX player instance for " + PlayerName + ". Presumably he is offline. Permissions will be added the next time he logs in.");
				continue;
			}

			// Clear the TMP
			mTemporaryMessageCalc.clear();
			if (PEXPlayer.hasPermission("xppromote.bypass"))
				continue;

			long TimeStarts = System.nanoTime();
			// Loop trough the levels
			for (LevelPack LP : mLevels)
			{
				// Manage permissions
				for (PermissionNode PermissionN : LP.mPermissionNodes)
				{
					// Check whether the player has the permission
					boolean PlayerHasPermission = PEXPlayer.hasPermission(PermissionN.mNode);

					// Normal permission nodes
					if (PermissionN.mType == PermissionNodeType.PNT_NORMAL)
					{
						// If the player hasn't got the level and has the permission it should be removed
						if(ExpLevel < LP.mLevel && PlayerHasPermission)
						{
							PEXPlayer.removePermission(PermissionN.mNode);
						}
						// If the player has got the level and hasn't got the permission it should be added
						if(ExpLevel >= LP.mLevel && !PlayerHasPermission)
							PEXPlayer.addPermission(PermissionN.mNode);


					}
					else if (PermissionN.mType == PermissionNodeType.PNT_PERSIST)
					{
						// If the player has got the level and hasn't got the permission, grant it
						if(ExpLevel >= LP.mLevel && !PlayerHasPermission)
							PEXPlayer.addPermission(PermissionN.mNode);
						// But never remove it
					}
					else if(PermissionN.mType == PermissionNodeType.PNT_STRIP)
					{
						// If the player hasn't got the level and hasn't got the permission grant it
						if(ExpLevel < LP.mLevel && !PlayerHasPermission)
							PEXPlayer.addPermission(PermissionN.mNode);
						// If the player has got the level and still has the permission remove it
						if(ExpLevel >= LP.mLevel && PlayerHasPermission)
							PEXPlayer.removePermission(PermissionN.mNode);
					}
					

				}
				// Manage groups
				if (!LP.mGroup.isEmpty())
				{
					if (ExpLevel >= LP.mLevel && !PEXPlayer.isInGroup(LP.mGroup))
						PEXPlayer.addGroup(LP.mGroup);
					else if (ExpLevel < LP.mLevel && PEXPlayer.isInGroup(LP.mGroup))
						PEXPlayer.removeGroup(LP.mGroup);
				}
				
				// And check whether this level pack is message relevant
				if (DiffPositive && LP.mLevel > OldLevel && LP.mLevel <= ExpLevel)
					mTemporaryMessageCalc.add(LP);
				else if(!DiffPositive && LP.mLevel <= OldLevel && LP.mLevel > ExpLevel)
					mTemporaryMessageCalc.add(LP);
			}	//

			double Valu = (double)(System.nanoTime() - TimeStarts)/1000000000D;
			String st2 =Double.toString(100D*(Valu/0.05D));
			String st1 =  Double.toString(Valu * 1000D);
			Logger.getLogger("Minecraft").info("[XPP] Checkingwewewee permissions done. It took " + st1.substring(0, st1.length() > 7? 7:st1.length()) + " ms, which is " + st2.substring(0, st2.length() > 4? 4:st2.length()) + " percent of the time affo20tps");
		
			
			// You have been .... the following levels due to your		
			String MessageWord = DiffPositive ? "granted" : "deprived of";

			
			// A single level or many?
			if (mTemporaryMessageCalc.size() == 1)
			{
				// A single one
				Player.sendMessage((DiffPositive?ChatColor.DARK_GREEN:ChatColor.DARK_RED) + "You have been " + MessageWord + " a level!");
				String Tokens[] = mTemporaryMessageCalc.get(0).mMessage.split(System.getProperty("line.separator"));
				for (int i = 1; i < Tokens.length; i++)
				{
					Player.sendMessage(ChatColor.DARK_GREEN + Tokens[i]);
				}
			}
			else if(mTemporaryMessageCalc.size() == 0)
			{
				
			}
			else
			{
				// many
				Player.sendMessage((DiffPositive?ChatColor.DARK_GREEN:ChatColor.DARK_RED) + "You have been " + MessageWord + " the following levels!");
				for (LevelPack LP : mTemporaryMessageCalc)
				{
					String Tokens[] = LP.mMessage.split(System.getProperty("line.separator"));
					Player.sendMessage((DiffPositive?ChatColor.DARK_GREEN:ChatColor.DARK_RED) + "Level " + Integer.toString(LP.mLevel) + ": " + Tokens[0]);
				}
				Player.sendMessage((DiffPositive?ChatColor.DARK_GREEN:ChatColor.DARK_RED) + "For "+ChatColor.RED +"further information"+(DiffPositive?ChatColor.DARK_GREEN:ChatColor.DARK_RED)+" about individual levels please consult " + ChatColor.RED + "/xpp [LevelNumber]");
			}	
			
			// Insert the new level
			mPlayersLevels.put(Player.getName(), Player.getLevel());
		}
		
		// Empty it
		mPlayers.clear();
		//
		double Valu = (double)(System.nanoTime() - TimeStart)/1000000000D;
		String st2 =Double.toString(100D*(Valu/0.05D));
		String st1 =  Double.toString(Valu * 1000D);
		Logger.getLogger("Minecraft").info("[XPP] Checking permissions done. It took " + st1.substring(0, st1.length() > 7? 7:st1.length()) + " ms, which is " + st2.substring(0, st2.length() > 4? 4:st2.length()) + " percent of the time affo20tps");
	
	
	}

	/*
	 * Sends the corresponding Levelpacks description to the sender.
	 */
	public void displayDescription(CommandSender sender, int parseInt) 
	{
		for (LevelPack LP : mLevels)
		{
			if (LP.mLevel == parseInt)
			{
				String Tokens[] = LP.mMessage.split(System.getProperty("line.separator"));
				for (int i = 1; i < Tokens.length; i++)
				{
					sender.sendMessage(ChatColor.DARK_GREEN +Tokens[i]);
				}
				return;
			}
		}
		
		sender.sendMessage(ChatColor.DARK_RED + "There is no level pack with the supplied ID.");
		
	}
	

	
	int mSystem;
	Main mPlugin;
	ArrayList<LevelPack> mLevels;
	ArrayList<String> mPlayers;
	HashMap<String, Integer> mPlayersLevels;
	ArrayList<LevelPack> mTemporaryMessageCalc;
	
}
