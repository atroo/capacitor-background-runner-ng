package de.atroo.backgroundrunnerng

sealed class BackgroundRunnerPluginError(message: String) : Exception(message) {
    object NoRunnerConfig : BackgroundRunnerPluginError("No Runner Configuration")

    data class InvalidRunnerConfig(val reason: String) : BackgroundRunnerPluginError("Invalid Runner Configuration: $reason")

    data class InvalidArgument(val reason: String) : BackgroundRunnerPluginError("Invalid Argument: $reason")

    data class RunnerError(val reason: String) : BackgroundRunnerPluginError("Runner Error: $reason")

    data class GenericError(val reason: String) : BackgroundRunnerPluginError("Generic Error: $reason")

    // Equatable equivalent: Implement 'equals' and 'hashCode' for comparisons
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BackgroundRunnerPluginError) return false
        return this.message == other.message
    }

    override fun hashCode(): Int {
        return message.hashCode()
    }
}