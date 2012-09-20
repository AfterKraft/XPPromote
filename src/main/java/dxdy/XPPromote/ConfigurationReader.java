package dxdy.XPPromote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ConfigurationReader {
	ConfigurationReader(ArrayList<LevelPack> Levels)
	{
		mLevels = Levels;
	}
	
	/*
	 * Reads the configuration
	 */
	boolean ReadConfiguration()
	{
		try
		{
			ReadConfigFile();
			ReadMessages();
		}
		catch (Exception e)
		{
			Logger.getLogger("Minecraft").warning(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	void ReadConfigFile() throws Exception
	{
		// Check whether the Configuration-Directory exists
		File ConfigDirectory = new File("plugins/XPPromote/");
		if (!ConfigDirectory.exists())
			if (!ConfigDirectory.mkdir())
				throw new Exception("[XPP] DISABLING XPPROMOTE. CANNOT CREATE DIRECTORY EXCEPTION.");
		
		// Check whether the Config-File exists
		File ConfigFile = new File(ConfigDirectory, "config.txt");
		if (!ConfigFile.exists())
		{
			try
			{
				CreateDefaultConfiguration (ConfigFile);			
			}
			catch (IOException e)
			{
				throw new Exception("[XPP] Configuration file not found. An input error occured while the default configuration was being created. DISABLING XPPROMOTE.");
			}
		}
		BufferedReader Reader = new BufferedReader(new FileReader(ConfigFile));
		
		// Read update interval and level loss
		String UpdateInterval = null, LevelLoss = null, PS = null;
		while((UpdateInterval = Reader.readLine()).charAt(0) == '#'){};
		while((LevelLoss = Reader.readLine()).charAt(0) == '#'){};
		while((PS = Reader.readLine()).charAt(0) == '#'){};
		
		// Check whether those numbers are numbers
		try
		{
			mUpdateInterval = Integer.parseInt(UpdateInterval);
			mLevelLoss = Integer.parseInt(LevelLoss);
			mPS = Integer.parseInt(PS);
		}
		catch (NumberFormatException e)
		{
			// apply defaults if it isn't so
			Logger.getLogger("Minecraft").info("[XPP] The Update interval (#1) as well as the Level loss (#2) have to be integers.");
			Logger.getLogger("Minecraft").info("[XPP] Applying default settings.");
			mUpdateInterval = 1000;
			mLevelLoss = 10;			
			mPS = 2;
		}
		
		// For each remaining line of the Configuration
		String CurrentLine = null;
		while ((CurrentLine = Reader.readLine()) != null)
		{
			if (CurrentLine.charAt(0) != '#');
			{
				try{
					AddLevelPack (CurrentLine);
				} catch (Exception e) {
					Logger.getLogger("Minecraft").info("[XPP] Found invalid configuration line \"" + CurrentLine + "\". Reason: " + e.getMessage());
				}
			}
				
		}
		
		// Close reader and return
		Reader.close();
		return;
	}
	
	/*
	 * ReReads all messages from all files
	 */
	void ReadMessages()
	{
		// Check whether the directory exists
		File MessageDirectory = new File("plugins/XPPromote/messages/");
		if (!MessageDirectory.exists())
		{
			// TODO: Error checking code for return value of mkdir
			MessageDirectory.mkdir();
		}
		
		for (LevelPack LP : mLevels)
		{
			try{
				ReadMessage(LP.mLevel, MessageDirectory, LP);
			} catch (Exception e) {
				Logger.getLogger("Minecraft").info("[XPP] Unspecified / Buggy level up message for \"" + Integer.toString(LP.mLevel) + "\". Reason: " + e.getMessage());
				LP.mMessage = "This is the default level up message."+System.getProperty("line.separator") + "This is the default level up message description";
			}
		}
	}
	
	/*
	 * Reads the messages for a single level.
	 * \param MessageDirectory The directory containing all the message files
	 */
	private void ReadMessage(int mLevel, File MessageDirectory, LevelPack LP) throws Exception
	{
		// Check whether the parameters provided are valid
		if (MessageDirectory == null || LP == null || mLevel < 0)
		{
			Logger.getLogger("Minecraft").severe("[XPP] Parameter validation failed @ReadMessages(ConfigurationReader.java). Report immediately.");
			return;
		}
		
		// Check whether file exists
		File MessageFile = new File(MessageDirectory, "message"+Integer.toString(mLevel));
		if (!MessageFile.exists())
		{
			throw new Exception("The file does not exist.");
		}
		
		// Create the reader instance
		BufferedReader Reader = new BufferedReader(new FileReader(MessageFile));
		
		// Read in lines
		String CurrentLine = null;
		StringBuilder MessageLine = new StringBuilder("");
		while ((CurrentLine = Reader.readLine()) != null)
		{
			MessageLine.append(CurrentLine).append(System.getProperty("line.separator"));
		}
		
		//
		Reader.close();
		
		// Overwrite
		LP.mMessage = MessageLine.toString();
	}

	/*
	 * Takes a configuration file entry as string and computes its level pack.
	 * 
	 * The computed level pack is automatically added to the existing collection of levels.
	 */
	void AddLevelPack(String ConfigurationLine) throws Exception
	{
		// Split up into tokens
		String[] Tokens = ConfigurationLine.split(";");
		
		// Validate Input
		if (Tokens.length < 2)
		{
			throw new Exception ("Each LevelPack needs at least a level number and an empty group name");
		}
		
		// Get Level
		LevelPack CurrentLevelPack = new LevelPack();
		try{
		CurrentLevelPack.mLevel = Integer.parseInt(Tokens[0]);
		} catch (NumberFormatException e) {
			throw new Exception("Level has to be an integer.");
		}
		
		// Get Group
		CurrentLevelPack.mGroup = Tokens[1];
		
		//
		CurrentLevelPack.mPermissionNodes = new ArrayList<PermissionNode>();
		
		// Get Permission Nodes
		for (int i = 2; i < Tokens.length; i++)
		{
			// Read Permission Type
			String PermissionNode = Tokens[i];
			PermissionNode PermNode = new PermissionNode();
			PermissionNodeType PermType;
			if (PermissionNode.charAt(0) == '#')
				PermType = PermissionNodeType.PNT_PERSIST;
			else if (PermissionNode.charAt(0) == '^')
				PermType = PermissionNodeType.PNT_STRIP;
			else
				PermType = PermissionNodeType.PNT_NORMAL;
			PermNode.mType = PermType;
			
			// Add the node and process the string
			PermNode.mNode = (PermType == PermissionNodeType.PNT_NORMAL ? PermissionNode : PermissionNode.substring(1));
			
			// and add it
			CurrentLevelPack.mPermissionNodes.add(PermNode);
		}


		// Add it
		mLevels.add(CurrentLevelPack);
	}
	
	// The caller guarantees that the directory exists.
	void CreateDefaultConfiguration(File ConfigFile) throws IOException
	{
		// Create the file, 
		ConfigFile.createNewFile();
		
		// Write the default configuration
		BufferedWriter Writer = new BufferedWriter(new FileWriter(ConfigFile));
		Writer.write("1000"); Writer.newLine(); Writer.write("90"); Writer.newLine(); Writer.flush(); Writer.close();
	}
	
	private ArrayList<LevelPack> mLevels;
	public int mUpdateInterval;
	public int mLevelLoss;
	public int mPS;
}
