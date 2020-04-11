package dev.esophose.rosestacker.conversion.converter;

import com.songoda.epicspawners.EpicSpawners;
import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.database.DatabaseConnector;
import dev.esophose.rosestacker.database.SQLiteConnector;
import dev.esophose.rosestacker.manager.DataManager;
import dev.esophose.rosestacker.stack.StackedSpawner;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class EpicSpawnersPluginConverter extends StackPluginConverter {

    private EpicSpawners epicSpawners;

    public EpicSpawnersPluginConverter(RoseStacker roseStacker) {
        super(roseStacker, "EpicSpawners");

        this.epicSpawners = (EpicSpawners) this.plugin;
    }

    @Override
    public void convert() {
        DataManager dataManager = this.roseStacker.getManager(DataManager.class);

        // Query their database ourselves
        DatabaseConnector connector = new SQLiteConnector(this.epicSpawners);
        connector.connect(connection -> {
            Set<StackedSpawner> stackedSpawners = new HashSet<>();

            String tablePrefix = this.epicSpawners.getDataManager().getTablePrefix();
            try (Statement statement = connection.createStatement()) {
                String query = "SELECT amount, world, x, y, z FROM " + tablePrefix + "placed_spawners ps JOIN " + tablePrefix + "spawner_stacks ss ON ps.id = ss.spawner_id";
                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString("world"));
                    if (world == null)
                        continue;

                    int amount = result.getInt("amount");
                    double x = result.getDouble("x");
                    double y = result.getDouble("y");
                    double z = result.getDouble("z");

                    Location location = new Location(world, x, y, z);
                    stackedSpawners.add(new StackedSpawner(amount, location));
                }
            }

            dataManager.createOrUpdateStackedBlocksOrSpawners(stackedSpawners);
        });
    }

}
