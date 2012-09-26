package dxdy.XPPromote.PSW;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;


public class VaultWrapper {

	Player mPlayer;
	Permission mRSP;
	String sPlayer;
	String sWorld = null;

	public VaultWrapper(Player pPlayer, Permission permissionProvider)
	{
		mPlayer = pPlayer;
		mRSP=permissionProvider;
		sPlayer = pPlayer.getName();

	}

	public boolean hasPermission(String world, String perm) {
		return mRSP.has(world, mPlayer.getName(), perm);
	}

	public boolean hasPermission(String perm) {
		return mPlayer.hasPermission(perm);
	}

	public void removePermission(String perm) {
		mRSP.playerRemoveTransient(mPlayer, perm);
	}


	public void addPermission(String perm) {
		mRSP.playerAddTransient(mPlayer, perm);

	}

	/*
	 * TODO: Get PersistPermissions to work globally.
	 * 
	 */
	public void addPersistentPermissions(String perm) {
		mRSP.playerAdd(sWorld, sPlayer, perm);
	}

	public boolean isInGroup(String group) {
		return mRSP.playerInGroup(mPlayer, group);
	}

	public void addGroup(String group) {
		mRSP.playerAddGroup(mPlayer, group);		
	}

	public void removeGroup(String group) {
		mRSP.playerAddGroup(mPlayer, group);		
	}


	public Map<String, String[]> getAllPermissions() {
		HashMap<String, String[]> tmp = new HashMap<String, String[]>();

		Set<PermissionAttachmentInfo> s = mPlayer.getEffectivePermissions();
		String[] ttmp = new String[s.size()];
		int it = 0;
		for (PermissionAttachmentInfo xy : s)
		{
			ttmp[it] = xy.getPermission();
			it+=1;
		}

		tmp.put("*", ttmp);

		return tmp;
	}
}
