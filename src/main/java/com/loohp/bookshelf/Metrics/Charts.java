package com.loohp.bookshelf.Metrics;

import com.loohp.bookshelf.Bookshelf;
import com.loohp.bookshelf.BookshelfManager;

public class Charts {

    private Charts(){ }

    public static void loadCharts(Metrics metrics) {
        metrics.addCustomChart(new Metrics.SingleLineChart("total_bookshelves", () -> BookshelfManager.getJsonObject().size()));

        metrics.addCustomChart(new Metrics.SimplePie("hoppers_enabled", () -> {
            String string = "Disabled";
            if (Bookshelf.EnableHopperSupport) {
                string = "Enabled";
            }
            return string;
        }));

        metrics.addCustomChart(new Metrics.SimplePie("droppers_enabled", () -> {
            String string = "Disabled";
            if (Bookshelf.EnableDropperSupport) {
                string = "Enabled";
            }
            return string;
        }));

        metrics.addCustomChart(new Metrics.SimplePie("enchtable_enabled", () -> {
            String string = "Disabled";
            if (Bookshelf.enchantmentTable) {
                string = "Enabled";
            }
            return string;
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("average_hopper_process_time", () -> {
            int num = Integer.MAX_VALUE;
            if (Bookshelf.lastHopperTime < 2147483647) {
                num = (int) Bookshelf.lastHopperTime;
            }
            return num;
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("average_hopper_minecart_process_time", () -> {
            int num = Integer.MAX_VALUE;
            if (Bookshelf.lastHoppercartTime < 2147483647) {
                num = (int) Bookshelf.lastHoppercartTime;
            }
            return num;
        }));
    }

}
