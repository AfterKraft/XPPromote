package dxdy.XPPromote;


import java.util.Map;

/*
 * Warps permission systems
 */
public interface PermissionSystemWrapper {	
	/*
	 * Has the player the permission?
	 */
    public boolean hasPermission(String world, String perm);
    
    /*
     * 
     */
    public boolean hasPermission(String perm);
    
    /*
     * Remove a permission from the player
     */
    public void removePermission(String perm);
    
    /*
     * Add a permission to the player
     */
    public void addPermission(String perm);
    
    /*
     * Check whether the player is in a group
     */
    public boolean isInGroup(String group);
    
    /*
     * Add a group to the player
     */
    public void addGroup(String group);
    
    /*
     * Remove a group from the player
     */
    public void removeGroup(String group);

	public Map<String, String[]> getAllPermissions();
    
}
