package com.fallenangel.login;

import java.util.HashMap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.util.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.net.Socket;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.util.Properties;

import com.fallenangel.login.ListenerClass;

public class Main extends JavaPlugin implements Listener{
	
	String url = "jdbc:mysql://127.0.0.1:3306/mc_users";
	Connection conn;
	Statement stmt;

	private static Plugin plugin;

	public static HashMap<Player, UUID> NotLogged = new HashMap<Player, UUID>();
	
	public static String SanitizeString (String sant) {
		String regex = "[^\\p{L}\\p{N}]";
		String cleaned = sant;
		cleaned.replaceAll(regex, "");
		System.out.println("fixed string: " + cleaned);
		return cleaned;
	}
	
	public static void PushPlayerLogon(UUID u, Player p) {
		NotLogged.put(p, u);
		//System.out.println("Players:" + NotLogged);
	}
	
	public static String getMd5(String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
 
            byte[] messageDigest = md.digest(input.getBytes());
 
            BigInteger no = new BigInteger(1, messageDigest);
 
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
 
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public void onEnable() {
		plugin = this;
		Bukkit.getPluginManager().registerEvents(new ListenerClass(), this);
		String tmp = "hashdebug1";
		String tmp2= "hashdebug1";
		System.out.println("FALLEN ANGEL SECURITY PLUGIN IS ENABLED v 1.0.24");
		System.out.println("goldilock1: " + getMd5(tmp));
		System.out.println("goldilock2: " + getMd5(tmp2));
		try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        	System.out.println("Could not connect to MySQL :(");
        	System.out.println("SQLException: " + ex.getMessage());
        }
		
		try {
            conn = DriverManager.getConnection(url,"_danii","prettysecpwd");
            stmt = conn.createStatement();
		} catch (Exception e) {
			System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
		}
		timer();
		serverFlush();
        
	}
	
	@Override
	public void onDisable() {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Plugin getPlugin() {
	    return plugin;
	}
	
	private static void sendCommand(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
	        writer.write(command + "\r\n");
	        writer.flush();
	        System.out.println("Sent: " + command);
	        System.out.println("Response: " + reader.readLine());
	}
	
	static String removeNonAlphabetic(String str) {
		 
        // Use regular expression to match all non-alphabetic characters and replace with empty string
        String result = str.replaceAll("[^a-zA-Z0-9.@]", "");
 
        return result; // Return the resulting string
    }
	
	static String getAlphaNumericString(int n) 
	 { 
	 
	  // choose a Character random from this String 
	  String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	         + "0123456789"
	         + "abcdefghijklmnopqrstuvxyz"; 
	 
	  // create StringBuffer size of AlphaNumericString 
	  StringBuilder sb = new StringBuilder(n); 
	 
	  for (int i = 0; i < n; i++) { 
	 
	   // generate a random number between 
	   // 0 to AlphaNumericString variable length 
	   int index 
	    = (int)(AlphaNumericString.length() 
	      * Math.random()); 
	 
	   // add Character one by one in end of sb 
	   sb.append(AlphaNumericString 
	      .charAt(index)); 
	  } 
	 
	  return sb.toString(); 
	 } 
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmd = command.getName().toLowerCase();
		
		if(sender instanceof Player)
		{
			System.out.println("Command cmd = " + sender);
			System.out.println("Args = " + args);
			Player player = (Player) sender;
			if (cmd.equals("reg")) {
				player.sendMessage("PLAYER REGISTERED");
				try {
					ResultSet rs = stmt.executeQuery("SELECT * FROM USERS WHERE NAME = '"+player.getName()+"'");
					System.out.println(rs);
					if(rs.next()) {
						player.sendMessage("YOU ARE ALREADY REGISTERED");
					}else {
						String pwd_ne = SanitizeString(args[1]);
						String pwd = getMd5(pwd_ne);
						System.out.println("password:" + pwd_ne + ":" + pwd);
						String cel = SanitizeString(args[0]);
						stmt.executeUpdate("INSERT INTO USERS (UUID,NAME,PASS,IADDR,VTEL,TOTP) VALUES ('"+player.getUniqueId()+"','"+player.getName()+"','"+pwd+"','"+player.getAddress()+"','"+cel+"','00000');");
						player.sendMessage("YOU ARE NOW REGISTERED PLEASE LOGIN WITH /LOG");
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			if (cmd.equals("log")) {
				player.sendMessage("PLAYER LOGGED");
				String OTP = getAlphaNumericString(8);
				try {
					ResultSet rs = stmt.executeQuery("SELECT * FROM USERS WHERE NAME = '"+player.getName()+"'");
					System.out.println(rs);
					if(!rs.next()) {
						player.sendMessage("YOU ARE NOT REGISTERED!!!!");
					}else {
						String pwd_ne = SanitizeString(args[0]);
						String pwd = getMd5(pwd_ne);
						System.out.println("password:" + pwd_ne + ":" + pwd);
						//String cel = args[0];
						String pass = rs.getString("PASS");
						String mail = rs.getString("VTEL");
						System.out.println("Tested: " + pwd_ne + " and " + pass);
						if(pass.equals(pwd)) {
							player.sendMessage(ChatColor.AQUA + "Logged in! verify your identity with /2fa using the One Time Passwd sent to your email");
							System.out.println("hashes are equal");
							NotLogged.remove(player);
							
							stmt.executeUpdate("UPDATE USERS SET TOTP='"+OTP+"' WHERE NAME = '"+player.getName()+"'");
							// --- 2FA VERIFICATION PART ----
							String mcmd = "echo \"Your verification OTP code is: "+OTP+"\" | mail mail -s \"Verification\" \""+removeNonAlphabetic(mail)+"\"";
							System.out.println("EXEC: " + mcmd);
							
							try {
								Runtime r = Runtime.getRuntime();
								Process p = r.exec(mcmd);
								System.out.println("Sent...");
							}catch (Exception e) {
								System.out.println("cant send email :/ contact system admin for OTP");
							}
							// -----------------------------
						}else {
							Bukkit.getScheduler().runTask( getPlugin(), new Runnable() {
	                			public void run() {
	                				player.kickPlayer("Incorrect Password!!!!");
	                				}
	                		});
						}
						//player.sendMessage("YOU ARE NOW REGISTERED PLEASE LOGIN WITH /LOG");
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			if (cmd.equals("2fa")) {
				player.sendMessage("PLAYER VERIFIED");
				try {
					ResultSet rs = stmt.executeQuery("SELECT * FROM USERS WHERE NAME = '"+player.getName()+"'");
					System.out.println(rs);
					if(!rs.next()) {
						player.sendMessage("YOU ARE NOT REGISTERED!!!!");
					}else {
						String pwd_ne = SanitizeString(args[0]);
						String pwd = pwd_ne;
						System.out.println("OTPassword:" + pwd_ne + ":" + pwd);
						//String cel = args[0];
						String pass = rs.getString("TOTP");
						System.out.println("Tested: " + pwd_ne + " and " + pass);
						if(pass.equals(pwd)) {
							player.sendMessage(ChatColor.YELLOW + "WELCOME BACK!!!");
							System.out.println("hashes are equal");
							if(NotLogged.containsKey(player)) {
								Bukkit.getScheduler().runTask( getPlugin(), new Runnable() {
		                			public void run() {
		                				player.kickPlayer("YOU ARE NOT LOGGED IN!!!!");
		                				}
		                		});
							}else {
								Bukkit.getScheduler().runTask( getPlugin(), new Runnable() {
		                			public void run() {
		                				//player.kickPlayer("Incorrect Password!!!!");
		                				player.setGameMode(GameMode.SURVIVAL);
		                			}
		                		});
							}
							
							stmt.executeUpdate("UPDATE USERS SET TOTP='000000' WHERE NAME = '"+player.getName()+"'");
						}else {
							Bukkit.getScheduler().runTask( getPlugin(), new Runnable() {
	                			public void run() {
	                				player.kickPlayer("Incorrect Password!!!!");
	                				}
	                		});
						}
						//player.sendMessage("YOU ARE NOW REGISTERED PLEASE LOGIN WITH /LOG");
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public void timer() {
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
        {
            public void run()
            {
            	//System.out.println("debug");
                for(Player player : Bukkit.getServer().getOnlinePlayers())
                {
                    //player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
                	if(NotLogged.containsKey(player)) {
                		Bukkit.getScheduler().runTask( getPlugin(), new Runnable() {
                			public void run() {
                				//player.kickPlayer("You have been kicked due to login timeout");
                				player.setGameMode(GameMode.ADVENTURE);
                				}
                		});
                		player.sendMessage(ChatColor.GREEN + "-= PLEASE REGISTER USING COMMAND /reg <email> <Password> " + ChatColor.RED + " OR LOGIN USING /log <password>=-");
                	}
                }
            }
        }
        , 120, 120);
	}
	
	public void serverFlush() {
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
        {
            public void run()
            {
            	//System.out.println("debug");
                for(Player player : Bukkit.getServer().getOnlinePlayers())
                {
                    //player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
                	if(NotLogged.containsKey(player)) {
                		//player.sendMessage("-= PLEASE REGISTER USING COMMAND /reg <Phone number> <Password> =-");
                		//player.kickPlayer("You failed to login the server"); 
                		Bukkit.getScheduler().runTask( getPlugin(), new Runnable() {
                			public void run() {
                				player.kickPlayer("You have been kicked due to login timeout");
                				}
                		});
                	}
                }
            }
        }
        , 1200, 1200);
	}
	
}
