package su.nightexpress.ama.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.google.gson.reflect.TypeToken;

import su.nexmedia.engine.data.DataTypes;
import su.nexmedia.engine.data.IDataHandler;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.stats.StatType;

public class ArenaUserData extends IDataHandler<AMA, ArenaUser> {

	private static ArenaUserData instance;
	
	private final Function<ResultSet, ArenaUser> FUNC_USER;
	
	protected ArenaUserData(@NotNull AMA plugin) throws SQLException {
		super(plugin);
		
		FUNC_USER = (rs) -> {
	        try {
	        	UUID uuid = UUID.fromString(rs.getString(COL_USER_UUID));
	        	String name = rs.getString(COL_USER_NAME);
	        	long lastOnline = rs.getLong(COL_USER_LAST_ONLINE);
	        	
	        	int coins = rs.getInt("coins");
		        Set<String> kits = gson.fromJson(rs.getString("kits"), new TypeToken<Set<String>>(){}.getType());
		        Map<String, Map<StatType, Integer>> stats = gson.fromJson(rs.getString("stats"), new TypeToken<Map<String, Map<StatType, Integer>>>(){}.getType());
		        
		        return new ArenaUser(plugin, uuid, name, lastOnline, coins, kits, stats);
	        }
	        catch (SQLException ex) {
	        	return null;
	        }
		};
	}

	@NotNull
	public static synchronized ArenaUserData getInstance() throws SQLException {
		if (instance == null) {
			instance = new ArenaUserData(AMA.getInstance());
		}
		return instance;
	}
	
	@Override
	@NotNull
	protected LinkedHashMap<String, String> getColumnsToCreate() {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("coins", DataTypes.INTEGER.build(this.dataType, 11));
		map.put("kits", DataTypes.STRING.build(this.dataType));
		map.put("stats", DataTypes.STRING.build(this.dataType));
		return map;
	}

	@Override
	@NotNull
	protected LinkedHashMap<String, String> getColumnsToSave(@NotNull ArenaUser user) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("coins", String.valueOf(user.getCoins()));
		map.put("kits", this.gson.toJson(user.getKits()));
		map.put("stats", this.gson.toJson(user.getStats()));
		return map;
	}

	@Override
	@NotNull
	protected Function<ResultSet, ArenaUser> getFunctionToUser() {
		return this.FUNC_USER;
	}
}
