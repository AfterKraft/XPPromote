package dxdy.XPPromote.PSW;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;


public class VaultWrapper {
	public VaultWrapper(Player pPlayer, Permission permissionProvider)
	{		
		mPlayer = pPlayer;
        mRSP=permissionProvider;
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

	/*
	 * TODO: Change this addPermission method to addTransientPermission and create a new
	 * addPersistPermission to actually force saving Persist Permissions to users (through Vault)
	 * 
	 */
	public void addPermission(String perm) {
		mRSP.playerAddTransient(mPlayer, perm);
	
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

	Player mPlayer;
	Permission mRSP;
	
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
