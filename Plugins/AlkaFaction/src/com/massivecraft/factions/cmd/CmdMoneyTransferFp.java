package com.massivecraft.factions.cmd;

import org.bukkit.ChatColor;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.P;
import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;

public class CmdMoneyTransferFp extends FCommand {
    public CmdMoneyTransferFp() {
        this.aliases.add("fp");

        this.requiredArgs.add("amount");
        this.requiredArgs.add("faction");
        this.requiredArgs.add("player");

        //this.optionalArgs.put("", "");

        this.permission = Permission.MONEY_F2P.node;
        this.setHelpShort("transfer f -> p");

        this.senderMustBePlayer = false;
        this.senderMustBeMember = false;
        this.senderMustBeOfficer = false;
        this.senderMustBeLeader = false;
    }

    @Override
    public void perform() {
        final double amount = this.argAsDouble(0, 0d);
        final EconomyParticipator from = this.argAsFaction(1);
        if (from == null) return;
        final EconomyParticipator to = this.argAsBestFPlayerMatch(2);
        if (to == null) return;

        final boolean success = Econ.transferMoney(this.fme, from, to, amount);

        if (success && Conf.logMoneyTransactions) {
            P.p.log(ChatColor.stripColor(P.p.txt.parse("%s transferred %s from the faction \"%s\" to the player \"%s\"", this.fme.getName(), Econ.moneyString(amount), from.describeTo(null), to.describeTo(null))));
        }
    }
}
