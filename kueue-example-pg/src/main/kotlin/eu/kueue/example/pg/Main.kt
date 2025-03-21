package eu.kueue.example.pg

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import eu.kueue.example.pg.command.CommandBenchmark
import eu.kueue.example.pg.command.CommandConsumer
import eu.kueue.example.pg.command.CommandProducer

fun main(args: Array<String>) = App()
    .subcommands(
        CommandBenchmark(),
        CommandProducer(),
        CommandConsumer(),
    )
    .main(args)

class App : CliktCommand(name = "Kueue CLI") {

    override val allowMultipleSubcommands: Boolean = true

    override fun run() {
        if (this.currentContext.invokedSubcommand == null) {
            throw PrintHelpMessage(
                context = this.currentContext,
                error = false,
            )
        }
    }
}
