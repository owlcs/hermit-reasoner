package org.semanticweb.HermiT.debugger.commands;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.debugger.Debugger;

public class ModelStatsCommand extends AbstractCommand {

    public ModelStatsCommand(Debugger debugger) {
        super(debugger);
    }
    public String getCommandName() {
        return "modelStats";
    }
    public String[] getDescription() {
        return new String[] { "","prints statistics about a model" };
    }
    public void printHelp(PrintWriter writer) {
        writer.println("usage: modelStats");
        writer.println("Prints the statistics about the current model.");
    }
    public void execute(String[] args) {
        int noNodes=0;
        int noUnblockedNodes=0;
        int noDirectlyBlockedNodes=0;
        int noIndirectlyBlockedNodes=0;
        Node node=m_debugger.getTableau().getFirstTableauNode();
        while (node!=null) {
            noNodes++;
            if (node.isDirectlyBlocked())
                noDirectlyBlockedNodes++;
            else if (node.isIndirectlyBlocked())
                noIndirectlyBlockedNodes++;
            else
                noUnblockedNodes++;
            node=node.getNextTableauNode();
        }
        CharArrayWriter buffer=new CharArrayWriter();
        PrintWriter writer=new PrintWriter(buffer);
        writer.println("  Model statistics");
        writer.println("================================================");
        writer.println("  Number of nodes:                    "+noNodes);
        writer.println("  Number of unblocked nodes:          "+noUnblockedNodes);
        writer.println("  Number of directly blocked nodes:   "+noDirectlyBlockedNodes);
        writer.println("  Number of indirectly blocked nodes: "+noIndirectlyBlockedNodes);
        writer.println("================================================");
        writer.flush();
        showTextInWindow(buffer.toString(),"Model statistics");
        selectConsoleWindow();
    }
}