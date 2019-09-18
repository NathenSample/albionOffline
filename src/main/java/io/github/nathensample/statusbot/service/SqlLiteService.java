package io.github.nathensample.statusbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class SqlLiteService
{
    //TODO: Dedupe and make SQL code less shit

    private static final String DATABASE_PATH_ROOT = "./sqlite/";
    private static final String DATABASE_JDBC_ROOT = "jdbc:sqlite:";
    private static final String DATABASE_NAME = "channelIds";
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlLiteService.class);
    private static final Marker FATAL = MarkerFactory.getMarker("FATAL");

    @PostConstruct
    public boolean instantiateDatabases() throws IOException
    {
        if (!databaseExists(DATABASE_NAME)) {
            createDatabase(DATABASE_NAME);
        }
        return true;
    }

    public List<String> loadChannelIdsFromDatabase() {
        File fullPathFile = new File(DATABASE_PATH_ROOT + DATABASE_NAME);

        try(Connection conn = DriverManager.getConnection(DATABASE_JDBC_ROOT + fullPathFile.getAbsolutePath()))
        {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM channelIds");
            List<String> ids = new ArrayList<>();
            while (rs.next()){
                ids.add(rs.getString("channelId"));
            }
            LOGGER.info("Queried database for channels {}", ids);
            return ids;
        } catch (SQLException e)
        {
            LOGGER.error(FATAL, "Unable to query database {} application will now terminate", DATABASE_NAME, e);
            System.exit(-1);
        }
        return null;//unreachable
    }


    public boolean persistChannelToDatabase(String channelId) {
        File fullPathFile = new File(DATABASE_PATH_ROOT + DATABASE_NAME);
        try(Connection conn = DriverManager.getConnection(DATABASE_JDBC_ROOT + fullPathFile.getAbsolutePath()))
        {
            PreparedStatement prep = conn.prepareStatement("INSERT INTO channelIds VALUES (?)");
            prep.setString(1, channelId);
            return prep.execute();
        } catch (SQLException e)
        {
            LOGGER.error("Unable to insert to database {}", DATABASE_NAME, e);
            return false;
        }
    }

    public boolean removeChannelFromDatabase(String channelId) {
        File fullPathFile = new File(DATABASE_PATH_ROOT + DATABASE_NAME);
        try(Connection conn = DriverManager.getConnection(DATABASE_JDBC_ROOT + fullPathFile.getAbsolutePath()))
        {
            PreparedStatement prep = conn.prepareStatement("DELETE FROM channelIds WHERE channelId=?;)");
            prep.setString(1, channelId);
            return prep.execute();
        } catch (SQLException e)
        {
            LOGGER.error("Unable to delete {}", channelId, e);
            return false;
        }
    }

    private boolean databaseExists(String databaseName)
    {
        File verifyExistence = new File(DATABASE_PATH_ROOT + databaseName);
        return verifyExistence.exists();
    }

    private void createDatabase(String databaseName) throws IOException
    {
        File folderPath = new File(DATABASE_PATH_ROOT);
        if (!folderPath.exists()){
            Files.createDirectory(Paths.get(DATABASE_PATH_ROOT));
        }
        File fullPathFile = new File(DATABASE_PATH_ROOT + databaseName);

        try(Connection conn = DriverManager.getConnection(DATABASE_JDBC_ROOT + fullPathFile.getAbsolutePath())){
            LOGGER.info("Created database {} with driver {}", databaseName, conn.getMetaData().getDriverName());
            Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS channelIds (channelId);");
        } catch (SQLException e)
        {
            LOGGER.error(FATAL, "Unable to connect to database {} application will now terminate", databaseName, e);
            System.exit(-1);
        }
    }


}
