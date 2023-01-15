package eu.kueue.example.pg

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.subcommands
import eu.kueue.example.pg.command.CommandConsumer
import eu.kueue.example.pg.command.CommandProducer

fun main(args: Array<String>) = App()
    .subcommands(
        CommandProducer(),
        CommandConsumer(),
    )
    .main(args)

class App : CliktCommand(
    name = "Kueue CLI",
    invokeWithoutSubcommand = true,
) {
    override fun run() {
        if (this.currentContext.originalArgv.isEmpty()) {
            throw PrintHelpMessage(
                command = this,
                error = false
            )
        }
    }
}
