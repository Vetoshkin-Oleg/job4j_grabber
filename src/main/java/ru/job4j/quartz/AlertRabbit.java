package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private final Properties properties;

    public AlertRabbit(Properties properties) {
        this.properties = properties;
    }

    private Connection initConnection() throws ClassNotFoundException, IOException {
        Connection connection;
        try (InputStream in = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(in);
            Class.forName("org.postgresql.Driver");
        }
        String url = properties.getProperty("url");
        String login = properties.getProperty("login");
        String password = properties.getProperty("password");
        try {
            connection = DriverManager.getConnection(url, login, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static void main(String[] args) {
        try {
            AlertRabbit alertRabbit = new AlertRabbit(new Properties());
            Connection connection = alertRabbit.initConnection();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(5)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            try (Connection connection = (Connection) context.getJobDetail()
                    .getJobDataMap().get("connection")) {
                try (Statement statement = connection.createStatement()) {
                    String sql = String.format(
                            "CREATE TABLE IF NOT EXISTS rabbit(%s, %s);",
                            "id SERIAL PRIMARY KEY",
                            "created_date timestamp"
                    );
                    statement.execute(sql);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     "INSERT INTO rabbit(created_date) VALUES (?)")) {
                    statement.setString(1, String.valueOf(
                            new Timestamp(System.currentTimeMillis())));
                    statement.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
