package commonTestImpl;

import commonTestImpl.substitutions.TestRTPPlayer;
import commonTestImpl.substitutions.TestRTPWorld;
import io.github.dailystruggle.rtp.common.RTP;
import io.github.dailystruggle.rtp.common.configuration.ConfigParser;
import io.github.dailystruggle.rtp.common.configuration.enums.MessagesKeys;
import io.github.dailystruggle.rtp.common.configuration.enums.RegionKeys;
import io.github.dailystruggle.rtp.common.selection.region.Region;
import io.github.dailystruggle.rtp.common.selection.region.selectors.shapes.Shape;
import io.github.dailystruggle.rtp.common.selection.worldborder.WorldBorder;
import io.github.dailystruggle.rtp.common.serverSide.RTPServerAccessor;
import io.github.dailystruggle.rtp.common.serverSide.substitutions.RTPCommandSender;
import io.github.dailystruggle.rtp.common.serverSide.substitutions.RTPPlayer;
import io.github.dailystruggle.rtp.common.serverSide.substitutions.RTPWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class TestRTPServerAccessor implements RTPServerAccessor {
    private String version = null;
    private Integer intVersion = null;

    Function<String,Shape<?>> shapeFunction;

    public TestRTPServerAccessor() {
        //run later to ensure RTP instance exists
        // configs are initialized in tick 1, so reference them at 2 or later
        // command processing timer is delayed to ensure this is set up before it's used

        shapeFunction = s -> {
            Region region = RTP.selectionAPI.getRegion(new TestRTPWorld());
            if(region == null) throw new IllegalStateException();
            Object o = region.getData().get(RegionKeys.shape);
            if(!(o instanceof Shape<?>)) throw new IllegalStateException();
            return (Shape<?>) o;
        };
    }

    @Override
    public @NotNull String getServerVersion() {
        if(version == null) {
            version = "1.19.3";
        }

        return version;
    }

    @Override
    public @NotNull Integer getServerIntVersion() {
        if(intVersion == null) {
            String[] splitVersion = getServerVersion().split("_");
            if(splitVersion.length == 0) {
                intVersion = 0;
            }
            else if (splitVersion.length == 1) {
                intVersion = Integer.valueOf(splitVersion[0]);
            }
            else {
                intVersion = Integer.valueOf(splitVersion[1]);
            }
        }
        return intVersion;
    }

    @Override
    public RTPWorld getRTPWorld(String name) {
        return new TestRTPWorld();
    }

    @Override
    public @Nullable RTPWorld getRTPWorld(UUID id) {
        return new TestRTPWorld();
    }

    @Override
    public @Nullable Shape<?> getShape(String name) {
        return shapeFunction.apply(name);
    }

    @Override
    public boolean setShapeFunction(Function<String, Shape<?>> shapeFunction) {
        boolean works = true;
        this.shapeFunction = shapeFunction;
        return works;
    }

    @Override
    public @NotNull List<RTPWorld> getRTPWorlds() {
        ArrayList<RTPWorld> res = new ArrayList<>(1);
        res.add(new TestRTPWorld());
        return res;
    }

    @Override
    public @Nullable RTPPlayer getPlayer(UUID uuid) {
        return new TestRTPPlayer();
    }

    @Override
    public @Nullable RTPPlayer getPlayer(String name) {
        return new TestRTPPlayer();
    }

    @Override
    public @Nullable RTPCommandSender getSender(UUID uuid) {
        return new TestRTPPlayer();
    }

    @Override
    public long overTime() {
        return 0;
    }

    @Override
    public File getPluginDirectory() {
        try {
            File res = new File(RTP.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
            res = res.getParentFile();
            res = new File(res.getAbsolutePath() + File.separator + "config");
            res.mkdir();
            return res;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void sendMessage(UUID target, MessagesKeys msgType) {
        ConfigParser<MessagesKeys> parser = (ConfigParser<MessagesKeys>) RTP.configs.getParser(MessagesKeys.class);
        String msg = String.valueOf(parser.getConfigValue(msgType,""));
        if(msg == null || msg.isEmpty()) return;
        sendMessage(target, msg);
    }

    @Override
    public void sendMessage(UUID target1, UUID target2, MessagesKeys msgType) {
        ConfigParser<MessagesKeys> parser = (ConfigParser<MessagesKeys>) RTP.configs.getParser(MessagesKeys.class);
        String msg = String.valueOf(parser.getConfigValue(msgType,""));
        if(msg == null || msg.isEmpty()) return;
        sendMessage(target1,target2,msg);
    }

    @Override
    public void sendMessage(UUID target, String message) {

    }

    @Override
    public void sendMessageAndSuggest(UUID target, String message, String suggestion) {

    }

    @Override
    public void sendMessage(UUID target1, UUID target2, String message) {

    }

    @Override
    public void log(Level level, String msg) {
        System.out.println(level.getName() + ": " + msg);
    }

    @Override
    public void log(Level level, String msg, Exception exception) {
        System.out.println(level.getName() + ": " + msg);
    }

    @Override
    public void announce(String msg, String permission) {

    }

    @Override
    public Set<String> getBiomes(RTPWorld rtpWorld) {
        return TestRTPWorld.getBiomes();
    }

    @Override
    public boolean isPrimaryThread() {
        return false;
    }

    @Override
    public @Nullable WorldBorder getWorldBorder(String worldName) {
        return null;
    }

    @Override
    public boolean setWorldBorderFunction(Function<String, WorldBorder> function) {
        return true;
    }

    @Override
    public Set<String> materials() {
        return null;
    }

    @Override
    public void stop() {

    }

    @Override
    public void start() {

    }
}
