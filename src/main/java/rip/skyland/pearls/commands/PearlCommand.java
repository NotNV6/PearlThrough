package rip.skyland.pearls.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import rip.skyland.pearls.Locale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO: clean code
public class PearlCommand implements CommandExecutor {

    private String[] arguments = {
            "taliban", "fence", "tripwire", "slab", "stair", "antiglitch"
    };


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            this.sendHelp(sender, label);
            return true;
        }

        if (Arrays.stream(arguments).noneMatch(argument -> argument.equalsIgnoreCase(args[0])) || (!args[1].equalsIgnoreCase("false") && !args[1].equalsIgnoreCase("true"))) {
            this.sendHelp(sender, label);
            return true;
        }

        String argument = args[0].toLowerCase();


        Locale locale = argument.equals("taliban") ? Locale.TALIBAN_PEARLING : (argument.equals("fence") ? Locale.PEARL_THROUGH_FENCE : (
                argument.equals("tripwire") ? Locale.PEARL_THROUGH_TRIPWIRE : (argument.equals("slab") ? Locale.PEARL_THROUGH_SLAB : (
                        argument.equals("stair") ? Locale.PEARL_THROUGH_STAIR : Locale.ANTI_GLITCH
                ))
        ));

        locale.setValue(Boolean.parseBoolean(args[1]));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&fYou have updated " + argument + "'s value to " + args[1].toLowerCase()));

        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        List<String> strings = new ArrayList<>(Collections.singletonList(StringUtils.repeat("&9&m-", 35)));

        Arrays.stream(arguments).forEach(argument -> strings.add("&c/" + label + " " + argument + " <true|false>"));
        strings.add(StringUtils.repeat("&9&m-", 35));

        strings.forEach(string -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', string)));
    }

}
