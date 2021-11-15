package su.nightexpress.ama.arena;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.core.Version;
import su.nexmedia.engine.hooks.Hooks;
import su.nightexpress.ama.api.arena.IArena;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ArenaBoard {

    private final ArenaPlayer  arenaPlayer;
    private final List<String> lines;
    private final Sideline     sideline;
    
    public ArenaBoard(@NotNull ArenaPlayer arenaPlayer, @NotNull Map.Entry<String, List<String>> entry) {
        //AMA plugin = arenaPlayer.getArena().plugin();
    	this.arenaPlayer = arenaPlayer;
        this.lines = entry.getValue();
        this.sideline = new Sideline(new Sidebar(arenaPlayer.getPlayer()));
        this.sideline.getSidebar().setName(entry.getKey());
        this.lines.forEach(this.sideline::add); // .map(SunBoard::color)
        //this.sideline.flush();
        this.update();
    }
    
    public synchronized void update() {
    	this.getLines().forEach(this.sideline::add);
        this.sideline.flush();
    }
    
    public synchronized void send(@NotNull String replace) {
	    this.sideline.getSidebar().setName(replace);
	}

	public void remove() {
        this.sideline.getSidebar().remove();
    }
    
	@NotNull
	private List<String> getLines() {
		Player player = this.arenaPlayer.getPlayer();
        IArena arena = this.arenaPlayer.getArena();
		List<String> list = new ArrayList<>();
		
		Set<ArenaPlayer> added = new HashSet<>();
		for (String line : this.lines) {
		    // TODO Use another placeholder to detect top stats line
			if (line.contains(ArenaPlayer.PLACEHOLDER_NAME)) {
				Set<ArenaPlayer> players2 = arena.getPlayersIngame();
				for (int pos = 0; pos < players2.size(); pos++) {
					ArenaPlayer topPlayer = arena.getHighScore(pos + 1);
					if (!added.add(topPlayer)) continue; // Quick fix for duplicated stats.
					
					list.add(topPlayer.replacePlaceholders().apply(line));
				}
				continue;
			}
			
	        if (Hooks.hasPlaceholderAPI()) {
	            line = PlaceholderAPI.setPlaceholders(player, line);
	        }
	        line = arena.replacePlaceholders().apply(line);
	        line = arenaPlayer.replacePlaceholders().apply(line);
	        list.add(line);
		}
		
	    return list;
	}
    
    public static class Sidebar {
    	
        private final Player                   player;
        private final HashMap<String, Integer> linesA;
        private final HashMap<String, Integer> linesB;
        private       Boolean                  a;
        private final SpecificWriterHandler handler;
        
        private String getBuffer() {
            return this.a ? "A" : "B";
        }
        
        private HashMap<String, Integer> linesBuffer() {
            return this.a ? this.linesA : this.linesB;
        }
        
        private HashMap<String, Integer> linesDisplayed() {
            return this.a ? this.linesB : this.linesA;
        }
        
        private void swapBuffer() {
            this.a = !this.a;
        }
        
        public Sidebar(Player p) {
            this.a = true;
            this.player = p;
            this.linesA = new HashMap<>();
            this.linesB = new HashMap<>();
            this.handler = new SpecificWriterHandler();
            PacketContainer createA = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            createA.getStrings().write(0, "A");
            createA.getChatComponents().write(0, WrappedChatComponent.fromText(""));
            createA.getIntegers().write(0, 0);
            this.handler.write(createA, SpecificWriterType.DISPLAY);
            PacketContainer createB = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            createB.getStrings().write(0, "B");
            createB.getChatComponents().write(0, WrappedChatComponent.fromText(""));
            createB.getIntegers().write(0, 0);
            this.handler.write(createB, SpecificWriterType.DISPLAY);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(p, createA);
                ProtocolLibrary.getProtocolManager().sendServerPacket(p, createB);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        
        public void send() {
            PacketContainer display = new PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
            display.getIntegers().write(0, 1);
            display.getStrings().write(0, this.getBuffer());
            this.swapBuffer();
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, display);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            for (String text : this.linesDisplayed().keySet()) {
                if (this.linesBuffer().containsKey(text) && this.linesBuffer().get(text) == this.linesDisplayed().get(text)) {
                    continue;
                }
                this.setLine(text, this.linesDisplayed().get(text));
            }
            for (String text : new ArrayList<>(this.linesBuffer().keySet())) {
                if (!this.linesDisplayed().containsKey(text)) {
                    this.removeLine(text);
                }
            }
        }
        
        public void remove() {
            PacketContainer removeA = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            removeA.getStrings().write(0, "A");
            removeA.getChatComponents().write(0, WrappedChatComponent.fromText(""));
            removeA.getIntegers().write(0, 1);
            PacketContainer removeB = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            removeB.getStrings().write(0, "B");
            removeB.getChatComponents().write(0, WrappedChatComponent.fromText(""));
            removeB.getIntegers().write(0, 1);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, removeA);
                ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, removeB);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        
        public void clear() {
            for (String text : new ArrayList<>(this.linesBuffer().keySet())) {
                this.removeLine(text);
            }
        }
        
        public void setLine(String text, Integer line) {
            if (text == null) {
                return;
            }
            if (text.length() > 40) {
                text = text.substring(0, 40);
            }
            if (this.linesBuffer().containsKey(text)) {
                this.removeLine(text);
            }
            PacketContainer set = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
            set.getStrings().write(0, text).write(1, this.getBuffer());
            set.getIntegers().write(0, line);
            this.handler.write(set, SpecificWriterType.ACTIONCHANGE);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, set);
                this.linesBuffer().put(text, line);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        
        public void removeLine(String text) {
            if (text.length() > 40) {
                text = text.substring(0, 40);
            }
            if (!this.linesBuffer().containsKey(text)) {
                return;
            }
            Integer line = this.linesBuffer().get(text);
            PacketContainer reset = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
            reset.getStrings().write(0, text).write(1, this.getBuffer());
            reset.getIntegers().write(0, line);
            this.handler.write(reset, SpecificWriterType.ACTIONREMOVE);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, reset);
                this.linesBuffer().remove(text);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        
        public void setName(String displayName) {
            if (displayName.length() > 32) {
                displayName = displayName.substring(0, 32);
            }
            PacketContainer nameA = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            nameA.getStrings().write(0, "A");
            nameA.getChatComponents().write(0, WrappedChatComponent.fromText(displayName));
            nameA.getIntegers().write(0, 2);
            this.handler.write(nameA, SpecificWriterType.DISPLAY);
            PacketContainer nameB = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
            nameB.getStrings().write(0, "B");
            nameB.getChatComponents().write(0, WrappedChatComponent.fromText(displayName));
            nameB.getIntegers().write(0, 2);
            this.handler.write(nameB, SpecificWriterType.DISPLAY);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, nameA);
                ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, nameB);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        
        public String getName() {
            return this.player.getName();
        }
    }
    
    public static class Sideline {
    	
        Sidebar sb;
        HashMap<Integer, String> old;
        Deque<String> buffer;
        
        public Sideline(Sidebar sb) {
            this.old = new HashMap<>();
            this.buffer = new ArrayDeque<>();
            this.sb = sb;
        }
        
        public void clear() {
            this.sb.clear();
            this.old.clear();
        }
        
        public void set(Integer i, String str) {
            if (this.old.containsKey(i)) {
                this.sb.removeLine(this.old.get(i));
            }
            if (str.equals("")) {
                str = " ";
            }
            str = this.makeUnique(str);
            this.old.put(i, str);
            this.sb.setLine(str, i);
        }
        
        public String makeUnique(String str) {
            while (this.old.containsValue(str)) {
                for (int j = 0; j < ChatColor.values().length; ++j) {
                    if (!this.old.containsValue(str + ChatColor.values()[j])) {
                        str += ChatColor.values()[j];
                        return str;
                    }
                }
                str += ChatColor.RESET;
            }
            return str;
        }
        
        public void add(String s) {
            this.buffer.add(s);
        }
        
        public void flush() {
            this.clear();
            Integer i = 0;
            Iterator<String> it = this.buffer.descendingIterator();
            while (it.hasNext()) {
                String line = it.next();
                ++i;
                this.set(i, line);
            }
            this.buffer.clear();
            this.sb.send();
        }
        
        public void send() {
            this.sb.send();
        }
        
        public Integer getRemainingSize() {
            return 15 - this.buffer.size();
        }
        
        public Sidebar getSidebar() {
            return this.sb;
        }
    }
    
    public enum SpecificWriterType {
        DISPLAY, 
        ACTIONCHANGE, 
        ACTIONREMOVE;
    }
    
    public static class SpecificWriterHandler {
    	
        private static String version;
        private static Class<?> healthclass;
        private static Object interger;
        
        public void write(PacketContainer container, SpecificWriterType type) {
            if (type == SpecificWriterType.DISPLAY) {
                container.getModifier().write(2, SpecificWriterHandler.interger);
            }
            else if (type == SpecificWriterType.ACTIONCHANGE) {
                container.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.CHANGE);
            }
            else if (type == SpecificWriterType.ACTIONREMOVE) {
                container.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.REMOVE);
            }
        }
        
        public static String a(String str) {
            if (Version.CURRENT.isHigher(Version.V1_16_R3)) {
                return "net.minecraft.world.scores.criteria." + str;
            }
            return "net.minecraft.server." + SpecificWriterHandler.version + "." + str;
        }
        
        public static String getNMSVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        }
        
        static {
            SpecificWriterHandler.version = getNMSVersion();
            try {
                SpecificWriterHandler.healthclass = Class.forName(a("IScoreboardCriteria$EnumScoreboardHealthDisplay"));
                SpecificWriterHandler.interger = SpecificWriterHandler.healthclass.getEnumConstants()[0];
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
