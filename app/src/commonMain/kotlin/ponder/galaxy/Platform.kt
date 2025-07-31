package ponder.galaxy

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform