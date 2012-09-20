package dxdy.XPPromote;

import java.util.ArrayList;

enum PermissionNodeType
{
	PNT_NORMAL, // Normal permission Node, Grant on LevelUp, Remove on LevelDown
	PNT_STRIP, // Stripped permission Node. Grant as long as under threshold
	PNT_PERSIST // Persisting permission Node. Grant and never take away.
}

// Level type
public class LevelPack {
	int mLevel;
	ArrayList<PermissionNode> mPermissionNodes;
	String mGroup;
	String mMessage;
}
