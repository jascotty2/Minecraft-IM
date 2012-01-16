/**
 * Programmer: Jacob Scott
 * Program Name: MinecraftIM
 * Description:
 * Date: Mar 24, 2011
 */
package com.jascotty2.minecraftim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author jacob
 */
public class MinecraftIM extends JavaPlugin {

	protected final static Logger logger = Logger.getLogger("Minecraft");
	public static final String name = "MinecraftIM";
	public Messenger mess;
	public PListener playerListener;
	ArrayList<ChatMessageHandler> messages = new ArrayList<ChatMessageHandler>();
	final static HashMap<String, String> libs = new HashMap<String, String>();
	{
		libs.put("lib/jml-1.0b4-full.jar", "net.sf.jml.MsnMessenger");
		libs.put("lib/joscar-0.9.3.jar", "net.kano.joscar.ByteBlock");
		libs.put("lib/smack.jar", "org.jivesoftware.smack.Chat");
		libs.put("lib/smackx.jar", "org.jivesoftware.smackx.XHTMLManager");
	}
	
	public MinecraftIM() {
		// extract jars to lib folder
		try {
			if (extractLibs()) {
//				Log("reloading plugin for new libraries..");
//				pm.disablePlugin(this);
//				try {
//					pm.loadPlugin(getJarFile());
//					Plugin p = pm.getPlugin(name);
//					pm.enablePlugin(p);
//				} catch (Exception ex) {
//					Log(Level.SEVERE, "Can't reload Plugin", ex);
//				}
//				return;
			}
		} catch (Exception e) {
			Log(Level.SEVERE, "Failed to extract lib jar", e);
		}
		mess = new Messenger(this);
		playerListener = new PListener(this);
	}
	
	public void onEnable() {
		Log("Starting Version " + this.getDescription().getVersion());

		PluginManager pm = getServer().getPluginManager();

		

		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Monitor, this);
		startIM();
	}

	public void onDisable() {
		mess.disconnect();
		Log("Disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("reload") && sender.isOp()) {
				if (startIM()) {
					sender.sendMessage("Successfully reloaded");
				} else {
					sender.sendMessage("An error occurred while reloading");
				}
			}
		}
		return true;
	}

	boolean startIM() {
		mess.disconnect();
		if (mess.load()) {
			Log("Config Loaded");
		} else {
			Log("Config Load Error");
			return false;
		}
		if (mess.connect()) {
			Log("Connecting to IM account");
			Log("sending connect confirm to " + mess.sendToUsername);
			mess.sendNotify("MinecraftIM enabled");

			return true;
		} else {
			Log(Level.SEVERE, "Error connecting to IM protocol");
			return false;
		}
	}

	public void sendNotify(String message) {
		mess.sendNotify(message);
	}

	public void registerMessageHandler(ChatMessageHandler toAdd) {
		if (!messages.contains(toAdd)) {
			messages.add(toAdd);
		}
	}

	public void removeMessageHandler(ChatMessageHandler toRem) {
		if (!messages.contains(toRem)) {
			messages.remove(toRem);
		}
	}

	public boolean messageRecieved(String fromUser, String message) {
		for (ChatMessageHandler c : messages) {
			if (c.messageHandled(fromUser, message)) {
				return true;
			}
		}
		return false;
	}

	public boolean messageRecieved(String message) {
		for (ChatMessageHandler c : messages) {
			if (c.messageHandled(message)) {
				return true;
			}
		}
		return false;
	}

	public static void Log(String txt) {
		logger.log(Level.INFO, String.format("[%s] %s", name, txt));
	}

	public static void Log(String txt, Object params) {
		logger.log(Level.INFO, String.format("[%s] %s", name, txt == null ? "" : txt), params);
	}

	public static void Log(Level loglevel, String txt) {
		logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt));
	}

	public static void Log(Level loglevel, String txt, Object params) {
		logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), params);
	}

	public static void Log(Level loglevel, String txt, Exception params) {
		if (txt == null) {
			Log(loglevel, params);
		} else {
			logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), params);
		}
	}

	public static void Log(Level loglevel, String txt, Object[] params) {
		logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), params);
	}

	public static void Log(Level loglevel, Exception err) {
		logger.log(loglevel, String.format("[%s] %s", name, err == null ? "? unknown exception ?" : err.getMessage()), err);
	}

//	public static File getJarFile() {
//		return new File(MinecraftIM.class.getProtectionDomain().getCodeSource().getLocation().getPath().
//				replace("%20", " ").replace("%25", "%"));
//	}

	protected static boolean extractLibs() throws IOException {
		boolean ex = false;
		for (String fn : libs.keySet()) {
			File f = new File(fn.replace("/", File.separator));
			if (!f.exists()) {
				ex = true;
				extractResource("/" + fn, f.getParentFile());
				//callbackPlugin.tryLoadClass(libs.get(fn));
			}
		}
		return ex;
	}

	protected static void extractResource(String jarPath, String destFolder) throws IOException {
		extractResource(jarPath, new File(destFolder));
	}

	protected static void extractResource(String jarPath, File destFolder) throws IOException {
		File j = new File(jarPath);
		File wr = new File(destFolder, j.getName());
//		wr.createNewFile();
//		InputStream res = Messenger.class.getResourceAsStream(jarPath);
//		FileWriter tx = new FileWriter(wr);
//		try {
//			for (int i = 0; (i = res.read()) > 0;) {
//				tx.write(i);
//			}
//		} finally {
//			tx.flush();
//			tx.close();
//			res.close();
//		}
		byte buf[] = new byte[1024];
		int l;
		URL res = Messenger.class.getResource(jarPath);
		if (res == null) {
			throw new IOException("cannot find " + jarPath + " in jar");
		}
		URLConnection resConn = res.openConnection();
		resConn.setUseCaches(false);
		InputStream in = resConn.getInputStream();
		if(!wr.exists()) {
			// first check if need to create the directory structure
			if(destFolder.exists() && !destFolder.isDirectory()){
				throw new IOException("cannot use '" + destFolder.getAbsolutePath() + "': is a directory");
			} else if(!destFolder.exists() && !destFolder.mkdirs()) {
				throw new IOException("cannot use '" + destFolder.getAbsolutePath() + "': cannot create the directory");
			}
			if(!wr.createNewFile()) {
				throw new IOException("cannot write to " + wr.getAbsolutePath());
			}
		}
		FileOutputStream out = new FileOutputStream(wr);

		while ((l = in.read(buf)) > 0) {
			out.write(buf, 0, l);
		}
		in.close();
		out.close();
	}
} // end class MinecraftIM

