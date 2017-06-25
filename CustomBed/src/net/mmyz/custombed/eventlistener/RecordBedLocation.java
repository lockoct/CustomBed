package net.mmyz.custombed.eventlistener;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public final class RecordBedLocation implements Listener{

	
	@EventHandler
	public void getBedLocation(PlayerInteractEvent e){
		if (new File("plugins/CustomBed/Temp/data.json").exists() && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			try {
				JsonParser jp = new JsonParser();
				JsonObject temp = (JsonObject) jp.parse(new FileReader("plugins/CustomBed/Temp/data.json"));
				
                JsonArray playerDataArray = temp.get(e.getPlayer().getName()).getAsJsonArray();

                String markTool = playerDataArray.get(0).getAsJsonObject().get("MarkTool").getAsString();
				
				if(markTool.equalsIgnoreCase(e.getItem().getType().name())){
					if (playerDataArray.size() == 1) {
						Location l = e.getClickedBlock().getLocation();
						
						JsonObject locationDataElement = new JsonObject();
						locationDataElement.addProperty("World",l.getWorld().getName());
						locationDataElement.addProperty("LX",l.getBlockX());
						locationDataElement.addProperty("LY",l.getBlockY());
						locationDataElement.addProperty("LZ",l.getBlockZ());
						
						playerDataArray.add(locationDataElement);
						
						temp.add(e.getPlayer().getName(),playerDataArray);
						
						e.getPlayer().sendMessage("床位置为：世界="+l.getWorld().getName()+";X="+l.getBlockX()+";Y="+l.getBlockY()+";Z="+l.getBlockZ());
						
						FileOutputStream fos = new FileOutputStream("plugins/CustomBed/Temp/data.json");
						OutputStreamWriter osw = new OutputStreamWriter(fos);
						osw.write(temp.toString());
						osw.close();
						fos.close();
					}else if(playerDataArray.size() == 2){
						JsonObject newTemp = new JsonObject();
						
						JsonArray newPlayerDataArray = new JsonArray();

						JsonObject newPlayerDataElement = new JsonObject();

						newPlayerDataElement.addProperty("MarkTool", markTool);
						
						Location l = e.getClickedBlock().getLocation();
						
						JsonObject newLocationDataElement = new JsonObject();
						
						newLocationDataElement.addProperty("World",l.getWorld().getName());
						newLocationDataElement.addProperty("LX",l.getBlockX());
						newLocationDataElement.addProperty("LY",l.getBlockY());
						newLocationDataElement.addProperty("LZ",l.getBlockZ());
						
						newPlayerDataArray.add(newPlayerDataElement);
						newPlayerDataArray.add(newLocationDataElement);
						
						newTemp.add(e.getPlayer().getName(),newPlayerDataArray);
						
						e.getPlayer().sendMessage("床位置为：世界="+l.getWorld().getName()+";X="+l.getBlockX()+";Y="+l.getBlockY()+";Z="+l.getBlockZ());
						
						System.out.println(newTemp.toString());
						
						FileOutputStream fos = new FileOutputStream("plugins/CustomBed/Temp/data.json");
						OutputStreamWriter osw = new OutputStreamWriter(fos);
						osw.write(newTemp.toString());
						osw.close();
						fos.close();
					}
				}
			} catch (JsonIOException e1) {
				e1.printStackTrace();
			} catch (JsonSyntaxException e1) {
				e1.printStackTrace();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		  }
		
		/*尝试右键发送数据包，让玩家进入睡眠状态
		 * 然而在127行出现Field index out of bounds. (Index: 1, Size: 1)异常
		 * 未找到解决方法，开发进度暂停
		 */
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			Player player = e.getPlayer();
			Set<Player> sleeping = Collections.newSetFromMap(new WeakHashMap<Player, Boolean>());
			
			sleeping.add(player);
			
			PacketContainer bedPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.BED, false);
			Location l = e.getClickedBlock().getLocation();
			BlockPosition bp = new BlockPosition(l.getBlockX(),l.getBlockY(),l.getBlockZ());
			
			bedPacket.getEntityModifier(player.getWorld()).write(0, player);
			bedPacket.getIntegers().write(0, bp.getX()).write(1, bp.getY() + 1).write(2, bp.getZ());
			
			for(Player observer : ProtocolLibrary.getProtocolManager().getEntityTrackers(player)){
				try{
					ProtocolLibrary.getProtocolManager().sendServerPacket(observer, bedPacket);
					}
				
				catch(InvocationTargetException exception){
					throw new RuntimeException("Cannot send packet.", exception);
				}
			}
			
            System.out.println(123456);
			}
		}
	}
