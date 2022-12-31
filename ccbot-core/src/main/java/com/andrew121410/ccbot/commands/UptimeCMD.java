package com.andrew121410.ccbot.commands;

import com.andrew121410.ccbot.CCBotCore;
import com.andrew121410.ccbot.commands.manager.ACommand;
import com.andrew121410.ccbot.commands.manager.AbstractCommand;
import com.andrew121410.ccutils.utils.TimeUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@ACommand(command = "uptime", description = "Shows the uptime of the bot.")
public class UptimeCMD extends AbstractCommand {

    private CCBotCore ccBotCore;

    public UptimeCMD(CCBotCore ccBotCore) {
        super(ccBotCore);
        this.ccBotCore = ccBotCore;
    }

    @Override
    public boolean onMessage(MessageReceivedEvent event, String[] args) {
        String theUptime = TimeUtils.makeIntoEnglishWords(this.ccBotCore.getUptime(), System.currentTimeMillis(), false, false);
        event.getChannel().sendMessage("The uptime of the bot is " + theUptime).queue();
        return true;
    }
}