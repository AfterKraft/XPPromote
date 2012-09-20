package dxdy.XPPromote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import dxdy.XPPromote.PSW.VaultWrapper;

public class LevelPackListener implements Listener{
	
	LevelPackListener(int LevelLoss, Main plugin)
	{
		mPlugin = plugin;
		mListToUpdate = new ArrayList<String>();
		mLevelLoss = LevelLoss; 
		mPlayers = new HashMap<String, Double>();
		mPlayersLevels = new HashMap<String, Integer>();
		
		for (Player p : Bukkit.getOnlinePlayers())
		{
			mListToUpdate.add(p.getName());
			checkBoosterLevel(p);
			mPlayersLevels.put(p.getName(),p.getLevel());
		}
		
		// 
		Logger.getLogger("Minecraft").info("[XPP] Listener created. Queued all players.");
	}
	
	@EventHandler(ignoreCancelled=true)
	void onPlayerChangeLevel(PlayerLevelChangeEvent event)
	{
		if (!mListToUpdate.contains(event.getPlayer().getName()))
			mListToUpdate.add(event.getPlayer().getName());
	}
	
	@EventHandler(ignoreCancelled=true)
	void onPlayerChangeExp(PlayerExpChangeEvent event)
	{
		// Check out whether he is in the map with the precalculated boosters
		Double ThisPlayersBooster = mPlayers.get(event.getPlayer().getName());
		if (ThisPlayersBooster == null)
		{
			checkBoosterLevel(event.getPlayer());
			ThisPlayersBooster = mPlayers.get(event.getPlayer().getName());
			if (ThisPlayersBooster == null)
			{
				Logger.getLogger("Minecraft").info("[XPP] Warning: Ignored boost of " + event.getPlayer().getName() + " because the plugin wasn't able to determine it from the players permissions.");
				return;
			}
		}
		
		//
		event.setAmount((int)(event.getAmount()*ThisPlayersBooster));
	}
	
	@EventHandler(ignoreCancelled=true)
	void onPlayerLogin(PlayerLoginEvent event)
	{
		// Is the event active?
		if (event.getResult() == PlayerLoginEvent.Result.ALLOWED && event.getPlayer() != null && !mListToUpdate.contains(event.getPlayer().getName()))
			mListToUpdate.add(event.getPlayer().getName());
		// Check the booster level out and cache it
		checkBoosterLevel(event.getPlayer());
		//
		mPlayersLevels.put(event.getPlayer().getName(), event.getPlayer().getLevel());
	}
	
	void onPlayer(PlayerExpChangeEvent event)
	{
		event.setAmount((int)(event.getAmount() * (double)(Math.pow(0.5D, ((double)event.getPlayer().getLevel())/48.D))));
	}
	
	@EventHandler(ignoreCancelled=true)
	void onPlayerDeath(PlayerDeathEvent event)
	{
		// Died. Calculate Level Loss
		PlayerDeathEvent pde = event;
		if (!mListToUpdate.contains(pde.getEntity().getName()))
			mListToUpdate.add(pde.getEntity().getName());
		
		// 
		int newLevel = (int) (((double)pde.getEntity().getLevel())*(1-((double)mLevelLoss)/100.));
		if (newLevel < 0)newLevel = 0;
		pde.getEntity().setLevel(newLevel);
		pde.setKeepLevel(true);
		pde.setDroppedExp((int) (pde.getDroppedExp() * (1-((double)mLevelLoss)/100.)));
		Logger.getLogger("Minecraft").info("[XPP] Player " + pde.getEntity().getName() + " lost " + String.valueOf(pde.getEntity().getLevel()-newLevel) + " Level(old" + String.valueOf(pde.getEntity().getLevel())+"new"+pde.getNewLevel()+")");
		
	}
	
	@EventHandler(ignoreCancelled=true)
	void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player p = event.getPlayer();
		Bukkit.getScheduler().scheduleSyncDelayedTask(mPlugin, new ExpRefreshTask(p),10);
		
	}

	@EventHandler(ignoreCancelled=true)
	void onPlayerKicked(PlayerKickEvent event)
	{
		mPlayers.remove(event.getPlayer().getName());
		mPlayersLevels.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event)
	{
		mPlayers.remove(event.getPlayer().getName());
		mPlayersLevels.remove(event.getPlayer().getName());
	}
	
	private void checkBoosterLevel(Player p)
	{
		// Remove old mapping
		mPlayers.remove(p.getName());
		
		// 
		VaultWrapper PEXPlayer = mPlugin.getPermissionWrapper(p, mPlugin.getPermSystem());
		if (PEXPlayer == null)
		{
			mPlayers.put(p.getName(), 1D);
			Logger.getLogger("Minecraft").info("[XPP] Warning: PermissionSystem returned null as PermissionsUser corresponding to the player " + p.getName());
			return;
		}
		
		// Loop through all players permissions
		double Boost = 1D;
		Set<Entry<String, String[]>> Permissions = PEXPlayer.getAllPermissions().entrySet();
		for (Entry<String, String[]> PE : Permissions)
		{
			for (int i = 0; i < PE.getValue().length; i++)
			{
				String CurrentPerm = PE.getValue()[i];
				// Check whether it contains the permission prefix
				if (CurrentPerm.contains(mBoostPermissionPrefix))
				{
					CurrentPerm = CurrentPerm.substring(mBoostPermissionPrefix.length());
					Boost = Double.parseDouble(CurrentPerm);
				}
			}
		}
		
		//
		mPlayers.put(p.getName(), Boost);
		Logger.getLogger("Minecraft").info("[XPP] Info: Registered booster value of " + Double.toString(Boost) + " for player " + p.getName());
		
	}
	
	public ArrayList<String> mListToUpdate;
	public int mLevelLoss;
	private HashMap<String, Double> mPlayers;
	public HashMap<String, Integer> mPlayersLevels;
	public Main mPlugin;
	private static String mBoostPermissionPrefix = "xppromote.boost.";
}
