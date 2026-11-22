package ch.verein.whosintoday

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
