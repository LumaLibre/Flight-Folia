/*
 * Flight
 * Copyright 2022 Kiran Hart
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ca.tweetzy.flight.command;

import ca.tweetzy.flight.utils.Common;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Date Created: April 09 2022
 * Time Created: 11:53 p.m.
 *
 * @author Kiran Hart
 */
public final class MainCommand extends Command {

    final Plugin plugin;

    private String header = null;
    private String description;
    private boolean sortHelp = false;
    final String command;

    protected final NestedCommand nestedCommands;

    public MainCommand(Plugin plugin, String command) {
        super(AllowedExecutor.BOTH, command);
        this.command = command;
        this.plugin = plugin;
        this.description = "Shows the command help page for /" + command;
        this.nestedCommands = new NestedCommand(this);
    }

    @Override
    protected ReturnType execute(CommandSender sender, String... args) {
        sender.sendMessage("");

        if (header != null) {
            Common.tell(sender, header);
        } else {
            Common.tell(sender, String.format("#ff8080&l%s &8» &7Version %s Created By #00a87fT#00ae7fw#00b57ee#00bb7ce#00c27at#00c877z#00ce74y", this.plugin.getDescription().getName(), this.plugin.getDescription().getVersion()));
        }

        sender.sendMessage("");
        if (nestedCommands != null) {
            List<String> commands = nestedCommands.children.values().stream().distinct().map(c -> c.getSubCommands().get(0)).toList();

            if (sortHelp) {
                Collections.sort(commands);
            }

            Common.tell(sender, "&8- &e" + getSyntax() + "&7 - " + getDescription());

            for (String cmdStr : commands) {
                final Command cmd = nestedCommands.children.get(cmdStr);
                if (cmd == null) continue;
                if (cmd.getPermissionNode() == null || sender.hasPermission(cmd.getPermissionNode())) {
                    Common.tell(sender, "&8- &e" + cmd.getSyntax() + "&7 - " + cmd.getDescription());
                }
            }
        }

        Common.tell(sender, "");
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> tab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/" + this.command;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    /*
    =================== Methods ===================
     */
    public MainCommand setHeader(String header) {
        this.header = header;
        return this;
    }

    public MainCommand setDescription(String description) {
        this.description = description;
        return this;
    }

    public MainCommand setSortHelp(boolean sortHelp) {
        this.sortHelp = sortHelp;
        return this;
    }

    public MainCommand addSubCommand(Command command) {
        nestedCommands.addSubCommand(command);
        return this;
    }

    public MainCommand addSubCommands(Command... commands) {
        nestedCommands.addSubCommands(commands);
        return this;
    }
}
