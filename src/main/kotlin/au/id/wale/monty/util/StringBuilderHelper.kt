package au.id.wale.monty.util

// This
operator fun StringBuilder.plusAssign(text: Any) {
    this.append("$text ")
}