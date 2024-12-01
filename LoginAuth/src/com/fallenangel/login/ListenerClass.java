package com.fallenangel.login;

// import com.fallenangel.Main.NotLogged;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class ListenerClass implements Listener {
	@EventHandler
	public void onPlayerJoin (PlayerLoginEvent e){
		Player p = e.getPlayer();
		//p.sendMessage("Please Register using command /register <phone number> <password>");
		//p.setGameMode(GameMode.ADVENTURE);
		com.fallenangel.login.Main.PushPlayerLogon(p.getUniqueId(), p);
		System.out.println("a user has joined OwO");
	}

	@EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        //event.setMessage(event.getMessage().replaceAll("word", "[censored]"));
		Player p = event.getPlayer();
    }
}
