package io.github.dailystruggle.rtp.bukkit.tools;

import io.github.dailystruggle.commandsapi.common.CommandsAPI;
import io.github.dailystruggle.rtp.bukkit.RTPBukkitPlugin;
import io.github.dailystruggle.rtp.bukkit.server.substitutions.BukkitRTPCommandSender;
import io.github.dailystruggle.rtp.bukkit.server.substitutions.BukkitRTPPlayer;
import io.github.dailystruggle.rtp.bukkit.server.substitutions.BukkitRTPWorld;
import io.github.dailystruggle.rtp.bukkit.tools.softdepends.PAPIChecker;
import io.github.dailystruggle.rtp.common.RTP;
import io.github.dailystruggle.rtp.common.configuration.ConfigParser;
import io.github.dailystruggle.rtp.common.configuration.enums.ConfigKeys;
import io.github.dailystruggle.rtp.common.configuration.enums.MessagesKeys;
import io.github.dailystruggle.rtp.common.playerData.TeleportData;
import io.github.dailystruggle.rtp.common.selection.region.Region;
import io.github.dailystruggle.rtp.common.serverSide.substitutions.RTPCommandSender;
import io.github.dailystruggle.rtp.common.tasks.RTPRunnable;
import io.github.dailystruggle.rtp.common.tasks.teleport.DoTeleport;
import io.github.dailystruggle.rtp.common.tasks.teleport.LoadChunks;
import io.github.dailystruggle.rtp.common.tasks.teleport.SetupTeleport;
import io.github.dailystruggle.rtp.common.tools.ParsePermissions;
import io.github.dailystruggle.rtp.common.tools.ParseString;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.tozymc.spigot.api.title.TitleApi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendMessage {
    public static final Map<String, Function<UUID, String>> placeholders = new ConcurrentHashMap<>();
    private static final Pattern hexColorPattern1 = Pattern.compile( "(&?#[0-9a-fA-F]{6})" );
    private static final Pattern hexColorPattern2 = Pattern.compile( "(&[0-9a-fA-F]&[0-9a-fA-F]&[0-9a-fA-F]&[0-9a-fA-F]&[0-9a-fA-F]&[0-9a-fA-F])" );
    private static ConfigParser<MessagesKeys> lang = null;

    static {
        Bukkit.getScheduler().runTaskLater( RTPBukkitPlugin.getInstance(), () -> {
            lang = ( ConfigParser<MessagesKeys> ) RTP.configs.getParser( MessagesKeys.class );
        }, 2 );

        placeholders.put( "delay", uuid -> {
            if ( RTP.getInstance() == null ) return "0";
            if ( RTP.serverAccessor == null ) return "0";
            RTPCommandSender commandSender = RTP.serverAccessor.getSender( uuid );
            Number n = RTP.configs.getParser( ConfigKeys.class ).getNumber( ConfigKeys.teleportDelay, 0 );
            int n2 = ParsePermissions.getInt( commandSender, "RTP.getInstance().delay." );
            if ( n2 >= 0 ) n = n2;
            if ( n.longValue() == 0 ) return "0";

            long time = n.longValue();
            ConfigParser<MessagesKeys> langParser = ( ConfigParser<MessagesKeys> ) RTP.configs.getParser( MessagesKeys.class );
            long days = TimeUnit.SECONDS.toDays( time );
            long hours = TimeUnit.SECONDS.toHours( time ) % 24;
            long minutes = TimeUnit.SECONDS.toMinutes( time ) % 60;
            long seconds = time % 60;

            String replacement = "";
            if ( days > 0 ) replacement += days + langParser.getConfigValue( MessagesKeys.days, "" ).toString() + " ";
            if ( hours > 0 ) replacement += hours + langParser.getConfigValue( MessagesKeys.hours, "" ).toString() + " ";
            if ( minutes > 0 )
                replacement += minutes + langParser.getConfigValue( MessagesKeys.minutes, "" ).toString() + " ";
            if ( seconds > 0 ) replacement += seconds + langParser.getConfigValue( MessagesKeys.seconds, "" ).toString();
            return replacement;
        } );
        placeholders.put( "cooldown", uuid -> {
            if ( RTP.getInstance() == null ) return "A";
            if ( RTP.serverAccessor == null ) return "B";
            RTPCommandSender commandSender = RTP.serverAccessor.getSender( uuid );
            Number n = RTP.configs.getParser( ConfigKeys.class ).getNumber( ConfigKeys.teleportCooldown, 0 );
            int n2 = ParsePermissions.getInt( commandSender, "RTP.getInstance().cooldown." );
            if ( n2 >= 0 ) n = n2;

            long time = n.longValue();
            if ( time <= 0 ) time = 0;
            ConfigParser<MessagesKeys> langParser = ( ConfigParser<MessagesKeys> ) RTP.configs.getParser( MessagesKeys.class );
            long days = TimeUnit.SECONDS.toDays( time );
            long hours = TimeUnit.SECONDS.toHours( time ) % 24;
            long minutes = TimeUnit.SECONDS.toMinutes( time ) % 60;
            long seconds = time % 60;

            String replacement = "";
            if ( days > 0 ) replacement += days + langParser.getConfigValue( MessagesKeys.days, "" ).toString() + " ";
            if ( hours > 0 ) replacement += hours + langParser.getConfigValue( MessagesKeys.hours, "" ).toString() + " ";
            if ( minutes > 0 )
                replacement += minutes + langParser.getConfigValue( MessagesKeys.minutes, "" ).toString() + " ";
            if ( seconds > 0 ) replacement += seconds + langParser.getConfigValue( MessagesKeys.seconds, "" ).toString();
            return replacement;
        } );
        placeholders.put( "remainingCooldown", uuid -> {
            if ( RTP.getInstance() == null ) return "A";
            if ( RTP.serverAccessor == null ) return "B";

            long start = System.currentTimeMillis();

            Player player = Bukkit.getPlayer( uuid );
            if ( player != null && player.isOnline() ) {
                TeleportData teleportData = RTP.getInstance().latestTeleportData.get( uuid );
                long lastTime = start;
                if ( teleportData != null ) lastTime = teleportData.time;


                RTPCommandSender sender = RTP.serverAccessor.getSender( player.getUniqueId() );
                long n = sender.cooldown();

                long currTime = ( start - lastTime );
                long remainingTime = n - currTime;
                if ( remainingTime < 0 ) remainingTime = 0;

                ConfigParser<MessagesKeys> langParser = ( ConfigParser<MessagesKeys> ) RTP.configs.getParser( MessagesKeys.class );
                long days = TimeUnit.MILLISECONDS.toDays( remainingTime );
                long hours = TimeUnit.MILLISECONDS.toHours( remainingTime ) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes( remainingTime ) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds( remainingTime ) % 60;
                long millis = remainingTime % 1000;
                if ( millis > 500 && seconds > 0 ) {
                    seconds++;
                    millis = 0;
                }

                String replacement = "";
                if ( days > 0 ) replacement += days + langParser.getConfigValue( MessagesKeys.days, "" ).toString() + " ";
                if ( hours > 0 )
                    replacement += hours + langParser.getConfigValue( MessagesKeys.hours, "" ).toString() + " ";
                if ( minutes > 0 )
                    replacement += minutes + langParser.getConfigValue( MessagesKeys.minutes, "" ).toString() + " ";
                if ( seconds > 0 ) {

                    replacement += seconds + langParser.getConfigValue( MessagesKeys.seconds, "" ).toString();
                }
                if ( seconds < 2 ) {
                    replacement += millis + langParser.getConfigValue( MessagesKeys.millis, "" ).toString();
                }
                return replacement;
            }
            return "C";
        } );
        placeholders.put( "queueLocation", uuid -> {
            if ( RTP.getInstance() == null ) return "0";
            TeleportData teleportData = RTP.getInstance().latestTeleportData.get( uuid );
            if ( teleportData == null ) return "0";
            return String.valueOf( teleportData.queueLocation );
        } );
        placeholders.put( "chunks", uuid -> {
            if ( RTP.getInstance() == null ) return "0";
            AtomicInteger c = new AtomicInteger();
            RTP.serverAccessor.getRTPWorlds().forEach( world -> {
                if ( !(world instanceof BukkitRTPWorld) ) return;
                c.addAndGet( ((BukkitRTPWorld ) world ).chunkMap.size() );
            } );
            return String.valueOf( c.get() );
        } );
        placeholders.put( "attempts", uuid -> {
            if ( RTP.getInstance() == null ) return "A";
            TeleportData teleportData = RTP.getInstance().latestTeleportData.get( uuid );
            if ( teleportData == null ) return "B";
            return String.valueOf( teleportData.attempts );
        } );
        placeholders.put( "processingTime", uuid -> {
            if ( RTP.getInstance() == null ) return "0";
            TeleportData teleportData = RTP.getInstance().latestTeleportData.get( uuid );

            long time = ( teleportData != null )
                    ? teleportData.processingTime
                    : 0L;

            ConfigParser<MessagesKeys> langParser = ( ConfigParser<MessagesKeys> ) RTP.configs.getParser( MessagesKeys.class );
            long days = TimeUnit.MILLISECONDS.toDays( time );
            long hours = TimeUnit.MILLISECONDS.toHours( time ) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes( time ) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds( time ) % 60;
            long millis = time % 1000;
            if ( millis > 500 && seconds > 0 ) {
                seconds++;
                millis = 0;
            }

            String replacement = "";
            if ( days > 0 ) replacement += days + langParser.getConfigValue( MessagesKeys.days, "" ).toString() + " ";
            if ( hours > 0 ) replacement += hours + langParser.getConfigValue( MessagesKeys.hours, "" ).toString() + " ";
            if ( minutes > 0 )
                replacement += minutes + langParser.getConfigValue( MessagesKeys.minutes, "" ).toString() + " ";
            if ( seconds > 0 ) replacement += seconds + langParser.getConfigValue( MessagesKeys.seconds, "" ).toString();
            if ( seconds < 2 ) {
                replacement += millis + langParser.getConfigValue( MessagesKeys.millis, "" ).toString();
            }
            return replacement;
        } );
        placeholders.put( "spot", uuid -> {
            if ( RTP.getInstance() == null ) return "0";
            TeleportData teleportData = RTP.getInstance().latestTeleportData.get( uuid );
            if ( teleportData == null ) return "0";

            long spot = teleportData.queueLocation;
            return String.valueOf( spot );
        } );
        placeholders.put( "player", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }
            return player.getName();
        } );
        placeholders.put( "player_name", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }
            return player.getName();
        } );
        placeholders.put( "player_status", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }

            TeleportData data = RTP.getInstance().latestTeleportData.get( player.getUniqueId() );
            ConfigParser<MessagesKeys> lang = ( ConfigParser<MessagesKeys> ) RTP.configs.getParser( MessagesKeys.class );

            if ( data == null )
                return SendMessage.formatDry( player, lang.getConfigValue( MessagesKeys.PLAYER_AVAILABLE, "" ).toString() );
            if ( data.completed ) {
                BukkitRTPCommandSender sender = new BukkitRTPCommandSender( player );
                long dt = System.currentTimeMillis() - data.time;
                if ( dt < 0 ) dt = Long.MAX_VALUE + dt;
                if ( dt < sender.cooldown() ) {
                    return SendMessage.formatDry( player, lang.getConfigValue( MessagesKeys.PLAYER_COOLDOWN, "" ).toString() );
                }

                return SendMessage.formatDry( player, lang.getConfigValue( MessagesKeys.PLAYER_AVAILABLE, "" ).toString() );
            }

            RTPRunnable nextTask = data.nextTask;
            if ( nextTask instanceof DoTeleport )
                return SendMessage.formatDry( player, lang.getConfigValue( MessagesKeys.PLAYER_TELEPORTING, "" ).toString() );
            if ( nextTask instanceof LoadChunks )
                return SendMessage.formatDry( player, lang.getConfigValue( MessagesKeys.PLAYER_LOADING, "" ).toString() );
            if ( nextTask instanceof SetupTeleport )
                return SendMessage.formatDry( player, lang.getConfigValue( MessagesKeys.PLAYER_SETUP, "" ).toString() );
            return "";
        } );

        placeholders.put( "total_queue_length", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }

            Region region = RTP.selectionAPI.getRegion( new BukkitRTPPlayer( player) );
            if ( region == null ) return "0";
            return String.valueOf( region.getTotalQueueLength( player.getUniqueId()) );
        } );

        placeholders.put( "public_queue_length", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }

            Region region = RTP.selectionAPI.getRegion( new BukkitRTPPlayer( player) );
            if ( region == null ) return "0";
            return String.valueOf( region.getPublicQueueLength() );
        } );

        placeholders.put( "personal_queue_length", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }

            Region region = RTP.selectionAPI.getRegion( new BukkitRTPPlayer( player) );
            if ( region == null ) return "0";
            return String.valueOf( region.getPersonalQueueLength( player.getUniqueId()) );
        } );

        placeholders.put( "teleport_world", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }

            TeleportData data = RTP.getInstance().latestTeleportData.get( player.getUniqueId() );
            return data.selectedLocation.world().name();
        } );

        placeholders.put( "teleport_x", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }

            TeleportData data = RTP.getInstance().latestTeleportData.get( player.getUniqueId() );
            return String.valueOf( data.selectedLocation.x() );
        } );

        placeholders.put( "teleport_y", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }

            TeleportData data = RTP.getInstance().latestTeleportData.get( player.getUniqueId() );
            return String.valueOf( data.selectedLocation.y() );
        } );

        placeholders.put( "teleport_z", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }

            TeleportData data = RTP.getInstance().latestTeleportData.get( player.getUniqueId() );
            return String.valueOf( data.selectedLocation.z() );
        } );

        placeholders.put( "teleport_biome", uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player == null ) {
                return "";
            }

            TeleportData data = RTP.getInstance().latestTeleportData.get( player.getUniqueId() );
            return data.selectedLocation.world().getBiome( 
                    data.selectedLocation.x(),
                    data.selectedLocation.y(),
                    data.selectedLocation.z()
             );
        } );
    }

    public static void sendMessage( CommandSender target1, CommandSender target2, String message ) {
        if ( message == null || message.isEmpty() ) return;
        sendMessage( target1, message );
        if ( !target1.getName().equals( target2.getName()) ) {
            sendMessage( target2, message );
        }
    }

    public static void sendMessage( CommandSender sender, String message ) {
        if ( message == null || message.isEmpty() ) return;
        if ( sender instanceof Player ) sendMessage( (Player ) sender, message );
        else {
            message = format( Bukkit.getOfflinePlayer( CommandsAPI.serverId ), message );
            if ( RTP.serverAccessor.getServerIntVersion() > 12 ) {
                BaseComponent[] components = TextComponent.fromLegacyText( message );
                sender.spigot().sendMessage( components );
            } else sender.sendMessage( message );
        }
    }

    public static void sendMessage( Player player, String message ) {
        if ( message == null || message.isEmpty() ) return;
        message = format( player, message );
        if ( RTP.serverAccessor.getServerIntVersion() > 12 ) {
            BaseComponent[] components = TextComponent.fromLegacyText( message );
            player.spigot().sendMessage( components );
        } else player.sendMessage( message );
    }

    public static void sendMessage( RTPCommandSender sender, String message, String hover, String click ) {
        if (message.isEmpty()) return;

        OfflinePlayer player;
        if ( sender instanceof Player ) player = ( OfflinePlayer ) sender;
        else player = Bukkit.getOfflinePlayer( CommandsAPI.serverId ).getPlayer();

        message = format( player, message );

        if ( RTP.serverAccessor.getServerIntVersion() > 12 ) {
            BaseComponent[] textComponents = TextComponent.fromLegacyText( message );

            if (!hover.isEmpty()) {
                BaseComponent[] hoverComponents = TextComponent.fromLegacyText( format( player, hover) );
                //noinspection deprecation
                HoverEvent hoverEvent = ( RTP.serverAccessor.getServerIntVersion() >= 16 )
                        ? new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text( hoverComponents) )
                        : new HoverEvent( HoverEvent.Action.SHOW_TEXT, hoverComponents );
                for ( BaseComponent component : textComponents ) {
                    component.setHoverEvent( hoverEvent );
                }
            }

            if (!click.isEmpty()) {
                ClickEvent clickEvent = new ClickEvent( ClickEvent.Action.SUGGEST_COMMAND, click );
                for ( BaseComponent component : textComponents ) {
                    component.setClickEvent( clickEvent );
                }
            }

            if ( sender instanceof BukkitRTPCommandSender )
                ( (BukkitRTPCommandSender ) sender ).sender().spigot().sendMessage( textComponents );
            else if ( sender instanceof BukkitRTPPlayer )
                ( (BukkitRTPPlayer ) sender ).player().spigot().sendMessage( textComponents );
        } else sender.sendMessage( message );
    }

    public static String format( @Nullable OfflinePlayer player, @Nullable String text ) {
        if ( text == null ) return "";

        //get uuid to be referenced by placeholder getters
        UUID uuid = ( player != null ) ? player.getUniqueId() : CommandsAPI.serverId;

        //create a container for placeholder getter results
        // initialize with the same size as the placeholder getter map to skip reallocation
        Map<String, String> placeholders = new HashMap<>( SendMessage.placeholders.size() );

        Set<String> keywords = ParseString.keywords( text, SendMessage.placeholders.keySet(), new HashSet<>( Arrays.asList( '[', '%') ), new HashSet<>( Arrays.asList( ']', '%')) );

        //for each placeholder getter, add placeholder and result to container
        for ( Map.Entry<String, Function<UUID, String>> entry : SendMessage.placeholders.entrySet() ) {
            String s = entry.getKey();
            Function<UUID, String> uuidStringFunction = entry.getValue();
            if ( !keywords.contains( s ) ) continue;
            placeholders.put( s, uuidStringFunction.apply( uuid ) );
        }

        //replace all placeholders with their respective string function results
        for ( Map.Entry<String, String> e : placeholders.entrySet() ) {
            text = Pattern.compile( "\\[" + e.getKey() + "]",Pattern.CASE_INSENSITIVE ).matcher( text ).replaceAll( e.getValue() );
        }

        if ( lang != null ) {
            Pattern placeholderIntPattern = Pattern.compile( "\\[([Pp])(\\d*)]" );
            Matcher placeholderIntMatcher = placeholderIntPattern.matcher( text );
            while ( placeholderIntMatcher.find() ) {
                String group = placeholderIntMatcher.group( 2 );
                int bits;
                try {
                    bits = Integer.parseInt( group );
                } catch ( NumberFormatException ignored ) {
                    continue;
                }
                placeholderIntMatcher.reset();

                String replacement = "[invalid]";
                ConfigParser<MessagesKeys> parser = ( ConfigParser<MessagesKeys> ) RTP.configs.getParser( MessagesKeys.class );
                Object o = parser.getConfigValue( MessagesKeys.placeholders, new ArrayList<>() );
                if ( o instanceof List ) {
                    List<?> pList = ( List<?> ) o;
                    if ( pList.size() > bits ) {
                        replacement = pList.get( bits ).toString();
                    }
                }

                replacement = placeholderIntPattern.matcher( replacement ).replaceAll( "" );

                text = placeholderIntMatcher.replaceFirst( replacement );
                placeholderIntMatcher = placeholderIntPattern.matcher( text );
            }
        }

        //check PAPI exists and fill remaining PAPI placeholders
        //todo: if a null player doesn't work with another PAPI import, blame that import for not verifying its inputs.
        text = PAPIChecker.fillPlaceholders( player, text );

        text = ChatColor.translateAlternateColorCodes( '&', text );
        text = Hex2Color( text );
        return text;
    }

    public static String formatDry( @Nullable OfflinePlayer player, @Nullable String text ) {
        if ( text == null ) return "";

        //get uuid to be referenced by placeholder getters
        UUID uuid = ( player != null ) ? player.getUniqueId() : CommandsAPI.serverId;

        //create a container for placeholder getter results
        // initialize with the same size as the placeholder getter map to skip reallocation
        Map<String, String> placeholders = new HashMap<>( SendMessage.placeholders.size() );

        Set<String> keywords = ParseString.keywords( text, SendMessage.placeholders.keySet(), new HashSet<>( Arrays.asList( '[', '%') ), new HashSet<>( Arrays.asList( ']', '%')) );
        //for each placeholder getter, add placeholder and result to container
        SendMessage.placeholders.forEach( (s, uuidStringFunction ) -> {
            if ( !keywords.contains( s) ) return;
            placeholders.put( s, uuidStringFunction.apply( uuid) );
        } );

        //replace all placeholders with their respective string function results
        for ( Map.Entry<String, String> e : placeholders.entrySet() ) {
            text = text.replaceAll( "\\[" + e.getKey() + "]", e.getValue() );
        }

        //check PAPI exists and fill remaining PAPI placeholders
        //todo: if a null player doesn't work with another PAPI import, blame that import for not verifying its inputs.
        text = PAPIChecker.fillPlaceholders( player, text );

        text = ChatColor.translateAlternateColorCodes( '&', text );
        text = Hex2Color( text );
        return text;
    }

    public static String formatNoColor( @Nullable OfflinePlayer player, @Nullable String text ) {
        if ( text == null ) return "";

        //get uuid to be referenced by placeholder getters
        UUID uuid = ( player != null ) ? player.getUniqueId() : CommandsAPI.serverId;

        //create a container for placeholder getter results
        // initialize with the same size as the placeholder getter map to skip reallocation
        Map<String, String> placeholders = new HashMap<>( SendMessage.placeholders.size() );

        Set<String> keywords = ParseString.keywords( text, SendMessage.placeholders.keySet(), new HashSet<>( Arrays.asList( '[', '%') ), new HashSet<>( Arrays.asList( ']', '%')) );
        //for each placeholder getter, add placeholder and result to container
        for ( Map.Entry<String, Function<UUID, String>> entry : SendMessage.placeholders.entrySet() ) {
            String s = entry.getKey();
            if ( !keywords.contains( s) ) continue;
            Function<UUID, String> uuidStringFunction = entry.getValue();
            placeholders.put( s, uuidStringFunction.apply( uuid) );
        }

        //replace all placeholders with their respective string function results
        for ( Map.Entry<String, String> e : placeholders.entrySet() ) {
            text = Pattern.compile( "\\[" + e.getKey() + "]",Pattern.CASE_INSENSITIVE ).matcher( text ).replaceAll( e.getValue() );
            text = Pattern.compile( "%" + e.getKey() + "%",Pattern.CASE_INSENSITIVE ).matcher( text ).replaceAll( e.getValue() );
        }

        if ( lang != null ) {
            Pattern placeholderIntPattern = Pattern.compile( "\\[( [Pp] )( \\d* )]" );
            Pattern placeholderIntPattern2 = Pattern.compile( "%( [Pp] )( \\d* )%" );
            Matcher placeholderIntMatcher = placeholderIntPattern.matcher( text );
            Matcher placeholderIntMatcher2 = placeholderIntPattern.matcher( text );
            while ( placeholderIntMatcher.find() ) {
                String group = placeholderIntMatcher.group( 2 );
                int bits;
                try {
                    bits = Integer.parseInt( group );
                } catch ( NumberFormatException ignored ) {
                    continue;
                }
                placeholderIntMatcher.reset();

                String replacement = "[invalid]";
                ConfigParser<MessagesKeys> parser = ( ConfigParser<MessagesKeys> ) RTP.configs.getParser( MessagesKeys.class );
                Object o = parser.getConfigValue( MessagesKeys.placeholders, new ArrayList<>() );
                if ( o instanceof List ) {
                    List<?> pList = ( List<?> ) o;
                    if ( pList.size() > bits ) {
                        replacement = pList.get( bits ).toString();
                    }
                }

                replacement = placeholderIntPattern.matcher( replacement ).replaceAll( "" );

                text = placeholderIntMatcher.replaceFirst( replacement );
                placeholderIntMatcher = placeholderIntPattern.matcher( text );
            }

            while ( placeholderIntMatcher2.find() ) {
                String group = placeholderIntMatcher2.group( 2 );
                int bits;
                try {
                    bits = Integer.parseInt( group );
                } catch ( NumberFormatException ignored ) {
                    continue;
                }
                placeholderIntMatcher2.reset();

                String replacement = "[invalid]";
                ConfigParser<MessagesKeys> parser = ( ConfigParser<MessagesKeys> ) RTP.configs.getParser( MessagesKeys.class );
                Object o = parser.getConfigValue( MessagesKeys.placeholders, new ArrayList<>() );
                if ( o instanceof List ) {
                    List<?> pList = ( List<?> ) o;
                    if ( pList.size() > bits ) {
                        replacement = pList.get( bits ).toString();
                    }
                }

                replacement = placeholderIntPattern2.matcher( replacement ).replaceAll( "" );

                text = placeholderIntMatcher2.replaceFirst( replacement );
                placeholderIntMatcher2 = placeholderIntPattern.matcher( text );
            }
        }

        //check PAPI exists and fill remaining PAPI placeholders
        //todo: if a null player doesn't work with another PAPI import, blame that import for not verifying its inputs.
        text = PAPIChecker.fillPlaceholders( player, text );

        return text;
    }


    private static String Hex2Color( String text ) {
        //reduce patterns
        if ( text == null ) return "";
        Matcher matcher2 = hexColorPattern2.matcher( text );
        while ( matcher2.find() ) {
            String hexColor = text.substring( matcher2.start(), matcher2.end() );
            String shortColor = "#" + hexColor.replaceAll( "&", "" );
            text = text.replaceAll( hexColor, shortColor );
        }

        //colorize
        Matcher matcher1 = hexColorPattern1.matcher( text );
        while ( matcher1.find() ) {
            String hexColor = text.substring( matcher1.start(), matcher1.end() );
            String bukkitColor;
            StringBuilder bukkitColorCode = new StringBuilder( '§' + "x" );
            for ( int i = hexColor.indexOf( '#' ) + 1; i < hexColor.length(); i++ ) {
                bukkitColorCode.append( '§' ).append( hexColor.charAt( i) );
            }
            bukkitColor = bukkitColorCode.toString().toLowerCase();
            text = text.replaceAll( hexColor, bukkitColor );
            matcher1.reset( text );
        }
        return text;
    }

    public static void log( Level level, String message ) {
        if ( message.isEmpty() ) return;

        message = format( null, message );

        if( RTP.serverAccessor.getServerIntVersion() <= 12 ) message = ChatColor.stripColor( message );

        if ( level.equals( Level.INFO) ) {
            String[] split = message.split( "\n" );
            for ( String s : split ) {
                s = ChatColor.WHITE + s;
                if( RTP.serverAccessor.getServerIntVersion() > 12 ) {
                    BaseComponent[] baseComponents = TextComponent.fromLegacyText( s );
                    Bukkit.getConsoleSender().spigot().sendMessage( baseComponents );
                }
                else Bukkit.getLogger().log( Level.INFO,s );
            }
        } else if ( level.equals( Level.CONFIG) ) {
            String[] split = message.split( "\n" );
            for ( String s : split ) {
                s = ChatColor.GREEN + s;
                if( RTP.serverAccessor.getServerIntVersion() > 12 ) {
                    BaseComponent[] baseComponents = TextComponent.fromLegacyText( s );
                    Bukkit.getConsoleSender().spigot().sendMessage( baseComponents );
                }
                else Bukkit.getLogger().log( Level.INFO,s );
            }
        } else if ( level.equals( Level.WARNING) ) {
            String[] split = message.split( "\n" );
            for ( String s : split ) {
                s = ChatColor.YELLOW + s;
                if( RTP.serverAccessor.getServerIntVersion() > 12 ) {
                    BaseComponent[] baseComponents = TextComponent.fromLegacyText( s );
                    Bukkit.getConsoleSender().spigot().sendMessage( baseComponents );
                }
                else Bukkit.getLogger().log( Level.WARNING,s );
            }
        } else {
            Logger logger = Bukkit.getLogger();
            if ( logger != null ) logger.log( level, message );
        }
    }

    public static void log( Level level, String message, Throwable throwable ) {
        if ( message.isEmpty() ) return;

        if ( level.equals( Level.INFO) )
            Bukkit.getConsoleSender().spigot().sendMessage( TextComponent.fromLegacyText( format( null, message)) );
        else if ( level.equals( Level.CONFIG) )
            Bukkit.getConsoleSender().spigot().sendMessage( TextComponent.fromLegacyText( ChatColor.GREEN + format( null, message)) );
        else if ( level.equals( Level.WARNING) )
            Bukkit.getConsoleSender().spigot().sendMessage( TextComponent.fromLegacyText( ChatColor.YELLOW + format( null, message)) );
        else if ( level.equals( Level.SEVERE) )
            Bukkit.getConsoleSender().spigot().sendMessage( TextComponent.fromLegacyText( ChatColor.RED + format( null, message)) );
        else {
            Logger logger = Bukkit.getLogger();
            if ( logger != null ) logger.log( level, message );
        }

        for ( StackTraceElement element : throwable.getStackTrace() ) {
            Logger logger = Bukkit.getLogger();
            message = element.toString();

            if ( level.equals( Level.INFO) )
                Bukkit.getConsoleSender().spigot().sendMessage( TextComponent.fromLegacyText( format( null, message)) );
            else if ( level.equals( Level.CONFIG) )
                Bukkit.getConsoleSender().spigot().sendMessage( TextComponent.fromLegacyText( ChatColor.GREEN + format( null, message)) );
            else if ( level.equals( Level.WARNING) )
                Bukkit.getConsoleSender().spigot().sendMessage( TextComponent.fromLegacyText( ChatColor.YELLOW + format( null, message)) );
            else if ( level.equals( Level.SEVERE) )
                Bukkit.getConsoleSender().spigot().sendMessage( TextComponent.fromLegacyText( ChatColor.RED + format( null, message)) );
            else {
                if ( logger != null ) logger.log( level, message );
            }
        }
    }

    public static void title( Player player, String title, String subtitle, int in, int stay, int out ) {
        boolean noTitle = title == null || title.isEmpty();
        boolean noSubtitle = subtitle == null || subtitle.isEmpty();

        if ( noTitle && noSubtitle ) return;

        if ( title != null ) {
            title = format( player,title );
        }
        if ( subtitle != null ) {
            subtitle = format( player,subtitle );
        }

        if ( RTP.serverAccessor.getServerIntVersion() < 18 ) TitleApi.sendTitle( player, title, subtitle, in, stay, out );
        else player.sendTitle( title, subtitle, in, stay, out );
    }

    public static void actionbar( Player player, String bar ) {
        if ( bar == null || bar.isEmpty() ) return;
        bar = Hex2Color( ChatColor.translateAlternateColorCodes( '&', bar) );
        if ( RTP.serverAccessor.getServerIntVersion() < 18 ) TitleApi.sendActionbar( player, bar );
        else {
            BaseComponent[] components = TextComponent.fromLegacyText( bar );
            player.spigot().sendMessage( ChatMessageType.ACTION_BAR, components );
        }
    }
}
