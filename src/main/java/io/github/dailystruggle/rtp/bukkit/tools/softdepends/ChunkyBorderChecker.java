package io.github.dailystruggle.rtp.bukkit.tools.softdepends;

import io.github.dailystruggle.rtp.api.RTPAPI;
import io.github.dailystruggle.rtp.bukkit.server.substitutions.BukkitRTPWorld;
import io.github.dailystruggle.rtp.common.RTP;
import io.github.dailystruggle.rtp.common.factory.Factory;
import io.github.dailystruggle.rtp.common.factory.FactoryValue;
import io.github.dailystruggle.rtp.common.selection.region.selectors.memory.shapes.enums.RectangleParams;
import io.github.dailystruggle.rtp.common.selection.region.selectors.shapes.Shape;
import io.github.dailystruggle.rtp.common.selection.worldborder.WorldBorder;
import io.github.dailystruggle.rtp.common.serverSide.substitutions.RTPWorld;
import io.github.dailystruggle.rtp.common.tools.ChunkyRTPShape;
import org.bukkit.Location;
import org.bukkit.World;
import org.popcraft.chunkyborder.BorderData;
import org.popcraft.chunkyborder.ChunkyBorder;
import org.popcraft.chunkyborder.ChunkyBorderProvider;

import java.util.Optional;

public class ChunkyBorderChecker {
    //stored object reference to skip plugin getting sometimes
    private static ChunkyBorder chunkyBorder = null;

    /**
     * getPAPI - function to if PAPI exists and fill the above object reference accordingly
     */
    private static void getChunky() {
        try {
            chunkyBorder = ChunkyBorderProvider.get();
        } catch ( Throwable t ) {
            chunkyBorder = null;
        }
    }

    public static void loadChunky() {
        //if I don't have a correct object reference, try to get one.
        getChunky();

        // chunkyborder initialization
        if ( chunkyBorder != null ) {
            RTP.serverAccessor.setWorldBorderFunction( worldName -> {
                try {
                    Optional<BorderData> borderDataOptional = ChunkyBorderProvider.get().getBorder( worldName );
                    if ( borderDataOptional.isPresent() ) {
                        BorderData borderData = borderDataOptional.get();

                        Factory<?> factory = RTP.factoryMap.get( RTP.factoryNames.shape );
                        org.popcraft.chunky.shape.Shape border = borderData.getBorder();
                        FactoryValue<?> value = factory.get( "chunky_" + border.name() );
                        if( !(value instanceof Shape<?>) ) throw new IllegalStateException( "Shape factory is not using shape class" );
                        if( !value.myClass.equals( RectangleParams.class) ) throw new IllegalStateException( "chunky shape ` " + "chunky_" + border.name() + " ` not using rectangle as its superclass" );
                        @SuppressWarnings( "unchecked" ) Shape<RectangleParams> shape = ( Shape<RectangleParams> ) value;
                        if ( !(shape instanceof ChunkyRTPShape) ) {
                            shape = new ChunkyRTPShape( "chunky_" + border.name() );
                            RTPAPI.addShape( shape );
                        }
                        double radiusX = borderData.getRadiusX()*0.9d;
                        double radiusZ = borderData.getRadiusZ()*0.9d;
                        shape.set( RectangleParams.width, ((long)radiusX)/16 );
                        shape.set( RectangleParams.height, ((long)radiusZ)/16 );
                        Shape<RectangleParams> finalShape = shape;
                        return new WorldBorder( () -> finalShape, rtpLocation -> border.isBounding( rtpLocation.x() / 16.0, rtpLocation.z() / 16.0) );
                    }
                    RTPWorld rtpWorld = RTP.serverAccessor.getRTPWorld( worldName );
                    if ( rtpWorld instanceof BukkitRTPWorld ) {
                        BukkitRTPWorld bukkitRTPWorld = ( BukkitRTPWorld ) rtpWorld;
                        World world = bukkitRTPWorld.world();
                        org.bukkit.WorldBorder worldBorder = world.getWorldBorder();
                        return new WorldBorder(
                                () -> ( (Shape<?> ) RTP.factoryMap.get( RTP.factoryNames.shape ).get( "SQUARE") ),
                                rtpLocation -> worldBorder.isInside( new Location( world, rtpLocation.x(), rtpLocation.y(), rtpLocation.z())) );
                    }
                } catch ( Throwable t ) {
                    chunkyBorder = null;
                    RTPWorld rtpWorld = RTP.serverAccessor.getRTPWorld( worldName );
                    if ( rtpWorld instanceof BukkitRTPWorld ) {
                        BukkitRTPWorld bukkitRTPWorld = ( BukkitRTPWorld ) rtpWorld;
                        World world = bukkitRTPWorld.world();
                        org.bukkit.WorldBorder worldBorder = world.getWorldBorder();
                        return new WorldBorder(
                                () -> ( (Shape<?> ) RTP.factoryMap.get( RTP.factoryNames.shape ).get( "SQUARE") ),
                                rtpLocation -> worldBorder.isInside( new Location( world, rtpLocation.x(), rtpLocation.y(), rtpLocation.z())) );
                    }
                }
                return null;
            } );
        }
    }
}
